package com.azkari.wasalati

import android.app.Application
import android.icu.text.SimpleDateFormat
import android.icu.util.ULocale
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import java.io.File
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FaithfulMainViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val app = application
    private val dataRepository = WebParityDataRepository(app)
    private val prayerLogStore = PrayerLogStore(app)
    private val qadaStore = QadaStore(app)
    private val quranRepository = QuranRepository(app)
    private val exoPlayer = ExoPlayer.Builder(app).build()
    private val timeFormatter = java.text.SimpleDateFormat("hh:mm a", Locale("ar"))

    private val snoozeUntil = mutableMapOf<PrayerKey, Long>()
    private var toastJob: Job? = null
    private var audioMonitorJob: Job? = null
    private var currentAudioTimestamps: List<QuranTimestamp> = emptyList()
    private var currentAudioMeta: QuranSurahMeta? = null

    val itemRemaining = mutableStateMapOf<String, Int>()

    var uiState by mutableStateOf(
        AppUiState2(
            cityResults = dataRepository.popularCities(),
            quran = QuranUiState(
                currentPage = QuranPrefsStore.currentPage(app),
                dailyGoal = QuranPrefsStore.dailyGoal(app),
                dailyLog = QuranPrefsStore.getTodayLog(app),
                selectedReciter = QuranPrefsStore.selectedReciter(app),
                stats = QuranPrefsStore.statsLast30Days(app).toUi(),
            ),
        ),
    )
        private set

    init {
        bindPlayer()
        restoreSettings()
        refreshUi()
        refreshQuranSupportState()
        startTicker()
    }

    override fun onCleared() {
        audioMonitorJob?.cancel()
        exoPlayer.release()
        super.onCleared()
    }

    fun refreshTick() {
        refreshUi()
    }

    fun onConnectivityChanged(isOffline: Boolean) {
        uiState = uiState.copy(isOffline = isOffline)
    }

    fun openModal(modal: AppModal) {
        uiState = uiState.copy(activeModal = modal)
        if (modal == AppModal.QURAN) {
            initializeQuran()
        }
    }

    fun dismissModal() {
        if (uiState.activeModal == AppModal.QURAN) {
            stopAudio(resetOnly = false)
        }
        uiState = uiState.copy(activeModal = AppModal.NONE, prayerPrompt = null)
    }

    fun toggleBanner() {
        uiState = uiState.copy(bannerCollapsed = !uiState.bannerCollapsed)
    }

    fun setBannerCollapsed(collapsed: Boolean) {
        if (uiState.bannerCollapsed != collapsed) {
            uiState = uiState.copy(bannerCollapsed = collapsed)
        }
    }

    fun selectTab(tab: HomeTab, manual: Boolean = true) {
        uiState = uiState.copy(
            selectedTab = tab,
            manualTabOverride = manual,
        )
        refreshUi()
    }

    fun resetAutoTab() {
        uiState = uiState.copy(manualTabOverride = false)
        refreshUi()
    }

    fun searchCities(query: String) {
        uiState = uiState.copy(
            cityResults = if (query.isBlank()) dataRepository.popularCities() else dataRepository.searchCities(query),
        )
    }

    fun setCity(city: City) {
        val updatedSettings = SettingsStore.load(app).copy(
            cityName = city.name,
            countryName = city.country,
            latitude = city.lat,
            longitude = city.lon,
            calcMethod = uiState.calcMethod,
            reminders = uiState.reminderSettings,
        )
        SettingsStore.save(app, updatedSettings)
        uiState = uiState.copy(
            currentCity = city,
            activeModal = AppModal.NONE,
        )
        refreshUi()
        PrayerScheduler.scheduleAll(app)
    }

    fun setCurrentLocation(latitude: Double, longitude: Double) {
        val nearest = dataRepository.resolveNearestCity(latitude, longitude)
        val resolvedCity = when {
            nearest == null -> City(
                name = "موقعي الحالي",
                country = "",
                lat = latitude,
                lon = longitude,
            )

            distanceKm(latitude, longitude, nearest.lat, nearest.lon) <= 30.0 -> nearest.copy(
                lat = latitude,
                lon = longitude,
            )

            else -> City(
                name = "موقعي الحالي",
                country = nearest.country,
                lat = latitude,
                lon = longitude,
            )
        }
        setCity(resolvedCity)
        pushToast("تم تحديث الموقع الحالي.")
    }

    fun changeCalcMethod(method: String) {
        val current = SettingsStore.load(app)
        SettingsStore.save(
            app,
            current.copy(
                cityName = uiState.currentCity?.name,
                countryName = uiState.currentCity?.country,
                latitude = uiState.currentCity?.lat,
                longitude = uiState.currentCity?.lon,
                calcMethod = method,
                reminders = uiState.reminderSettings,
            ),
        )
        uiState = uiState.copy(calcMethod = method)
        refreshUi()
        PrayerScheduler.scheduleAll(app)
        pushToast("تم تحديث طريقة الحساب.")
    }

    fun updateReminderSettings(settings: ReminderSettings) {
        val current = SettingsStore.load(app)
        SettingsStore.save(
            app,
            current.copy(
                cityName = uiState.currentCity?.name,
                countryName = uiState.currentCity?.country,
                latitude = uiState.currentCity?.lat,
                longitude = uiState.currentCity?.lon,
                calcMethod = uiState.calcMethod,
                reminders = settings,
            ),
        )
        uiState = uiState.copy(reminderSettings = settings)
        PrayerScheduler.scheduleAll(app)
    }

    fun markPrayer(prayerKey: PrayerKey, status: String?) {
        prayerLogStore.saveTodayStatus(prayerKey, status)
        if (status == "missed") {
            val updated = uiState.qada.toMutableMap()
            updated[prayerKey] = (updated[prayerKey] ?: 0) + 1
            qadaStore.save(updated)
        }
        refreshUi()
    }

    fun changeQada(prayerKey: PrayerKey, delta: Int) {
        val updated = uiState.qada.toMutableMap()
        updated[prayerKey] = ((updated[prayerKey] ?: 0) + delta).coerceAtLeast(0)
        qadaStore.save(updated)
        uiState = uiState.copy(qada = updated)
    }

    fun decrementAzkar(sectionId: String, index: Int, initialCount: Int) {
        val key = "$sectionId:$index"
        val current = itemRemaining[key] ?: initialCount
        if (current > 0) {
            itemRemaining[key] = current - 1
        }
    }

    fun handleOpenTab(tab: String?) {
        val resolvedTab = when (tab) {
            HomeTab.MORNING.openTabKey -> HomeTab.MORNING
            HomeTab.EVENING.openTabKey -> HomeTab.EVENING
            HomeTab.PRAYER.openTabKey -> HomeTab.PRAYER
            HomeTab.SLEEP.openTabKey -> HomeTab.SLEEP
            HomeTab.FRIDAY.openTabKey -> HomeTab.FRIDAY
            else -> null
        } ?: return
        selectTab(resolvedTab, manual = true)
    }

    fun confirmPrayer(didPray: Boolean) {
        val prompt = uiState.prayerPrompt ?: run {
            uiState = uiState.copy(activeModal = AppModal.NONE)
            return
        }
        if (didPray) {
            prayerLogStore.saveTodayStatus(prompt.prayerKey, "done")
            pushToast("تقبل الله منا ومنكم صالح الأعمال 🤲")
        } else {
            prayerLogStore.saveTodayStatus(prompt.prayerKey, "missed")
            val updated = uiState.qada.toMutableMap()
            updated[prompt.prayerKey] = (updated[prompt.prayerKey] ?: 0) + 1
            qadaStore.save(updated)
        }
        uiState = uiState.copy(activeModal = AppModal.NONE, prayerPrompt = null)
        refreshUi()
    }

    fun snoozePrayerPrompt() {
        uiState.prayerPrompt?.let { prompt ->
            snoozeUntil[prompt.prayerKey] = System.currentTimeMillis() + 30 * 60_000L
        }
        uiState = uiState.copy(activeModal = AppModal.NONE, prayerPrompt = null)
    }

    fun openQuran() {
        openModal(AppModal.QURAN)
    }

    fun openQuranAtPage(page: Int) {
        setQuranCurrentPage(page)
        openModal(AppModal.QURAN)
    }

    fun openSurahKahf() {
        openQuranAtPage(293)
    }

    fun dismissToast(id: Long) {
        if (uiState.currentToast?.id == id) {
            uiState = uiState.copy(currentToast = null)
        }
    }

    fun downloadQuranText() {
        viewModelScope.launch {
            uiState = uiState.copy(
                quran = uiState.quran.copy(
                    download = uiState.quran.download.copy(
                        isDownloading = true,
                        error = null,
                        current = 0,
                        total = 114,
                    ),
                ),
            )
            try {
                quranRepository.ensureTextDownloaded { current, total ->
                    uiState = uiState.copy(
                        quran = uiState.quran.copy(
                            download = uiState.quran.download.copy(
                                isDownloading = true,
                                current = current,
                                total = total,
                            ),
                        ),
                    )
                }
                pushToast("تم تحميل القرآن الكريم.")
                uiState = uiState.copy(
                    quran = uiState.quran.copy(
                        download = uiState.quran.download.copy(
                            isDownloading = false,
                            isDownloaded = true,
                            current = 114,
                            total = 114,
                            error = null,
                        ),
                    ),
                )
                loadQuranPage(uiState.quran.currentPage)
                refreshQuranSupportState()
            } catch (_: Exception) {
                uiState = uiState.copy(
                    quran = uiState.quran.copy(
                        download = uiState.quran.download.copy(
                            isDownloading = false,
                            error = "تعذر تحميل القرآن الآن.",
                        ),
                    ),
                )
                pushToast("تعذر تحميل القرآن الآن.")
            }
        }
    }

    fun showQuranMode(mode: QuranReaderMode) {
        uiState = uiState.copy(quran = uiState.quran.copy(mode = mode))
        if (mode == QuranReaderMode.SURAH_LIST || mode == QuranReaderMode.ACHIEVEMENTS) {
            refreshQuranSupportState()
        }
    }

    fun updateQuranSearchQuery(query: String) {
        uiState = uiState.copy(quran = uiState.quran.copy(searchQuery = query))
        if (query.isBlank()) {
            uiState = uiState.copy(quran = uiState.quran.copy(searchResults = emptyList()))
            return
        }
        viewModelScope.launch {
            val results = quranRepository.search(query)
            uiState = uiState.copy(
                quran = uiState.quran.copy(
                    searchResults = results,
                ),
            )
        }
    }

    fun jumpToSurah(surahNumber: Int) {
        viewModelScope.launch {
            val page = quranRepository.firstPageOfSurah(surahNumber) ?: return@launch
            loadQuranPage(page)
            uiState = uiState.copy(
                quran = uiState.quran.copy(
                    mode = QuranReaderMode.READER,
                    searchQuery = "",
                    searchResults = emptyList(),
                ),
            )
            pushToast("تم الانتقال إلى السورة.")
        }
    }

    fun nextQuranPage() {
        if (uiState.quran.currentPage < 604) {
            loadQuranPage(uiState.quran.currentPage + 1)
        }
    }

    fun prevQuranPage() {
        if (uiState.quran.currentPage > 1) {
            loadQuranPage(uiState.quran.currentPage - 1)
        }
    }

    fun markCurrentQuranPageRead(advancePage: Boolean = true) {
        val updated = QuranPrefsStore.markPageRead(app, uiState.quran.currentPage)
        uiState = uiState.copy(
            quran = uiState.quran.copy(
                dailyLog = updated,
                stats = QuranPrefsStore.statsLast30Days(app).toUi(),
            ),
        )
        val goal = uiState.quran.dailyGoal
        if (goal > 0 && updated.pagesRead.size >= goal && updated.completed) {
            pushToast("ما شاء الله! أتممت وِردك.")
        } else {
            pushToast("صفحة ${updated.pagesRead.size}${if (goal > 0) "/$goal" else ""}")
        }
        if (advancePage && uiState.quran.currentPage < 604) {
            loadQuranPage(uiState.quran.currentPage + 1)
        }
    }

    fun setQuranDailyGoal(goal: Int) {
        val updated = QuranPrefsStore.setDailyGoal(app, goal)
        uiState = uiState.copy(
            quran = uiState.quran.copy(
                dailyGoal = QuranPrefsStore.dailyGoal(app),
                dailyLog = updated,
                stats = QuranPrefsStore.statsLast30Days(app).toUi(),
            ),
        )
        pushToast(if (goal > 0) "الوِرد اليومي: $goal صفحات" else "بدون هدف يومي")
    }

    fun selectReciter(reciter: QuranReciter) {
        QuranPrefsStore.setSelectedReciter(app, reciter.id)
        uiState = uiState.copy(
            quran = uiState.quran.copy(
                selectedReciter = reciter,
                audio = uiState.quran.audio.copy(
                    downloadedSurahs = emptySet(),
                    singleDownloadLabel = null,
                    bulkDownloadLabel = null,
                ),
            ),
        )
        refreshQuranSupportState()
        pushToast(reciter.name)
    }

    fun playSurah(surahNumber: Int, startAyahIndex: Int = 0) {
        viewModelScope.launch {
            try {
                if (!quranRepository.isTextDownloaded()) {
                    quranRepository.ensureTextDownloaded()
                }
                val meta = quranRepository.surahMetadata().firstOrNull { it.number == surahNumber }
                val descriptor = quranRepository.resolveAudio(uiState.quran.selectedReciter, surahNumber)
                currentAudioMeta = meta
                currentAudioTimestamps = descriptor.timestamps
                val mediaItem = if (descriptor.isLocalFile) {
                    MediaItem.fromUri(Uri.fromFile(File(descriptor.uri)))
                } else {
                    MediaItem.fromUri(descriptor.uri)
                }
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                if (startAyahIndex in descriptor.timestamps.indices) {
                    exoPlayer.seekTo(descriptor.timestamps[startAyahIndex].startMs)
                }
                exoPlayer.playWhenReady = true
                val startPage = quranRepository.firstPageOfSurah(surahNumber)
                if (startPage != null) loadQuranPage(startPage)
                uiState = uiState.copy(
                    quran = uiState.quran.copy(
                        audio = uiState.quran.audio.copy(
                            currentSurah = surahNumber,
                            currentAyahIndex = startAyahIndex,
                            isPlaying = true,
                            label = buildAudioLabel(meta, startAyahIndex),
                        ),
                    ),
                )
                startAudioMonitor()
            } catch (_: Exception) {
                pushToast("تعذر تشغيل التلاوة الآن.")
            }
        }
    }

    fun toggleReaderPlayPause() {
        val audio = uiState.quran.audio
        if (audio.currentSurah == 0) {
            val firstSurah = uiState.quran.pageGroups.firstOrNull()?.surahNum ?: return
            playSurah(firstSurah)
            return
        }
        if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
        uiState = uiState.copy(
            quran = uiState.quran.copy(
                audio = uiState.quran.audio.copy(isPlaying = exoPlayer.isPlaying),
            ),
        )
    }

    fun prevAyahInQueue() {
        val previousIndex = (uiState.quran.audio.currentAyahIndex - 1).coerceAtLeast(0)
        currentAudioTimestamps.getOrNull(previousIndex)?.let { exoPlayer.seekTo(it.startMs) }
    }

    fun skipNextSurah() {
        val next = (uiState.quran.audio.currentSurah + 1).coerceAtMost(114)
        if (next != uiState.quran.audio.currentSurah) {
            playSurah(next, 0)
        }
    }

    fun downloadAudioSurah(surahNumber: Int) {
        viewModelScope.launch {
            val meta = quranRepository.surahMetadata().firstOrNull { it.number == surahNumber }
            try {
                quranRepository.downloadAudioSurah(uiState.quran.selectedReciter, surahNumber) { status ->
                    uiState = uiState.copy(
                        quran = uiState.quran.copy(
                            audio = uiState.quran.audio.copy(singleDownloadLabel = status),
                        ),
                    )
                }
                refreshQuranSupportState()
                pushToast("تم تحميل ${meta?.name ?: "السورة"}")
            } catch (_: Exception) {
                pushToast("تعذر تحميل السورة الآن.")
            } finally {
                uiState = uiState.copy(
                    quran = uiState.quran.copy(
                        audio = uiState.quran.audio.copy(singleDownloadLabel = null),
                    ),
                )
            }
        }
    }

    fun downloadAllSurahs() {
        viewModelScope.launch {
            try {
                quranRepository.downloadAllSurahs(uiState.quran.selectedReciter) { current, total, label ->
                    uiState = uiState.copy(
                        quran = uiState.quran.copy(
                            audio = uiState.quran.audio.copy(
                                bulkDownloadLabel = "$label (${current}/${total})",
                            ),
                        ),
                    )
                }
                refreshQuranSupportState()
                pushToast("تم تحميل السور المتبقية.")
            } catch (_: Exception) {
                pushToast("تعذر تحميل جميع السور الآن.")
            } finally {
                uiState = uiState.copy(
                    quran = uiState.quran.copy(
                        audio = uiState.quran.audio.copy(bulkDownloadLabel = null),
                    ),
                )
            }
        }
    }

    fun deleteQuranData() {
        viewModelScope.launch {
            stopAudio(resetOnly = true)
            quranRepository.deleteDownloadedQuran()
            uiState = uiState.copy(
                quran = uiState.quran.copy(
                    mode = QuranReaderMode.DOWNLOAD,
                    pageGroups = emptyList(),
                    download = QuranDownloadUi(),
                    audio = QuranAudioUi(),
                ),
            )
            refreshQuranSupportState()
            pushToast("تم حذف بيانات القرآن المحمّلة.")
        }
    }

    private fun initializeQuran() {
        viewModelScope.launch {
            val isDownloaded = quranRepository.isTextDownloaded()
            uiState = uiState.copy(
                quran = uiState.quran.copy(
                    download = uiState.quran.download.copy(
                        isDownloaded = isDownloaded,
                        isDownloading = false,
                        error = null,
                    ),
                    mode = if (isDownloaded) QuranReaderMode.READER else QuranReaderMode.DOWNLOAD,
                ),
            )
            refreshQuranSupportState()
            if (isDownloaded) {
                loadQuranPage(uiState.quran.currentPage)
            }
        }
    }

    private fun loadQuranPage(page: Int) {
        viewModelScope.launch {
            val safePage = page.coerceIn(1, 604)
            QuranPrefsStore.setCurrentPage(app, safePage)
            val (juz, groups) = quranRepository.page(safePage)
            uiState = uiState.copy(
                quran = uiState.quran.copy(
                    currentPage = safePage,
                    firstJuzOnPage = juz,
                    pageGroups = groups,
                    mode = QuranReaderMode.READER,
                ),
            )
        }
    }

    private fun restoreSettings() {
        val settings = SettingsStore.load(app)
        val restoredCity = if (settings.hasLocation) {
            City(
                name = settings.cityName.orEmpty(),
                country = settings.countryName.orEmpty(),
                lat = settings.latitude!!,
                lon = settings.longitude!!,
            )
        } else {
            null
        }
        uiState = uiState.copy(
            isLoading = false,
            currentCity = restoredCity,
            calcMethod = settings.calcMethod,
            reminderSettings = settings.reminders,
            qada = qadaStore.load(),
        )
    }

    private fun startTicker() {
        viewModelScope.launch {
            while (isActive) {
                refreshTick()
                delay(1_000L)
            }
        }
    }

    private fun refreshUi() {
        val now = Date()
        val prayerTimes = uiState.currentCity?.let {
            PrayerCalculation.calculatePrayerTimes(
                latitude = it.lat,
                longitude = it.lon,
                method = uiState.calcMethod,
                date = now,
            )
        }

        if (prayerTimes != null) {
            autoMarkMissedPrayers(prayerTimes, now)
        }

        val prayerLog = prayerLogStore.getTodayLog()
        val qada = qadaStore.load()
        val prayerSummary = if (uiState.currentCity != null && prayerTimes != null) {
            PrayerCalculation.buildSummary(
                prayerTimes = prayerTimes,
                city = uiState.currentCity!!,
                method = uiState.calcMethod,
                sunan = dataRepository.sunan(),
                now = now,
            )
        } else {
            null
        }

        val suggestion = when {
            prayerTimes == null -> "اختر مدينة لعرض المواقيت والأذكار"
            isWithinPostPrayerWindow(prayerTimes, now) -> "🤲 وقت أذكار دبر الصلاة"
            now < prayerTimes.asr -> "☀️ أذكار الاستيقاظ والصباح"
            now < prayerTimes.isha -> "🌙 أذكار المساء"
            else -> "💤 أذكار النوم والمساء"
        }

        val resolvedTab = if (uiState.manualTabOverride || prayerTimes == null) {
            uiState.selectedTab
        } else {
            when {
                isWithinPostPrayerWindow(prayerTimes, now) -> HomeTab.PRAYER
                now < prayerTimes.asr -> HomeTab.MORNING
                now < prayerTimes.isha -> HomeTab.EVENING
                else -> HomeTab.SLEEP
            }
        }

        val weeklyStats = prayerLogStore.lastSevenDaysStats().mapIndexed { index, stat ->
            WeeklyPrayerUi(
                label = when (index) {
                    0 -> "اليوم"
                    1 -> "أمس"
                    else -> arabicWeekday(stat.date)
                },
                done = stat.done,
                missed = stat.missed,
                isToday = stat.isToday,
            )
        }

        uiState = uiState.copy(
            currentPrayerTimes = prayerTimes,
            prayerSummary = prayerSummary,
            prayerDots = buildPrayerDots(prayerTimes, prayerLog, now),
            hijriDate = currentHijriDate(now),
            selectedTab = resolvedTab,
            homeSections = if (prayerTimes == null && uiState.currentCity == null) {
                emptyList()
            } else {
                dataRepository.buildSections(resolvedTab, prayerTimes, now)
            },
            suggestion = suggestion,
            qada = qada,
            prayerLog = prayerLog,
            todayDoneCount = prayerLogStore.todayDoneCount(),
            streakDays = prayerLogStore.streakDays(),
            weeklyPrayerUi = weeklyStats,
            quran = uiState.quran.copy(
                dailyGoal = QuranPrefsStore.dailyGoal(app),
                dailyLog = QuranPrefsStore.getTodayLog(app),
                stats = QuranPrefsStore.statsLast30Days(app).toUi(),
            ),
        )

        evaluatePrayerPrompt(prayerTimes, prayerLog, now)
    }

    private fun refreshQuranSupportState() {
        viewModelScope.launch {
            val downloaded = quranRepository.downloadedSurahs(uiState.quran.selectedReciter)
            val isDownloaded = quranRepository.isTextDownloaded()
            val surahs = if (isDownloaded) quranRepository.surahMetadata() else emptyList()
            uiState = uiState.copy(
                quran = uiState.quran.copy(
                    selectedReciter = QuranPrefsStore.selectedReciter(app),
                    currentPage = QuranPrefsStore.currentPage(app),
                    dailyGoal = QuranPrefsStore.dailyGoal(app),
                    dailyLog = QuranPrefsStore.getTodayLog(app),
                    stats = QuranPrefsStore.statsLast30Days(app).toUi(),
                    surahs = surahs,
                    download = uiState.quran.download.copy(isDownloaded = isDownloaded),
                    audio = uiState.quran.audio.copy(downloadedSurahs = downloaded),
                ),
            )
        }
    }

    private fun autoMarkMissedPrayers(prayerTimes: PrayerTimeData, now: Date) {
        val prayers = prayerTimes.list()
        val currentLog = prayerLogStore.getTodayLog()
        val updatedQada = qadaStore.load().toMutableMap()
        var changed = false

        prayers.forEachIndexed { index, (prayerKey, prayerTime) ->
            if (currentLog[prayerKey] != null) return@forEachIndexed
            val nextTime = if (index < prayers.lastIndex) {
                prayers[index + 1].second
            } else {
                Calendar.getInstance().apply {
                    time = now
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                }.time
            }

            if (now > nextTime) {
                prayerLogStore.saveTodayStatus(prayerKey, "missed")
                updatedQada[prayerKey] = (updatedQada[prayerKey] ?: 0) + 1
                changed = true
            }
        }

        if (changed) {
            qadaStore.save(updatedQada)
        }
    }

    private fun evaluatePrayerPrompt(
        prayerTimes: PrayerTimeData?,
        log: Map<PrayerKey, String?>,
        now: Date,
    ) {
        if (prayerTimes == null || uiState.activeModal == AppModal.QURAN) return
        val prompts = prayerTimes.list()
        for ((key, time) in prompts) {
            if (log[key] != null) continue
            val diffMinutes = (now.time - time.time) / 60_000
            if (diffMinutes !in 5..120) continue
            if ((snoozeUntil[key] ?: 0L) > now.time) continue
            if (uiState.activeModal == AppModal.NONE || uiState.activeModal == AppModal.PRAYER_CONFIRM) {
                uiState = uiState.copy(
                    activeModal = AppModal.PRAYER_CONFIRM,
                    prayerPrompt = PrayerPromptUi(
                        prayerKey = key,
                        prayerName = key.displayName(),
                        timeLabel = formatPrayerTime(time),
                        quranTargetPage = QuranPrefsStore.getNextPage(app),
                    ),
                )
            }
            return
        }
    }

    private fun bindPlayer() {
        exoPlayer.addListener(
            object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    uiState = uiState.copy(
                        quran = uiState.quran.copy(
                            audio = uiState.quran.audio.copy(isPlaying = isPlaying),
                        ),
                    )
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        val next = uiState.quran.audio.currentSurah + 1
                        if (next in 1..114) {
                            playSurah(next, 0)
                            pushToast("الانتقال إلى السورة التالية.")
                        } else {
                            stopAudio(resetOnly = true)
                            pushToast("ختمة مباركة!")
                        }
                    }
                }
            },
        )
    }

    private fun startAudioMonitor() {
        audioMonitorJob?.cancel()
        audioMonitorJob = viewModelScope.launch {
            while (isActive && uiState.quran.audio.currentSurah > 0) {
                val currentPosition = exoPlayer.currentPosition
                val activeIndex = currentAudioTimestamps.indexOfLast { currentPosition in it.startMs..it.endMs }
                    .takeIf { it >= 0 }
                    ?: currentAudioTimestamps.indexOfLast { currentPosition >= it.startMs }.takeIf { it >= 0 }
                    ?: 0
                if (activeIndex != uiState.quran.audio.currentAyahIndex) {
                    uiState = uiState.copy(
                        quran = uiState.quran.copy(
                            audio = uiState.quran.audio.copy(
                                currentAyahIndex = activeIndex,
                                label = buildAudioLabel(currentAudioMeta, activeIndex),
                            ),
                        ),
                    )
                }
                delay(400)
            }
        }
    }

    private fun stopAudio(resetOnly: Boolean) {
        audioMonitorJob?.cancel()
        audioMonitorJob = null
        exoPlayer.stop()
        uiState = uiState.copy(
            quran = uiState.quran.copy(
                audio = if (resetOnly) {
                    QuranAudioUi(downloadedSurahs = uiState.quran.audio.downloadedSurahs)
                } else {
                    uiState.quran.audio.copy(isPlaying = false)
                },
            ),
        )
        currentAudioTimestamps = emptyList()
        currentAudioMeta = null
    }

    private fun setQuranCurrentPage(page: Int) {
        QuranPrefsStore.setCurrentPage(app, page)
        uiState = uiState.copy(quran = uiState.quran.copy(currentPage = page.coerceIn(1, 604)))
    }

    private fun buildPrayerDots(
        prayerTimes: PrayerTimeData?,
        log: Map<PrayerKey, String?>,
        now: Date,
    ): List<PrayerDotUi> {
        return PrayerKey.entries.map { key ->
            val prayerTime = prayerTimes?.get(key)
            val status = when {
                log[key] == "done" -> PrayerDotStatus.DONE
                log[key] == "missed" -> PrayerDotStatus.MISSED
                prayerTime != null && now < prayerTime -> PrayerDotStatus.UPCOMING
                else -> PrayerDotStatus.PENDING
            }
            PrayerDotUi(
                prayerKey = key,
                label = key.displayName(),
                timeLabel = prayerTime?.let(::formatPrayerTime) ?: "--:--",
                status = status,
            )
        }
    }

    private fun currentHijriDate(date: Date): String {
        val formatter = SimpleDateFormat(
            "EEEE d MMMM y",
            ULocale("ar-SA@calendar=islamic-umalqura"),
        )
        return formatter.format(date)
    }

    private fun formatPrayerTime(date: Date): String = timeFormatter.format(date)

    private fun pushToast(message: String) {
        val toast = ToastMessage(message = message)
        uiState = uiState.copy(currentToast = toast)
        toastJob?.cancel()
        toastJob = viewModelScope.launch {
            delay(3_000L)
            dismissToast(toast.id)
        }
    }

    private fun buildAudioLabel(meta: QuranSurahMeta?, ayahIndex: Int): String {
        val currentAyah = ayahIndex + 1
        return if (meta == null) {
            "آية $currentAyah"
        } else {
            "${meta.name} · آية $currentAyah من ${meta.ayahs}"
        }
    }

    private fun isWithinPostPrayerWindow(prayerTimes: PrayerTimeData, now: Date): Boolean {
        return prayerTimes.list().any { (_, time) ->
            val diffMillis = now.time - time.time
            diffMillis in 0..(30 * 60_000L)
        }
    }

    private fun arabicWeekday(date: Date): String {
        return when (Calendar.getInstance().apply { time = date }.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SATURDAY -> "السبت"
            Calendar.SUNDAY -> "الأحد"
            Calendar.MONDAY -> "الاثنين"
            Calendar.TUESDAY -> "الثلاثاء"
            Calendar.WEDNESDAY -> "الأربعاء"
            Calendar.THURSDAY -> "الخميس"
            Calendar.FRIDAY -> "الجمعة"
            else -> ""
        }
    }

    private fun distanceKm(
        startLat: Double,
        startLon: Double,
        endLat: Double,
        endLon: Double,
    ): Double {
        val earthRadiusKm = 6_371.0
        val dLat = Math.toRadians(endLat - startLat)
        val dLon = Math.toRadians(endLon - startLon)
        val startLatRad = Math.toRadians(startLat)
        val endLatRad = Math.toRadians(endLat)
        val a = kotlin.math.sin(dLat / 2).pow2() +
            kotlin.math.cos(startLatRad) * kotlin.math.cos(endLatRad) * kotlin.math.sin(dLon / 2).pow2()
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return earthRadiusKm * c
    }

    private fun Double.pow2(): Double = this * this

    private fun QuranStatsSnapshot.toUi(): QuranStatsUi {
        return QuranStatsUi(
            streakDays = streakDays,
            totalPagesLast30Days = totalPagesLast30Days,
            daysReadLast30Days = daysReadLast30Days,
            dayBars = dayBars,
        )
    }
}

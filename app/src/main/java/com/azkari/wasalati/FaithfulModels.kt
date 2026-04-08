package com.azkari.wasalati

import java.util.Date

enum class AppModal {
    NONE,
    CITY,
    SETTINGS,
    TRACKER,
    QADA,
    PRAYER_CONFIRM,
    QURAN,
}

enum class HomeTab(
    val openTabKey: String,
    val chipLabel: String,
    val shortLabel: String,
) {
    MORNING("morning", "🌤️ الاستيقاظ والصباح", "الصباح"),
    EVENING("evening", "🌙 المساء", "المساء"),
    PRAYER("prayer", "🤲 بعد الصلاة", "بعد الصلاة"),
    SLEEP("sleep", "💤 النوم والمساء", "النوم"),
    FRIDAY("friday", "🕌 يوم الجمعة", "الجمعة"),
}

enum class PrayerDotStatus {
    PENDING,
    UPCOMING,
    DONE,
    MISSED,
}

enum class SectionPalette {
    WAKING,
    MORNING,
    DUHA,
    EVENING,
    SLEEP,
    TAHAJJUD,
    FRIDAY,
    PRAYER,
    PLAIN,
}

data class AzkarSection(
    val id: String,
    val title: String? = null,
    val subtitle: String? = null,
    val icon: String? = null,
    val palette: SectionPalette = SectionPalette.PLAIN,
    val items: List<AzkarItem>,
)

data class PrayerDotUi(
    val prayerKey: PrayerKey,
    val label: String,
    val timeLabel: String,
    val status: PrayerDotStatus,
)

data class WeeklyPrayerUi(
    val label: String,
    val done: Int,
    val missed: Int,
    val isToday: Boolean,
)

data class ToastMessage(
    val id: Long = System.currentTimeMillis(),
    val message: String,
)

data class PrayerPromptUi(
    val prayerKey: PrayerKey,
    val prayerName: String,
    val timeLabel: String,
    val quranTargetPage: Int? = null,
)

enum class QuranReaderMode {
    DOWNLOAD,
    READER,
    SURAH_LIST,
    SETTINGS,
    ACHIEVEMENTS,
    RECITERS,
}

data class QuranReciter(
    val id: Int,
    val name: String,
)

data class QuranAyah(
    val number: Int,
    val text: String,
    val page: Int,
    val juz: Int,
)

data class QuranSurahMeta(
    val number: Int,
    val name: String,
    val englishName: String,
    val ayahs: Int,
    val type: String,
)

data class QuranPageGroup(
    val surahNum: Int,
    val surahName: String,
    val basmalaText: String? = null,
    val ayahs: List<QuranAyah>,
)

data class QuranDailyLog(
    val pagesRead: List<Int> = emptyList(),
    val completed: Boolean = false,
)

data class QuranDownloadUi(
    val isDownloaded: Boolean = false,
    val isDownloading: Boolean = false,
    val current: Int = 0,
    val total: Int = 114,
    val error: String? = null,
)

data class QuranAudioUi(
    val currentSurah: Int = 0,
    val currentAyahIndex: Int = 0,
    val isPlaying: Boolean = false,
    val label: String = "",
    val downloadedSurahs: Set<Int> = emptySet(),
    val bulkDownloadLabel: String? = null,
    val singleDownloadLabel: String? = null,
)

data class QuranStatsUi(
    val streakDays: Int = 0,
    val totalPagesLast30Days: Int = 0,
    val daysReadLast30Days: Int = 0,
    val dayBars: List<Pair<String, Int>> = emptyList(),
)

data class QuranUiState(
    val mode: QuranReaderMode = QuranReaderMode.DOWNLOAD,
    val currentPage: Int = 1,
    val firstJuzOnPage: Int? = null,
    val pageGroups: List<QuranPageGroup> = emptyList(),
    val surahs: List<QuranSurahMeta> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<QuranSurahMeta> = emptyList(),
    val dailyGoal: Int = 0,
    val dailyLog: QuranDailyLog = QuranDailyLog(),
    val selectedReciter: QuranReciter = QuranDefaults.reciters.first(),
    val reciters: List<QuranReciter> = QuranDefaults.reciters,
    val download: QuranDownloadUi = QuranDownloadUi(),
    val audio: QuranAudioUi = QuranAudioUi(),
    val stats: QuranStatsUi = QuranStatsUi(),
)

data class AppUiState2(
    val isLoading: Boolean = true,
    val activeModal: AppModal = AppModal.NONE,
    val currentCity: City? = null,
    val currentPrayerTimes: PrayerTimeData? = null,
    val prayerSummary: PrayerSummary? = null,
    val prayerDots: List<PrayerDotUi> = emptyList(),
    val hijriDate: String = "",
    val selectedTab: HomeTab = HomeTab.MORNING,
    val manualTabOverride: Boolean = false,
    val homeSections: List<AzkarSection> = emptyList(),
    val suggestion: String? = null,
    val currentToast: ToastMessage? = null,
    val bannerCollapsed: Boolean = false,
    val qada: Map<PrayerKey, Int> = PrayerKey.entries.associateWith { 0 },
    val prayerLog: Map<PrayerKey, String?> = PrayerKey.entries.associateWith { null },
    val todayDoneCount: Int = 0,
    val streakDays: Int = 0,
    val weeklyPrayerUi: List<WeeklyPrayerUi> = emptyList(),
    val calcMethod: String = "Egyptian",
    val reminderSettings: ReminderSettings = ReminderSettings(),
    val cityResults: List<City> = emptyList(),
    val prayerPrompt: PrayerPromptUi? = null,
    val quran: QuranUiState = QuranUiState(),
    val isOffline: Boolean = false,
)

object QuranDefaults {
    val reciters = listOf(
        QuranReciter(6, "محمود خليل الحصري"),
        QuranReciter(7, "مشاري العفاسي"),
        QuranReciter(3, "عبدالرحمن السديس"),
        QuranReciter(8, "محمد صديق المنشاوي"),
        QuranReciter(1, "عبدالباسط عبدالصمد"),
        QuranReciter(10, "سعود الشريم"),
        QuranReciter(4, "أبو بكر الشاطري"),
        QuranReciter(11, "محمد الطبلاوي"),
        QuranReciter(5, "هاني الرفاعي"),
        QuranReciter(2, "عبدالباسط (مجود)"),
    )
}

fun PrayerKey.displayName(): String = when (this) {
    PrayerKey.FAJR -> "الفجر"
    PrayerKey.DHUHR -> "الظهر"
    PrayerKey.ASR -> "العصر"
    PrayerKey.MAGHRIB -> "المغرب"
    PrayerKey.ISHA -> "العشاء"
}

fun PrayerKey.displayIcon(): String = when (this) {
    PrayerKey.FAJR -> "🌤️"
    PrayerKey.DHUHR -> "☀️"
    PrayerKey.ASR -> "🌤️"
    PrayerKey.MAGHRIB -> "🌇"
    PrayerKey.ISHA -> "🌙"
}

data class QuranAudioDescriptor(
    val uri: String,
    val isLocalFile: Boolean,
    val timestamps: List<QuranTimestamp>,
)

data class QuranTimestamp(
    val ayahIndex: Int,
    val startMs: Long,
    val endMs: Long,
)

data class QuranStatsSnapshot(
    val streakDays: Int,
    val totalPagesLast30Days: Int,
    val daysReadLast30Days: Int,
    val dayBars: List<Pair<String, Int>>,
)

data class PrayerTimeWindow(
    val prayerKey: PrayerKey,
    val prayerTime: Date,
)

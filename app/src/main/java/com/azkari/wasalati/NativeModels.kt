package com.azkari.wasalati

import java.util.Date

data class City(
    val name: String,
    val country: String,
    val lat: Double,
    val lon: Double,
    val alt: List<String> = emptyList(),
)

data class AzkarItem(
    val title: String? = null,
    val text: String,
    val count: Int = 1,
    val fadl: String? = null,
    val isQuran: Boolean = false,
)

data class SunnahInfo(
    val pre: String,
    val post: String,
)

enum class HomeCategory(
    val key: String,
    val label: String,
) {
    MORNING("morning", "الصباح"),
    EVENING("evening", "المساء"),
    SLEEP("sleep", "النوم"),
    PRAYER("prayer", "بعد الصلاة"),
    FRIDAY("friday", "الجمعة"),
}

enum class AppSection(
    val label: String,
) {
    HOME("الرئيسية"),
    TRACKER("المتابعة"),
    QADA("القضاء"),
    SETTINGS("الإعدادات"),
}

enum class PrayerKey(
    val key: String,
    val label: String,
) {
    FAJR("fajr", "الفجر"),
    DHUHR("dhuhr", "الظهر"),
    ASR("asr", "العصر"),
    MAGHRIB("maghrib", "المغرب"),
    ISHA("isha", "العشاء"),
}

data class PrayerTimeData(
    val fajr: Date,
    val sunrise: Date,
    val dhuhr: Date,
    val asr: Date,
    val maghrib: Date,
    val isha: Date,
) {
    fun list(): List<Pair<PrayerKey, Date>> = listOf(
        PrayerKey.FAJR to fajr,
        PrayerKey.DHUHR to dhuhr,
        PrayerKey.ASR to asr,
        PrayerKey.MAGHRIB to maghrib,
        PrayerKey.ISHA to isha,
    )

    fun get(prayerKey: PrayerKey): Date = when (prayerKey) {
        PrayerKey.FAJR -> fajr
        PrayerKey.DHUHR -> dhuhr
        PrayerKey.ASR -> asr
        PrayerKey.MAGHRIB -> maghrib
        PrayerKey.ISHA -> isha
    }
}

data class PrayerSummary(
    val nextPrayer: PrayerKey,
    val nextPrayerTime: Date,
    val countdownMillis: Long,
    val sunnahInfo: SunnahInfo,
)

data class AppUiState(
    val isLoading: Boolean = true,
    val currentSection: AppSection = AppSection.HOME,
    val selectedCategory: HomeCategory = HomeCategory.MORNING,
    val manualCategoryOverride: Boolean = false,
    val currentCity: City? = null,
    val currentPrayerTimes: PrayerTimeData? = null,
    val prayerSummary: PrayerSummary? = null,
    val hijriDate: String = "",
    val todayDoneCount: Int = 0,
    val streakDays: Int = 0,
    val suggestion: String? = null,
    val categories: Map<HomeCategory, List<AzkarItem>> = emptyMap(),
    val allCities: List<City> = emptyList(),
    val popularCities: List<City> = emptyList(),
    val reminderSettings: ReminderSettings = ReminderSettings(),
    val calcMethod: String = "Egyptian",
    val qada: Map<PrayerKey, Int> = PrayerKey.entries.associateWith { 0 },
    val prayerLog: Map<PrayerKey, String?> = PrayerKey.entries.associateWith { null },
)

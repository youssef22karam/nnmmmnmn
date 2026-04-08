package com.azkari.wasalati

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.Normalizer
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class WebParityDataRepository(
    private val context: Context,
) {
    private val azkarRoot by lazy { JSONObject(readAsset("native/azkarData.json")) }
    private val allCitiesCache by lazy { parseCities(JSONArray(readAsset("native/allCities.json"))) }
    private val popularCitiesCache by lazy { parseCities(JSONArray(readAsset("native/popularCities.json"))) }
    private val sunanCache by lazy { loadSunanInternal() }

    fun allCities(): List<City> = allCitiesCache

    fun popularCities(): List<City> = popularCitiesCache

    fun sunan(): Map<String, SunnahInfo> = sunanCache

    fun searchCities(query: String, topN: Int = 20): List<City> {
        val normalizedQuery = normalizeArabic(query)
        if (normalizedQuery.isBlank()) return popularCities()

        return allCities()
            .map { city -> city to scoreCity(city, normalizedQuery) }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .take(topN)
            .map { it.first }
    }

    fun resolveNearestCity(latitude: Double, longitude: Double): City? {
        return allCities().minByOrNull { distanceKm(latitude, longitude, it.lat, it.lon) }
    }

    fun buildSections(
        tab: HomeTab,
        prayerTimes: PrayerTimeData?,
        now: Date = Date(),
    ): List<AzkarSection> {
        return when (tab) {
            HomeTab.MORNING -> buildMorningSections(prayerTimes, now)
            HomeTab.EVENING -> listOf(AzkarSection(id = "evening", items = loadGroup("evening")))
            HomeTab.PRAYER -> listOf(AzkarSection(id = "prayer", items = loadGroup("prayer")))
            HomeTab.SLEEP -> buildSleepSections(prayerTimes, now)
            HomeTab.FRIDAY -> listOf(
                AzkarSection(
                    id = "friday",
                    title = "أذكار يوم الجمعة",
                    subtitle = "يوم الجمعة خير يوم طلعت عليه الشمس",
                    icon = "🕌",
                    palette = SectionPalette.FRIDAY,
                    items = loadGroup("friday"),
                ),
            )
        }
    }

    fun loadGroup(name: String): List<AzkarItem> = parseAzkarArray(azkarRoot.optJSONArray(name))

    fun normalizeArabic(input: String): String {
        val value = Normalizer.normalize(input, Normalizer.Form.NFKC)
        return value
            .replace(Regex("[\\u064B-\\u065F\\u0670\\u0640]"), "")
            .replace(Regex("[أإآٱ]"), "ا")
            .replace("ة", "ه")
            .replace("ى", "ي")
            .replace("ؤ", "و")
            .replace("ئ", "ي")
            .replace(Regex("^ال\\s*"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
            .lowercase(Locale.ROOT)
    }

    private fun buildMorningSections(prayerTimes: PrayerTimeData?, now: Date): List<AzkarSection> {
        val sections = mutableListOf(
            AzkarSection(
                id = "waking",
                title = "أذكار الاستيقاظ",
                subtitle = "عند الإفاقة من النوم",
                icon = "🌅",
                palette = SectionPalette.WAKING,
                items = loadGroup("waking"),
            ),
            AzkarSection(
                id = "morning",
                title = "أذكار الصباح",
                subtitle = "تقرأ بعد صلاة الفجر وحتى الضحى",
                icon = "☀️",
                palette = SectionPalette.MORNING,
                items = loadGroup("morning"),
            ),
        )

        if (prayerTimes != null) {
            val duhaStart = Date(prayerTimes.sunrise.time + 90 * 60_000L)
            val duhaEnd = Date(prayerTimes.dhuhr.time - 30 * 60_000L)
            if (now >= duhaStart && now < duhaEnd) {
                sections += AzkarSection(
                    id = "duha",
                    title = "وقت الضحى",
                    subtitle = "الآن وقت صلاة الضحى وأذكارها",
                    icon = "🌞",
                    palette = SectionPalette.DUHA,
                    items = loadGroup("duha"),
                )
            }
        }

        if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
            sections += AzkarSection(
                id = "friday-extra",
                title = "أذكار يوم الجمعة",
                subtitle = "خاصة بيوم الجمعة المبارك",
                icon = "🕌",
                palette = SectionPalette.FRIDAY,
                items = loadGroup("friday"),
            )
        }

        return sections
    }

    private fun buildSleepSections(prayerTimes: PrayerTimeData?, now: Date): List<AzkarSection> {
        if (prayerTimes == null || now < prayerTimes.isha) {
            return listOf(
                AzkarSection(
                    id = "sleep",
                    title = "أذكار النوم",
                    subtitle = "تقرأ عند الخلود إلى النوم",
                    icon = "💤",
                    palette = SectionPalette.SLEEP,
                    items = loadGroup("sleep"),
                ),
            )
        }

        val sections = mutableListOf(
            AzkarSection(
                id = "evening-before-sleep",
                title = "أذكار المساء",
                subtitle = "تقرأ من بعد العصر حتى الغروب",
                icon = "🌙",
                palette = SectionPalette.EVENING,
                items = loadGroup("evening"),
            ),
            AzkarSection(
                id = "sleep",
                title = "أذكار النوم",
                subtitle = "تقرأ عند الخلود إلى النوم",
                icon = "💤",
                palette = SectionPalette.SLEEP,
                items = loadGroup("sleep"),
            ),
        )

        val nightDuration = prayerTimes.fajr.time - prayerTimes.isha.time
        val lastThirdStart = Date(prayerTimes.isha.time + (nightDuration * 2 / 3))
        if (now >= lastThirdStart) {
            sections += AzkarSection(
                id = "tahajjud",
                title = "قيام الليل والتهجد",
                subtitle = "الثلث الأخير من الليل — أفضل أوقات الدعاء",
                icon = "🌌",
                palette = SectionPalette.TAHAJJUD,
                items = loadGroup("tahajjud"),
            )
        }

        return sections
    }

    private fun loadSunanInternal(): Map<String, SunnahInfo> {
        val root = JSONObject(readAsset("native/sunanData.json"))
        val result = mutableMapOf<String, SunnahInfo>()
        root.keys().forEach { key ->
            val item = root.getJSONObject(key)
            result[key] = SunnahInfo(
                pre = item.optString("pre"),
                post = item.optString("post"),
            )
        }
        return result
    }

    private fun parseAzkarArray(array: JSONArray?): List<AzkarItem> {
        if (array == null) return emptyList()
        return List(array.length()) { index ->
            val item = array.getJSONObject(index)
            AzkarItem(
                title = item.optString("title").takeIf { it.isNotBlank() },
                text = item.optString("text"),
                count = item.optInt("count", 1),
                fadl = item.optString("fadl").takeIf { it.isNotBlank() },
                isQuran = item.optBoolean("isQuran", false),
            )
        }
    }

    private fun parseCities(array: JSONArray): List<City> {
        return List(array.length()) { index ->
            val item = array.getJSONObject(index)
            val altArray = item.optJSONArray("alt")
            val alt = if (altArray == null) {
                emptyList()
            } else {
                List(altArray.length()) { altIndex -> altArray.optString(altIndex) }
            }
            City(
                name = item.optString("name"),
                country = item.optString("country"),
                lat = item.optDouble("lat"),
                lon = item.optDouble("lon"),
                alt = alt,
            )
        }
    }

    private fun scoreCity(city: City, query: String): Int {
        val normalizedName = normalizeArabic(city.name)
        if (normalizedName == query) return 100
        if (normalizedName.startsWith(query)) return 90 - abs(normalizedName.length - query.length)
        if (normalizedName.contains(query)) return 75

        city.alt.forEach { alt ->
            val normalizedAlt = normalizeArabic(alt)
            if (normalizedAlt == query) return 95
            if (normalizedAlt.startsWith(query)) return 85
            if (normalizedAlt.contains(query)) return 70
        }

        if (query.length >= 3) {
            var overlap = 0
            for (index in 0 until query.length - 1) {
                if (normalizedName.contains(query.substring(index, index + 2))) overlap++
            }
            if (overlap > 1) return minOf(50, overlap * 15)
        }

        return 0
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
        val a = sin(dLat / 2).pow2() +
            cos(startLatRad) * cos(endLatRad) * sin(dLon / 2).pow2()
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusKm * c
    }

    private fun Double.pow2(): Double = this * this

    private fun readAsset(path: String): String {
        return context.assets.open(path).bufferedReader(Charsets.UTF_8).use { it.readText() }
    }
}

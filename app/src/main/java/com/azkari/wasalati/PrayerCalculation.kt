package com.azkari.wasalati

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.CalculationParameters
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.PrayerAdjustments
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import java.util.Calendar
import java.util.Date

object PrayerCalculation {
    fun calculatePrayerTimes(
        latitude: Double,
        longitude: Double,
        method: String,
        date: Date = Date(),
    ): PrayerTimeData {
        val prayerTimes = PrayerTimes(
            Coordinates(latitude, longitude),
            DateComponents.from(date),
            calculationParameters(method),
        )

        return PrayerTimeData(
            fajr = prayerTimes.fajr,
            sunrise = prayerTimes.sunrise,
            dhuhr = prayerTimes.dhuhr,
            asr = prayerTimes.asr,
            maghrib = prayerTimes.maghrib,
            isha = prayerTimes.isha,
        )
    }

    fun calculationParameters(method: String): CalculationParameters {
        val params = when (method) {
            "Egyptian" -> CalculationMethod.EGYPTIAN.getParameters()
            "UmmAlQura" -> CalculationMethod.UMM_AL_QURA.getParameters()
            "Kuwait" -> CalculationMethod.KUWAIT.getParameters()
            "Qatar" -> CalculationMethod.QATAR.getParameters()
            "Dubai" -> CalculationMethod.DUBAI.getParameters()
            "MuslimWorldLeague" -> CalculationMethod.MUSLIM_WORLD_LEAGUE.getParameters()
            "NorthAmerica" -> CalculationMethod.NORTH_AMERICA.getParameters()
            "Singapore" -> CalculationMethod.SINGAPORE.getParameters()
            "Turkey" -> CalculationParameters(18.0, 17.0, CalculationMethod.OTHER).apply {
                methodAdjustments = PrayerAdjustments().apply {
                    sunrise = -7
                    dhuhr = 5
                    asr = 4
                    maghrib = 7
                }
            }
            "Tehran" -> CalculationParameters(17.7, 14.0, CalculationMethod.OTHER)
            "Karachi" -> CalculationMethod.KARACHI.getParameters()
            else -> CalculationMethod.EGYPTIAN.getParameters()
        }
        params.madhab = Madhab.SHAFI
        return params
    }

    fun buildSummary(
        prayerTimes: PrayerTimeData,
        city: City,
        method: String,
        sunan: Map<String, SunnahInfo> = defaultSunan(),
        now: Date = Date(),
    ): PrayerSummary {
        val currentMillis = now.time
        val upcoming = prayerTimes.list().firstOrNull { it.second.time > currentMillis }
            ?: run {
                val tomorrow = Calendar.getInstance().apply {
                    time = now
                    add(Calendar.DAY_OF_YEAR, 1)
                }.time
                PrayerKey.FAJR to calculatePrayerTimes(city.lat, city.lon, method, tomorrow).fajr
            }

        val sunnahKey = when (upcoming.first) {
            PrayerKey.FAJR -> "الفجر"
            PrayerKey.DHUHR -> if (currentMillis < prayerTimes.fajr.time + 45 * 60_000L) "الفجر" else "الظهر"
            PrayerKey.ASR -> if (currentMillis < prayerTimes.dhuhr.time + 45 * 60_000L) "الظهر" else "العصر"
            PrayerKey.MAGHRIB -> if (currentMillis < prayerTimes.asr.time + 45 * 60_000L) "العصر" else "المغرب"
            PrayerKey.ISHA -> if (currentMillis < prayerTimes.maghrib.time + 45 * 60_000L) "المغرب" else "العشاء"
        }

        return PrayerSummary(
            nextPrayer = upcoming.first,
            nextPrayerTime = upcoming.second,
            countdownMillis = upcoming.second.time - currentMillis,
            sunnahInfo = sunan[sunnahKey] ?: SunnahInfo("-", "-"),
        )
    }

    fun defaultSunan(): Map<String, SunnahInfo> = mapOf(
        "الفجر" to SunnahInfo("ركعتان خفيفتان (سنة مؤكدة)", "لا يوجد (وقت كراهة)"),
        "الظهر" to SunnahInfo("أربع ركعات (2+2)", "ركعتان (سنة مؤكدة)"),
        "العصر" to SunnahInfo("أربع ركعات (خفيفتان)", "لا يوجد (وقت كراهة)"),
        "المغرب" to SunnahInfo("ركعتان (خفيفتان)", "ركعتان (سنة مؤكدة)"),
        "العشاء" to SunnahInfo("ركعتان (بين الأذان والإقامة)", "ركعتان + الوتر"),
    )
}

package com.azkari.wasalati

import android.content.Context
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PrayerLogStore(
    context: Context,
) {
    private val prefs = context.getSharedPreferences("prayer_log_store", Context.MODE_PRIVATE)
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun getTodayLog(): Map<PrayerKey, String?> = getLogForDate(Date())

    fun getLogForDate(date: Date): Map<PrayerKey, String?> {
        val raw = prefs.getString("prayer_log_${dateKey(date)}", null)
        if (raw.isNullOrBlank()) return emptyPrayerLog()

        return try {
            val root = JSONObject(raw)
            PrayerKey.entries.associateWith { key ->
                if (!root.has(key.key) || root.isNull(key.key)) null else root.optString(key.key)
            }
        } catch (_: Exception) {
            emptyPrayerLog()
        }
    }

    fun saveTodayStatus(prayerKey: PrayerKey, status: String?) {
        saveStatus(Date(), prayerKey, status)
    }

    fun doneCountForDate(date: Date): Int = getLogForDate(date).count { it.value == "done" }

    fun missedCountForDate(date: Date): Int = getLogForDate(date).count { it.value == "missed" }

    fun todayDoneCount(): Int = doneCountForDate(Date())

    fun streakDays(): Int {
        var streak = 0
        val today = Calendar.getInstance()
        repeat(365) { index ->
            val log = getLogForDate(today.time)
            val done = PrayerKey.entries.count { log[it] == "done" }
            val qualifies = if (index == 0) done > 0 else done == PrayerKey.entries.size
            if (qualifies) {
                streak++
                today.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                return streak
            }
        }
        return streak
    }

    fun lastSevenDaysStats(): List<DailyPrayerStat> {
        val calendar = Calendar.getInstance()
        return buildList {
            repeat(7) { index ->
                val date = calendar.time
                add(
                    DailyPrayerStat(
                        date = date,
                        done = doneCountForDate(date),
                        missed = missedCountForDate(date),
                        isToday = index == 0,
                    ),
                )
                calendar.add(Calendar.DAY_OF_YEAR, -1)
            }
        }
    }

    private fun saveStatus(date: Date, prayerKey: PrayerKey, status: String?) {
        val current = JSONObject()
        getLogForDate(date).forEach { (key, value) ->
            if (value == null) current.put(key.key, JSONObject.NULL) else current.put(key.key, value)
        }
        if (status == null) current.put(prayerKey.key, JSONObject.NULL) else current.put(prayerKey.key, status)
        prefs.edit().putString("prayer_log_${dateKey(date)}", current.toString()).apply()
    }

    private fun emptyPrayerLog(): Map<PrayerKey, String?> = PrayerKey.entries.associateWith { null }

    private fun dateKey(date: Date): String = dateFormatter.format(date)
}

data class DailyPrayerStat(
    val date: Date,
    val done: Int,
    val missed: Int,
    val isToday: Boolean,
)

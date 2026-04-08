package com.azkari.wasalati

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object QuranPrefsStore {
    private const val PREFS_NAME = "quran_progress_store"
    private const val KEY_CURRENT_PAGE = "current_page"
    private const val KEY_DAILY_GOAL = "daily_goal"
    private const val KEY_RECITER_ID = "reciter_id"
    private const val KEY_ALL_FINISHED_PAGES = "all_finished_pages"
    private const val KEY_LAST_FINISHED_PAGE = "last_finished_page"

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun currentPage(context: Context): Int = prefs(context).getInt(KEY_CURRENT_PAGE, 1).coerceIn(1, 604)

    fun setCurrentPage(context: Context, page: Int) {
        prefs(context).edit().putInt(KEY_CURRENT_PAGE, page.coerceIn(1, 604)).apply()
    }

    fun dailyGoal(context: Context): Int = prefs(context).getInt(KEY_DAILY_GOAL, 0).coerceIn(0, 604)

    fun setDailyGoal(context: Context, pages: Int): QuranDailyLog {
        prefs(context).edit().putInt(KEY_DAILY_GOAL, pages.coerceIn(0, 604)).apply()
        val today = getTodayLog(context)
        val updated = today.copy(
            completed = pages > 0 && today.pagesRead.size >= pages,
        )
        saveTodayLog(context, updated)
        return updated
    }

    fun selectedReciter(context: Context): QuranReciter {
        val id = prefs(context).getInt(KEY_RECITER_ID, QuranDefaults.reciters.first().id)
        return QuranDefaults.reciters.firstOrNull { it.id == id } ?: QuranDefaults.reciters.first()
    }

    fun setSelectedReciter(context: Context, reciterId: Int) {
        prefs(context).edit().putInt(KEY_RECITER_ID, reciterId).apply()
    }

    fun getTodayLog(context: Context): QuranDailyLog = getDailyLog(context, Date())

    fun markPageRead(context: Context, page: Int): QuranDailyLog {
        val log = getTodayLog(context)
        val pages = log.pagesRead.toMutableList()
        if (!pages.contains(page)) {
            pages += page
        }
        val updated = QuranDailyLog(
            pagesRead = pages.sorted(),
            completed = dailyGoal(context) > 0 && pages.size >= dailyGoal(context),
        )
        saveTodayLog(context, updated)
        saveFinishedPage(context, page)
        prefs(context).edit().putInt(KEY_LAST_FINISHED_PAGE, page).apply()
        return updated
    }

    fun allFinishedPages(context: Context): List<Int> {
        val raw = prefs(context).getString(KEY_ALL_FINISHED_PAGES, "[]").orEmpty()
        return try {
            val array = JSONArray(raw)
            List(array.length()) { index -> array.optInt(index) }
                .filter { it in 1..604 }
                .distinct()
                .sorted()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun getNextPage(context: Context): Int {
        val readSet = allFinishedPages(context).toSet()
        if (readSet.isEmpty()) return 1
        for (page in 1..604) {
            if (!readSet.contains(page)) return page
        }
        return 1
    }

    fun streakDays(context: Context): Int {
        var streak = 0
        val today = Calendar.getInstance()
        repeat(365) { index ->
            val log = getDailyLog(context, today.time)
            val count = log.pagesRead.size
            val qualifies = if (index == 0) count > 0 else count > 0
            if (qualifies) {
                streak++
                today.add(Calendar.DAY_OF_YEAR, -1)
            } else if (index > 0) {
                return streak
            } else {
                return 0
            }
        }
        return streak
    }

    fun statsLast30Days(context: Context): QuranStatsSnapshot {
        var totalPages = 0
        var daysRead = 0
        val bars = mutableListOf<Pair<String, Int>>()
        val today = Calendar.getInstance()

        for (index in 29 downTo 0) {
            val calendar = Calendar.getInstance().apply {
                time = today.time
                add(Calendar.DAY_OF_YEAR, -index)
            }
            val log = getDailyLog(context, calendar.time)
            val count = log.pagesRead.size
            if (count > 0) {
                totalPages += count
                daysRead++
            }
            bars += dayLabel(calendar.time) to count
        }

        return QuranStatsSnapshot(
            streakDays = streakDays(context),
            totalPagesLast30Days = totalPages,
            daysReadLast30Days = daysRead,
            dayBars = bars,
        )
    }

    fun saveTodayLog(context: Context, log: QuranDailyLog) {
        saveDailyLog(context, Date(), log)
    }

    private fun saveFinishedPage(context: Context, page: Int) {
        val pages = allFinishedPages(context).toMutableSet()
        pages += page.coerceIn(1, 604)
        val array = JSONArray()
        pages.sorted().forEach(array::put)
        prefs(context).edit().putString(KEY_ALL_FINISHED_PAGES, array.toString()).apply()
    }

    private fun getDailyLog(context: Context, date: Date): QuranDailyLog {
        val raw = prefs(context).getString(dayKey(date), null) ?: return QuranDailyLog()
        return try {
            val root = JSONObject(raw)
            val pageArray = root.optJSONArray("pagesRead")
            val pages = if (pageArray == null) {
                emptyList()
            } else {
                List(pageArray.length()) { index -> pageArray.optInt(index) }
                    .filter { it in 1..604 }
                    .distinct()
                    .sorted()
            }
            QuranDailyLog(
                pagesRead = pages,
                completed = root.optBoolean("completed", false),
            )
        } catch (_: Exception) {
            QuranDailyLog()
        }
    }

    private fun saveDailyLog(context: Context, date: Date, log: QuranDailyLog) {
        val root = JSONObject()
        val pages = JSONArray()
        log.pagesRead.sorted().forEach(pages::put)
        root.put("pagesRead", pages)
        root.put("completed", log.completed)
        prefs(context).edit().putString(dayKey(date), root.toString()).apply()
    }

    private fun dayKey(date: Date): String {
        return "quranDaily_${SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)}"
    }

    private fun dayLabel(date: Date): String {
        return SimpleDateFormat("dd", Locale.US).format(date)
    }
}

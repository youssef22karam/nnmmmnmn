package com.azkari.wasalati

import android.content.Context
import org.json.JSONObject

class QadaStore(
    context: Context,
) {
    private val prefs = context.getSharedPreferences("qada_store", Context.MODE_PRIVATE)
    private val key = "qada_counts"

    fun load(): Map<PrayerKey, Int> {
        val raw = prefs.getString(key, null)
        if (raw.isNullOrBlank()) return PrayerKey.entries.associateWith { 0 }

        return try {
            val root = JSONObject(raw)
            PrayerKey.entries.associateWith { prayerKey -> root.optInt(prayerKey.key, 0) }
        } catch (_: Exception) {
            PrayerKey.entries.associateWith { 0 }
        }
    }

    fun save(counts: Map<PrayerKey, Int>) {
        val root = JSONObject()
        counts.forEach { (prayerKey, count) -> root.put(prayerKey.key, count) }
        prefs.edit().putString(key, root.toString()).apply()
    }
}

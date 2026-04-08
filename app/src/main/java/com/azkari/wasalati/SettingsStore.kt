package com.azkari.wasalati

import android.content.Context
import org.json.JSONObject

object SettingsStore {
    private const val PREFS_NAME = "azkari_wasalati_prefs"
    private const val KEY_SETTINGS_JSON = "settings_json"

    fun load(context: Context): AppSettings {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_SETTINGS_JSON, null) ?: return AppSettings()
        return parseSettings(raw)
    }

    fun save(context: Context, settings: AppSettings) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SETTINGS_JSON, toJson(settings).toString()).apply()
    }

    fun saveFromWebPayload(context: Context, json: String): AppSettings {
        val settings = parsePayload(json)
        save(context, settings)
        return settings
    }

    private fun parsePayload(raw: String): AppSettings {
        return try {
            val root = JSONObject(raw)
            val city = root.optJSONObject("city")
            val reminders = root.optJSONObject("reminders")

            AppSettings(
                cityName = city?.optString("name")?.takeIf { !it.isNullOrBlank() },
                countryName = city?.optString("country")?.takeIf { !it.isNullOrBlank() },
                latitude = city?.optNullableDouble("lat"),
                longitude = city?.optNullableDouble("lon"),
                calcMethod = root.optString("calcMethod").ifBlank { "Egyptian" },
                reminders = ReminderSettings(
                    prayerNotificationsEnabled = reminders?.optBoolean("prayerNotificationsEnabled", true) ?: true,
                    prayerReminderOffsetMinutes = (reminders?.optInt("prayerReminderOffsetMinutes", 0)
                        ?: 0).coerceIn(0, 30),
                    morningAzkarEnabled = reminders?.optBoolean("morningAzkarEnabled", true) ?: true,
                    eveningAzkarEnabled = reminders?.optBoolean("eveningAzkarEnabled", true) ?: true,
                    sleepAzkarEnabled = reminders?.optBoolean("sleepAzkarEnabled", true) ?: true,
                    fridayKahfEnabled = reminders?.optBoolean("fridayKahfEnabled", true) ?: true,
                ),
            )
        } catch (_: Exception) {
            AppSettings()
        }
    }

    private fun parseSettings(raw: String): AppSettings {
        return try {
            val root = JSONObject(raw)
            val reminders = root.optJSONObject("reminders")

            AppSettings(
                cityName = root.optString("cityName").takeIf { it.isNotBlank() },
                countryName = root.optString("countryName").takeIf { it.isNotBlank() },
                latitude = root.optNullableDouble("latitude"),
                longitude = root.optNullableDouble("longitude"),
                calcMethod = root.optString("calcMethod").ifBlank { "Egyptian" },
                reminders = ReminderSettings(
                    prayerNotificationsEnabled = reminders?.optBoolean("prayerNotificationsEnabled", true) ?: true,
                    prayerReminderOffsetMinutes = (reminders?.optInt("prayerReminderOffsetMinutes", 0)
                        ?: 0).coerceIn(0, 30),
                    morningAzkarEnabled = reminders?.optBoolean("morningAzkarEnabled", true) ?: true,
                    eveningAzkarEnabled = reminders?.optBoolean("eveningAzkarEnabled", true) ?: true,
                    sleepAzkarEnabled = reminders?.optBoolean("sleepAzkarEnabled", true) ?: true,
                    fridayKahfEnabled = reminders?.optBoolean("fridayKahfEnabled", true) ?: true,
                ),
            )
        } catch (_: Exception) {
            AppSettings()
        }
    }

    private fun toJson(settings: AppSettings): JSONObject {
        return JSONObject()
            .put("cityName", settings.cityName ?: "")
            .put("countryName", settings.countryName ?: "")
            .put("latitude", settings.latitude)
            .put("longitude", settings.longitude)
            .put("calcMethod", settings.calcMethod)
            .put(
                "reminders",
                JSONObject()
                    .put("prayerNotificationsEnabled", settings.reminders.prayerNotificationsEnabled)
                    .put("prayerReminderOffsetMinutes", settings.reminders.prayerReminderOffsetMinutes)
                    .put("morningAzkarEnabled", settings.reminders.morningAzkarEnabled)
                    .put("eveningAzkarEnabled", settings.reminders.eveningAzkarEnabled)
                    .put("sleepAzkarEnabled", settings.reminders.sleepAzkarEnabled)
                    .put("fridayKahfEnabled", settings.reminders.fridayKahfEnabled),
            )
    }

    private fun JSONObject.optNullableDouble(key: String): Double? {
        return if (has(key) && !isNull(key)) optDouble(key) else null
    }
}


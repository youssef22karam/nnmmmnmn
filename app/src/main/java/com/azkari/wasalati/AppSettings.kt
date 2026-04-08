package com.azkari.wasalati

data class AppSettings(
    val cityName: String? = null,
    val countryName: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val calcMethod: String = "Egyptian",
    val reminders: ReminderSettings = ReminderSettings(),
) {
    val hasLocation: Boolean
        get() = latitude != null && longitude != null

    fun locationLabel(): String {
        val parts = listOfNotNull(
            cityName?.takeIf { it.isNotBlank() },
            countryName?.takeIf { it.isNotBlank() },
        )
        return if (parts.isNotEmpty()) parts.joinToString("، ") else "موقعك"
    }
}

data class ReminderSettings(
    val prayerNotificationsEnabled: Boolean = true,
    val prayerReminderOffsetMinutes: Int = 0,
    val morningAzkarEnabled: Boolean = true,
    val eveningAzkarEnabled: Boolean = true,
    val sleepAzkarEnabled: Boolean = true,
    val fridayKahfEnabled: Boolean = true,
)

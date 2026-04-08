package com.azkari.wasalati

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object PrayerScheduler {
    const val ACTION_FIRE_REMINDER = "com.azkari.wasalati.action.FIRE_REMINDER"
    const val ACTION_RESCHEDULE = "com.azkari.wasalati.action.RESCHEDULE"
    const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    const val EXTRA_CHANNEL_ID = "extra_channel_id"
    const val EXTRA_TITLE = "extra_title"
    const val EXTRA_BODY = "extra_body"
    const val EXTRA_OPEN_TAB = "extra_open_tab"

    private const val REQUEST_FAJR = 1101
    private const val REQUEST_DHUHR = 1102
    private const val REQUEST_ASR = 1103
    private const val REQUEST_MAGHRIB = 1104
    private const val REQUEST_ISHA = 1105
    private const val REQUEST_MORNING = 2101
    private const val REQUEST_EVENING = 2102
    private const val REQUEST_SLEEP = 2103
    private const val REQUEST_FRIDAY = 2104
    private const val REQUEST_REFRESH = 9100

    fun scheduleAll(context: Context) {
        NotificationHelper.createChannels(context)
        cancelAll(context)

        val settings = SettingsStore.load(context)
        if (!settings.hasLocation) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = System.currentTimeMillis()

        buildReminderEvents(settings)
            .filter { it.triggerAtMillis > now + 5_000L }
            .forEach { scheduleReminder(context, alarmManager, it) }

        scheduleDailyRefresh(context, alarmManager)
    }

    fun cancelAll(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val requestCodes = listOf(
            REQUEST_FAJR,
            REQUEST_DHUHR,
            REQUEST_ASR,
            REQUEST_MAGHRIB,
            REQUEST_ISHA,
            REQUEST_MORNING,
            REQUEST_EVENING,
            REQUEST_SLEEP,
            REQUEST_FRIDAY,
            REQUEST_REFRESH,
        )

        requestCodes.forEach { requestCode ->
            val pendingIntent = if (requestCode == REQUEST_REFRESH) {
                refreshPendingIntent(context, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            } else {
                reminderPendingIntent(
                    context = context,
                    requestCode = requestCode,
                    channelId = "",
                    title = "",
                    body = "",
                    openTab = "prayer",
                    flags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                )
            }
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun canScheduleExactAlarms(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return alarmManager.canScheduleExactAlarms()
    }

    private fun buildReminderEvents(settings: AppSettings): List<ReminderEvent> {
        val prayerTimes = PrayerCalculation.calculatePrayerTimes(
            latitude = settings.latitude!!,
            longitude = settings.longitude!!,
            method = settings.calcMethod,
        )
        val now = System.currentTimeMillis()
        val offsetMinutes = settings.reminders.prayerReminderOffsetMinutes.coerceIn(0, 30)
        val offsetMillis = offsetMinutes * 60_000L
        val location = settings.locationLabel()
        val events = mutableListOf<ReminderEvent>()

        if (settings.reminders.prayerNotificationsEnabled) {
            val prayers = listOf(
                Triple("الفجر", REQUEST_FAJR, prayerTimes.fajr),
                Triple("الظهر", REQUEST_DHUHR, prayerTimes.dhuhr),
                Triple("العصر", REQUEST_ASR, prayerTimes.asr),
                Triple("المغرب", REQUEST_MAGHRIB, prayerTimes.maghrib),
                Triple("العشاء", REQUEST_ISHA, prayerTimes.isha),
            )

            prayers.forEach { (name, requestCode, prayerTime) ->
                val prayerAt = prayerTime.time
                var triggerAt = prayerAt - offsetMillis
                var title = if (offsetMillis > 0L) "اقتربت صلاة $name" else "حان وقت صلاة $name"
                var body = if (offsetMillis > 0L) {
                    "باقي $offsetMinutes دقيقة على صلاة $name في $location."
                } else {
                    "دخل الآن وقت صلاة $name في $location."
                }

                if (triggerAt <= now && prayerAt > now) {
                    triggerAt = prayerAt
                    title = "حان وقت صلاة $name"
                    body = "دخل الآن وقت صلاة $name في $location."
                }

                if (triggerAt > now) {
                    events += ReminderEvent(
                        requestCode = requestCode,
                        triggerAtMillis = triggerAt,
                        channelId = NotificationHelper.CHANNEL_PRAYERS,
                        title = title,
                        body = body,
                        openTab = "prayer",
                    )
                }
            }
        }

        if (settings.reminders.morningAzkarEnabled) {
            val triggerAt = prayerTimes.fajr.time + 20 * 60_000L
            if (triggerAt > now) {
                events += ReminderEvent(
                    requestCode = REQUEST_MORNING,
                    triggerAtMillis = triggerAt,
                    channelId = NotificationHelper.CHANNEL_AZKAR,
                    title = "أذكار الصباح",
                    body = "ابدأ يومك بذكر الله. افتح التطبيق لورد الصباح بعد الفجر.",
                    openTab = "morning",
                )
            }
        }

        if (settings.reminders.eveningAzkarEnabled) {
            val triggerAt = prayerTimes.asr.time + 20 * 60_000L
            if (triggerAt > now) {
                events += ReminderEvent(
                    requestCode = REQUEST_EVENING,
                    triggerAtMillis = triggerAt,
                    channelId = NotificationHelper.CHANNEL_AZKAR,
                    title = "أذكار المساء",
                    body = "هذا وقت مناسب لورد المساء. افتح التطبيق وخذ دقائق هادئة مع الأذكار.",
                    openTab = "evening",
                )
            }
        }

        if (settings.reminders.sleepAzkarEnabled) {
            val triggerAt = prayerTimes.isha.time + 30 * 60_000L
            if (triggerAt > now) {
                events += ReminderEvent(
                    requestCode = REQUEST_SLEEP,
                    triggerAtMillis = triggerAt,
                    channelId = NotificationHelper.CHANNEL_AZKAR,
                    title = "أذكار النوم",
                    body = "قبل أن تنام، افتح التطبيق واقرأ أذكار النوم بهدوء.",
                    openTab = "sleep",
                )
            }
        }

        if (settings.reminders.fridayKahfEnabled) {
            val fridayReminder = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 9)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (fridayReminder.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY &&
                fridayReminder.timeInMillis > now
            ) {
                events += ReminderEvent(
                    requestCode = REQUEST_FRIDAY,
                    triggerAtMillis = fridayReminder.timeInMillis,
                    channelId = NotificationHelper.CHANNEL_AZKAR,
                    title = "جمعة مباركة",
                    body = "لا تنس سورة الكهف وكثرة الصلاة على النبي ﷺ اليوم.",
                    openTab = "friday",
                )
            }
        }

        return events
    }

    private fun scheduleReminder(context: Context, alarmManager: AlarmManager, event: ReminderEvent) {
        val pendingIntent = reminderPendingIntent(
            context = context,
            requestCode = event.requestCode,
            channelId = event.channelId,
            title = event.title,
            body = event.body,
            openTab = event.openTab,
            flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        if (canScheduleExactAlarms(context)) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                event.triggerAtMillis,
                pendingIntent,
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                event.triggerAtMillis,
                pendingIntent,
            )
        }
    }

    private fun scheduleDailyRefresh(context: Context, alarmManager: AlarmManager) {
        val tomorrow = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 5)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val pendingIntent = refreshPendingIntent(
            context,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        if (canScheduleExactAlarms(context)) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                tomorrow.timeInMillis,
                pendingIntent,
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                tomorrow.timeInMillis,
                pendingIntent,
            )
        }
    }

    private fun reminderPendingIntent(
        context: Context,
        requestCode: Int,
        channelId: String,
        title: String,
        body: String,
        openTab: String,
        flags: Int,
    ): PendingIntent {
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = ACTION_FIRE_REMINDER
            putExtra(EXTRA_NOTIFICATION_ID, requestCode)
            putExtra(EXTRA_CHANNEL_ID, channelId)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_BODY, body)
            putExtra(EXTRA_OPEN_TAB, openTab)
        }
        return PendingIntent.getBroadcast(context, requestCode, intent, flags)
    }

    private fun refreshPendingIntent(context: Context, flags: Int): PendingIntent {
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = ACTION_RESCHEDULE
        }
        return PendingIntent.getBroadcast(context, REQUEST_REFRESH, intent, flags)
    }

    private data class ReminderEvent(
        val requestCode: Int,
        val triggerAtMillis: Long,
        val channelId: String,
        val title: String,
        val body: String,
        val openTab: String,
    )
}

package com.azkari.wasalati

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            PrayerScheduler.ACTION_RESCHEDULE -> PrayerScheduler.scheduleAll(context)
            PrayerScheduler.ACTION_FIRE_REMINDER -> {
                NotificationHelper.createChannels(context)
                NotificationHelper.showReminder(
                    context = context,
                    notificationId = intent.getIntExtra(
                        PrayerScheduler.EXTRA_NOTIFICATION_ID,
                        0,
                    ),
                    channelId = intent.getStringExtra(PrayerScheduler.EXTRA_CHANNEL_ID)
                        ?: NotificationHelper.CHANNEL_PRAYERS,
                    title = intent.getStringExtra(PrayerScheduler.EXTRA_TITLE)
                        ?: context.getString(R.string.app_name),
                    body = intent.getStringExtra(PrayerScheduler.EXTRA_BODY).orEmpty(),
                    openTab = intent.getStringExtra(PrayerScheduler.EXTRA_OPEN_TAB) ?: "prayer",
                )
            }
        }
    }
}


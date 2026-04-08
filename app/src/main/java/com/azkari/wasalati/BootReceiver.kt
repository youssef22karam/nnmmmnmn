package com.azkari.wasalati

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        NotificationHelper.createChannels(context)
        PrayerScheduler.scheduleAll(context)
    }
}


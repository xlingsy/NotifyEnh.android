package com.dansheng.notifyenh

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

class App : Application() {

    companion object {
        lateinit var instance: App
            private set

        const val CHANNEL_ID = "notify_enh_service_channel"
        const val ALARM_CHANNEL_ID = "notify_enh_alarm_channel"
    }


    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannel()

    }

    private fun createNotificationChannel() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.fg_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.fg_channel_desc)
        }
        manager.createNotificationChannel(channel)

        // Using a different ID for alarm channel to ensure it's recreated with correct importance
        val alarmChannel = NotificationChannel(
            ALARM_CHANNEL_ID + "_v2",
            getString(R.string.action_alarm),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            setSound(null, null)
            enableVibration(true)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
        }
        manager.createNotificationChannel(alarmChannel)
    }

}
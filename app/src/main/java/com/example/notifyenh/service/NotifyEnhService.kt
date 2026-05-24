package com.example.notifyenh.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotifyEnhService: NotificationListenerService() {


    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        // 提取通知信息
        val pkg = sbn.packageName
        val notification = sbn.notification
        val extras = notification.extras
        val title = extras.getString(android.app.Notification.EXTRA_TITLE, "")
        val text = extras.getString(android.app.Notification.EXTRA_TEXT, "")
    }

}
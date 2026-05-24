package com.example.notifyenh.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotifyEnhService: NotificationListenerService() {


    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
    }

}
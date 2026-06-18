package com.dansheng.notifyenh.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dansheng.notifyenh.util.LogUtils

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        LogUtils.d("BootReceiver received action: $action")
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == "android.intent.action.QUICKBOOT_POWERON" ||
            action == "com.htc.intent.action.QUICKBOOT_POWERON"
        ) {
            NotifyEnhService.tryReconnectService(context)
        }
    }
}

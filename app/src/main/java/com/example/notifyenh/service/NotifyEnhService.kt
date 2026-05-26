package com.example.notifyenh.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.speech.tts.TextToSpeech
import android.util.Log
import com.example.notifyenh.data.AppDatabase
import com.example.notifyenh.data.NotificationEntity
import com.example.notifyenh.data.TaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Locale

class NotifyEnhService: NotificationListenerService(), TextToSpeech.OnInitListener {

    companion object {
        private const val TAG = "NotifyEnhService"
        var isServiceRunning = false
            private set
        
        private var instance: NotifyEnhService? = null

        fun stopService() {
            instance?.requestUnbind()
            isServiceRunning = false
        }
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var database: AppDatabase
    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false

    override fun onListenerConnected() {
        super.onListenerConnected()
        isServiceRunning = true
        instance = this
        Log.d(TAG, "Service connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        isServiceRunning = false
        instance = null
        Log.d(TAG, "Service disconnected")
    }

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
        tts = TextToSpeech(this, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.getDefault()
            isTtsInitialized = true
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        
        val pkg = sbn.packageName
        val notification = sbn.notification
        val extras = notification.extras
        val title = extras.getString(android.app.Notification.EXTRA_TITLE, "") ?: ""
        val content = extras.getString(android.app.Notification.EXTRA_TEXT, "") ?: ""
        val postTime = sbn.postTime

        // 存入数据库
        serviceScope.launch {
            val entity = NotificationEntity(
                packageName = pkg,
                title = title,
                content = content,
                postTime = postTime
            )
            database.notificationDao().insert(entity)
            
            // 处理任务
            processTasks(sbn, title, content)
        }
    }

    private suspend fun processTasks(sbn: StatusBarNotification, title: String, content: String) {
        val enabledTasks = database.taskDao().getEnabledTasks()
        val fullText = "$title $content"
        
        for (task in enabledTasks) {
            if (isMatch(task, fullText, sbn.packageName)) {
                handleAction(task, sbn, title, content)
            }
        }
    }

    private fun isMatch(task: TaskEntity, text: String, sbnPackage: String): Boolean {
        // 优先匹配包名
        if (!task.packageName.isNullOrBlank() && task.packageName != sbnPackage) {
            return false
        }

        return try {
            if (task.isRegex) {
                Regex(task.pattern, RegexOption.IGNORE_CASE).containsMatchIn(text)
            } else {
                text.contains(task.pattern, ignoreCase = true)
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun handleAction(task: TaskEntity, sbn: StatusBarNotification, title: String, content: String) {
        if (task.actionCancel) {
            cancelNotification(sbn.key)
        }
        
        if (task.actionTts && isTtsInitialized) {
            val speechText = if (title.isNotBlank()) "$title: $content" else content
            tts?.speak(speechText, TextToSpeech.QUEUE_ADD, null, "notify_${sbn.postTime}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        instance = null
        tts?.stop()
        tts?.shutdown()
    }
}

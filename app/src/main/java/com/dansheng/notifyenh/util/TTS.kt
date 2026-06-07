package com.dansheng.notifyenh.util

import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.dansheng.notifyenh.App
import java.util.Locale

object TTS : TextToSpeech.OnInitListener, UtteranceProgressListener() {

    private val mainHandler = Handler(Looper.getMainLooper())
    private val ttsReleaseRunnable = Runnable {
        tts?.shutdown()
        tts = null
        ttsIsInitRelease = false
    }
    private val speakList = arrayListOf<String>()
    private var tts: TextToSpeech? = null
    private var ttsIsInitRelease = false

    @Synchronized
    fun speak(text: String) {
        mainHandler.removeCallbacks(ttsReleaseRunnable)
        speakList.add(text)
        if (tts == null) {
            tts = TextToSpeech(App.instance, this)
        } else if (ttsIsInitRelease) {
            speak()
        }
    }

    @Synchronized
    private fun speak() {
        if (!ttsIsInitRelease) return
        speakList.forEach {
            tts?.speak(
                it,
                TextToSpeech.QUEUE_ADD,
                null,
                "notify_${System.currentTimeMillis()}"
            )
        }
        speakList.clear()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.getDefault()
            ttsIsInitRelease = true
            speak()
        }
    }

    override fun onDone(p0: String?) {
        //一分钟没有朗读释放资源
        mainHandler.postDelayed(ttsReleaseRunnable, 60000L)
    }

    @Deprecated("Deprecated in Java")
    override fun onError(p0: String?) {

    }

    override fun onStart(p0: String?) {
        //开始朗读取消释放资源任务
        mainHandler.removeCallbacks(ttsReleaseRunnable)
    }

}
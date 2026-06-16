package com.dansheng.notifyenh.util

import android.content.Context
import android.content.Intent
import com.dansheng.notifyenh.ui.CrashActivity
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.system.exitProcess

class CrashHandler(private val context: Context) : Thread.UncaughtExceptionHandler {

    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    init {
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))
            val crashLog = sw.toString()

            val intent = Intent(context, CrashActivity::class.java).apply {
                putExtra(CrashActivity.EXTRA_CRASH_LOG, crashLog)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            // Fallback if starting activity fails
            defaultHandler?.uncaughtException(thread, throwable)
        } finally {
            // Kill the process as it's in an unstable state
            android.os.Process.killProcess(android.os.Process.myPid())
            exitProcess(10)
        }
    }
}

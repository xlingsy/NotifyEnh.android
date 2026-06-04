package com.dansheng.notifyenh.util

import android.content.Context
import android.util.Log
import com.dansheng.notifyenh.data.AppDatabase
import com.dansheng.notifyenh.data.TaskEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

object BackupUtils {
    private const val TAG = "BackupUtils"
    private const val AUTO_BACKUP_FILE_NAME = "tasks_auto_backup.json"

    suspend fun autoBackup(context: Context) {
        withContext(Dispatchers.IO) {
            try {
                val database = AppDatabase.getDatabase(context)
                val tasks = database.taskDao().getAllTasksList()
                val json = Json.encodeToString(tasks)

                val backupFile = File(context.filesDir, AUTO_BACKUP_FILE_NAME)
                backupFile.writeText(json)
                Log.d(TAG, "Auto backup successful: ${backupFile.absolutePath}")
            } catch (e: Exception) {
                Log.e(TAG, "Auto backup failed", e)
            }
        }
    }

    suspend fun restoreFromAutoBackup(context: Context): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val backupFile = File(context.filesDir, AUTO_BACKUP_FILE_NAME)
                if (!backupFile.exists()) return@withContext false

                val json = backupFile.readText()
                val tasks = Json.decodeFromString<List<TaskEntity>>(json)

                val database = AppDatabase.getDatabase(context)
                // Clear ID to insert as new tasks if needed, or just insert
                val newTasks = tasks.map { it.copy(id = 0) }
                database.taskDao().insertAll(newTasks)
                true
            } catch (e: Exception) {
                Log.e(TAG, "Restore from auto backup failed", e)
                false
            }
        }
    }
}

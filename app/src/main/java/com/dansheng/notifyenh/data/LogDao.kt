package com.dansheng.notifyenh.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    @Insert
    suspend fun insert(log: LogEntity)

    @Query("SELECT * FROM app_logs ORDER BY time DESC")
    fun getAllLogs(): Flow<List<LogEntity>>

    @Query("DELETE FROM app_logs WHERE time < :timestamp")
    suspend fun deleteOldLogs(timestamp: Long)

    @Query("DELETE FROM app_logs")
    suspend fun deleteAll()
}

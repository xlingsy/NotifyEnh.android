package com.example.notifyenh.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface NotificationDao {
    @Insert
    suspend fun insert(notification: NotificationEntity)

    @Query("SELECT * FROM notifications ORDER BY postTime DESC")
    fun getAllNotificationsFlow(): kotlinx.coroutines.flow.Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE title LIKE '%' || :searchQuery || '%' OR content LIKE '%' || :searchQuery || '%' OR packageName LIKE '%' || :searchQuery || '%' ORDER BY postTime DESC")
    fun searchNotifications(searchQuery: String): kotlinx.coroutines.flow.Flow<List<NotificationEntity>>
}

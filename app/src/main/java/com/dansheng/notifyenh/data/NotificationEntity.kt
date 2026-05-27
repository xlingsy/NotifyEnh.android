package com.dansheng.notifyenh.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val title: String?,
    val content: String?,
    val postTime: Long,
    val triggeredTaskId: Long? = null // 新增：触发的任务 ID（非空表示触发了任务）
)

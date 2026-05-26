package com.dansheng.notifyenh.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val packageName: String? = null, // 新增包名过滤
    val pattern: String,
    val isRegex: Boolean = false,
    val actionCancel: Boolean = false,
    val actionTts: Boolean = false,
    val isEnabled: Boolean = true
)

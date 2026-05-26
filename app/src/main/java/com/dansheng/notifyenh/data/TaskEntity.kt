package com.dansheng.notifyenh.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val packageName: String? = null,
    val titlePattern: String? = null, // 新增：标题匹配模式
    val contentPattern: String? = null, // 新增：内容匹配模式
    val pattern: String = "", // 保留以兼容旧数据或作为通用匹配
    val isRegex: Boolean = false,
    val actionCancel: Boolean = false,
    val actionTts: Boolean = false,
    val isEnabled: Boolean = true
)

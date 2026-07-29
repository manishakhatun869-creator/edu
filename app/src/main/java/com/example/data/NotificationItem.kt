package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val targetType: String, // "ALL", "CLASS", "STUDENT"
    val targetValue: String, // e.g. "ALL", "Class 10-A", or studentCode "STU-1001"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val sender: String = "School Admin"
)

package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attendance")
data class Attendance(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentCode: String,
    val date: String, // Format "YYYY-MM-DD"
    val status: String, // "PRESENT", "ABSENT", "LATE"
    val remarks: String = ""
)

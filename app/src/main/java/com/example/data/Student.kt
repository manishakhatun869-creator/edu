package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentCode: String, // Auto generated unique code like "STU-1024"
    val name: String,
    val className: String, // e.g. "Class 10-A"
    val rollNumber: String,
    val parentContact: String = "",
    val email: String = "",
    val joinedDate: String = "",
    val status: String = "ACTIVE"
)

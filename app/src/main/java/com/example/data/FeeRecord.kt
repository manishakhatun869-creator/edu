package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fee_records")
data class FeeRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val receiptNo: String, // Unique receipt number e.g. "REC-2026-089"
    val studentCode: String,
    val title: String, // e.g., "Term 1 Tuition Fee", "Exam & Sports Fee"
    val amount: Double,
    val dueDate: String,
    val paidDate: String? = null,
    val status: String, // "PAID", "PENDING", "OVERDUE"
    val paymentMethod: String? = null // "UPI", "Cash", "Card", "Bank Transfer"
)

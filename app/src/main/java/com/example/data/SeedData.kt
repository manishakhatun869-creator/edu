package com.example.data

import java.text.SimpleDateFormat
import java.util.*

object SeedData {
    val initialStudents = listOf(
        Student(
            studentCode = "STU-1001",
            name = "Aarav Sharma",
            className = "Class 10-A",
            rollNumber = "01",
            parentContact = "+1 555-0192",
            email = "aarav.s@school.edu",
            joinedDate = "2025-08-15"
        ),
        Student(
            studentCode = "STU-1002",
            name = "Ananya Patel",
            className = "Class 10-A",
            rollNumber = "02",
            parentContact = "+1 555-0144",
            email = "ananya.p@school.edu",
            joinedDate = "2025-08-15"
        ),
        Student(
            studentCode = "STU-1003",
            name = "Rohan Verma",
            className = "Class 10-A",
            rollNumber = "03",
            parentContact = "+1 555-0188",
            email = "rohan.v@school.edu",
            joinedDate = "2025-08-16"
        ),
        Student(
            studentCode = "STU-1004",
            name = "Diya Sengupta",
            className = "Class 10-B",
            rollNumber = "01",
            parentContact = "+1 555-0211",
            email = "diya.s@school.edu",
            joinedDate = "2025-08-16"
        ),
        Student(
            studentCode = "STU-1005",
            name = "Kabir Khan",
            className = "Class 10-B",
            rollNumber = "02",
            parentContact = "+1 555-0233",
            email = "kabir.k@school.edu",
            joinedDate = "2025-08-17"
        ),
        Student(
            studentCode = "STU-1006",
            name = "Sanya Mehta",
            className = "Class 9-A",
            rollNumber = "01",
            parentContact = "+1 555-0377",
            email = "sanya.m@school.edu",
            joinedDate = "2025-08-20"
        ),
        Student(
            studentCode = "STU-1007",
            name = "Rahim Ahmed",
            className = "Class 10-A",
            rollNumber = "04",
            parentContact = "+1 555-0499",
            email = "rahim.a@school.edu",
            joinedDate = "2025-08-20"
        )
    )

    fun getInitialAttendance(): List<Attendance> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        val list = mutableListOf<Attendance>()

        // Generate attendance for the last 15 days
        for (i in 0..14) {
            cal.time = Date()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            // Skip weekends
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) continue

            val dateStr = dateFormat.format(cal.time)

            // STU-1001 (Aarav): mostly present, 1 late, 1 absent
            val status1 = when (i) {
                3 -> "LATE"
                7 -> "ABSENT"
                else -> "PRESENT"
            }
            list.add(Attendance(studentCode = "STU-1001", date = dateStr, status = status1))

            // STU-1002 (Ananya): 100% present
            list.add(Attendance(studentCode = "STU-1002", date = dateStr, status = "PRESENT"))

            // STU-1003 (Rohan): some late
            val status3 = if (i % 4 == 0) "LATE" else "PRESENT"
            list.add(Attendance(studentCode = "STU-1003", date = dateStr, status = status3))

            // STU-1004 (Diya)
            list.add(Attendance(studentCode = "STU-1004", date = dateStr, status = if (i == 2) "ABSENT" else "PRESENT"))

            // STU-1005 (Kabir)
            list.add(Attendance(studentCode = "STU-1005", date = dateStr, status = "PRESENT"))

            // STU-1006 (Sanya)
            list.add(Attendance(studentCode = "STU-1006", date = dateStr, status = "PRESENT"))

            // STU-1007 (Rahim)
            val statusRahim = if (i == 1) "ABSENT" else if (i == 4) "LATE" else "PRESENT"
            list.add(Attendance(studentCode = "STU-1007", date = dateStr, status = statusRahim))
        }

        return list
    }

    fun generateInitialFees(): List<FeeRecord> {
        val months = listOf(
            "January" to "01-10",
            "February" to "02-10",
            "March" to "03-10",
            "April" to "04-10",
            "May" to "05-10",
            "June" to "06-10",
            "July" to "07-10",
            "August" to "08-10",
            "September" to "09-10",
            "October" to "10-10",
            "November" to "11-10",
            "December" to "12-10"
        )

        val list = mutableListOf<FeeRecord>()
        var count = 1

        val studentCodes = listOf("STU-1001", "STU-1002", "STU-1003", "STU-1004", "STU-1005", "STU-1006", "STU-1007")

        studentCodes.forEach { code ->
            months.forEachIndexed { monthIdx, (monthName, monthDay) ->
                val receiptNumber = "REC-2026-${String.format("%03d", count++)}"
                val dueDateStr = "2026-$monthDay"

                // First 6-7 months marked paid for realistic 12-month tracker
                val isPaid = monthIdx < 6 || (code == "STU-1002" && monthIdx < 7) || (code == "STU-1007" && monthIdx < 5)
                val statusStr = if (isPaid) "PAID" else if (monthIdx == 7) "OVERDUE" else "PENDING"
                val paidDateStr = if (isPaid) "2026-0$monthIdx-05" else null
                val methodStr = if (isPaid) (if (count % 2 == 0) "UPI / Online" else "Cash") else null

                list.add(
                    FeeRecord(
                        receiptNo = receiptNumber,
                        studentCode = code,
                        title = "$monthName 2026 Tuition Fee (₹350)",
                        amount = 350.00,
                        dueDate = dueDateStr,
                        paidDate = paidDateStr,
                        status = statusStr,
                        paymentMethod = methodStr
                    )
                )
            }
        }
        return list
    }

    val initialFees = generateInitialFees()

    val initialNotifications = listOf(
        NotificationItem(
            title = "Parent-Teacher Meeting Scheduled",
            message = "Dear Parents & Students, PTM for Class 10 will be conducted this Saturday at 10:00 AM.",
            targetType = "CLASS",
            targetValue = "Class 10-A",
            timestamp = System.currentTimeMillis() - 86400000L * 2
        ),
        NotificationItem(
            title = "Annual Science Exhibition",
            message = "Registration for Science Fair 2026 is now open! Contact your class teacher before next Friday.",
            targetType = "ALL",
            targetValue = "ALL",
            timestamp = System.currentTimeMillis() - 86400000L
        ),
        NotificationItem(
            title = "Fee Reminder: August Monthly Fee (₹350)",
            message = "Your pending fee invoice for August 2026 (₹350) is due on Aug 10, 2026.",
            targetType = "STUDENT",
            targetValue = "STU-1001",
            timestamp = System.currentTimeMillis() - 3600000L * 3
        ),
        NotificationItem(
            title = "Attendance Alert: ABSENT",
            message = "Today Rahim Ahmed is ABSENT.",
            targetType = "STUDENT",
            targetValue = "STU-1007",
            timestamp = System.currentTimeMillis() - 3600000L * 2
        ),
        NotificationItem(
            title = "Fee Payment Confirmed for Rahim Ahmed",
            message = "Today fee payment of ₹350 for Rahim Ahmed (January 2026 Tuition Fee) was marked PAID via Cash.",
            targetType = "STUDENT",
            targetValue = "STU-1007",
            timestamp = System.currentTimeMillis() - 3600000L * 5
        )
    )
}

package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class EduRepository(private val dao: EduDao) {

    val allStudents: Flow<List<Student>> = dao.getAllStudents()
    val allAttendance: Flow<List<Attendance>> = dao.getAllAttendance()
    val allFees: Flow<List<FeeRecord>> = dao.getAllFees()
    val allNotifications: Flow<List<NotificationItem>> = dao.getAllNotifications()

    fun getStudentsByClass(className: String): Flow<List<Student>> =
        dao.getStudentsByClass(className)

    suspend fun getStudentByCode(code: String): Student? = withContext(Dispatchers.IO) {
        dao.getStudentByCode(code)
    }

    fun getAttendanceForStudent(studentCode: String): Flow<List<Attendance>> =
        dao.getAttendanceForStudent(studentCode)

    fun getFeesForStudent(studentCode: String): Flow<List<FeeRecord>> =
        dao.getFeesForStudent(studentCode)

    fun getNotificationsForStudent(studentCode: String, className: String): Flow<List<NotificationItem>> =
        dao.getNotificationsForStudent(studentCode, className)

    suspend fun insertStudent(name: String, className: String, rollNumber: String, parentContact: String, email: String): String =
        withContext(Dispatchers.IO) {
            val randomNum = (1000..9999).random()
            val studentCode = "STU-$randomNum"
            val newStudent = Student(
                studentCode = studentCode,
                name = name,
                className = className,
                rollNumber = rollNumber,
                parentContact = parentContact,
                email = email,
                joinedDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            )
            dao.insertStudent(newStudent)
            studentCode
        }

    suspend fun updateStudent(student: Student) = withContext(Dispatchers.IO) {
        dao.updateStudent(student)
    }

    suspend fun deleteStudent(student: Student) = withContext(Dispatchers.IO) {
        dao.deleteStudent(student)
    }

    suspend fun markAttendanceBatch(attendanceList: List<Attendance>, date: String, className: String) =
        withContext(Dispatchers.IO) {
            dao.insertAttendanceList(attendanceList)

            // Generate personalized notification for each student with their name
            attendanceList.forEach { att ->
                val student = dao.getStudentByCode(att.studentCode)
                val studentName = student?.name ?: att.studentCode

                val statusDisplay = when (att.status.uppercase()) {
                    "ABSENT" -> "ABSENT"
                    "LATE" -> "LATE"
                    else -> "PRESENT"
                }

                val notifyStudent = NotificationItem(
                    title = "Attendance Alert: $statusDisplay",
                    message = "Today $studentName is $statusDisplay.",
                    targetType = "STUDENT",
                    targetValue = att.studentCode,
                    sender = "Class Administrator"
                )
                dao.insertNotification(notifyStudent)
            }

            // Trigger push notification to class
            val notify = NotificationItem(
                title = "Attendance Updated for $date",
                message = "Attendance status for $className on $date has been marked by Admin.",
                targetType = "CLASS",
                targetValue = className,
                sender = "Class Administrator"
            )
            dao.insertNotification(notify)
        }

    suspend fun markAttendanceSingle(attendance: Attendance) = withContext(Dispatchers.IO) {
        dao.insertAttendance(attendance)
        val student = dao.getStudentByCode(attendance.studentCode)
        val studentName = student?.name ?: attendance.studentCode
        val statusDisplay = when (attendance.status.uppercase()) {
            "ABSENT" -> "ABSENT"
            "LATE" -> "LATE"
            else -> "PRESENT"
        }
        val notify = NotificationItem(
            title = "Attendance Alert: $statusDisplay",
            message = "Today $studentName is $statusDisplay.",
            targetType = "STUDENT",
            targetValue = attendance.studentCode,
            sender = "Class Administrator"
        )
        dao.insertNotification(notify)
    }

    suspend fun createFeeRecord(studentCode: String, title: String, amount: Double, dueDate: String): String =
        withContext(Dispatchers.IO) {
            val rand = (100..999).random()
            val receiptNo = "REC-2026-$rand"
            val fee = FeeRecord(
                receiptNo = receiptNo,
                studentCode = studentCode,
                title = title,
                amount = amount,
                dueDate = dueDate,
                status = "PENDING"
            )
            dao.insertFee(fee)

            val student = dao.getStudentByCode(studentCode)
            val studentName = student?.name ?: studentCode
            val notify = NotificationItem(
                title = "Fee Invoice Issued for $studentName",
                message = "Today fee invoice ($title - ₹$amount) was issued for $studentName due on $dueDate. Receipt #: $receiptNo.",
                targetType = "STUDENT",
                targetValue = studentCode,
                sender = "Accounts Dept"
            )
            dao.insertNotification(notify)

            receiptNo
        }

    suspend fun markFeeAsPaid(feeId: Int, paymentMethod: String): String = withContext(Dispatchers.IO) {
        val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val fee = dao.getFeeById(feeId)
        dao.updateFeePayment(feeId, "PAID", todayStr, paymentMethod)

        if (fee != null) {
            val student = dao.getStudentByCode(fee.studentCode)
            val studentName = student?.name ?: fee.studentCode
            val notify = NotificationItem(
                title = "Fee Payment Confirmed for $studentName",
                message = "Today fee payment of ₹${fee.amount} for $studentName (${fee.title}) was marked PAID via $paymentMethod. Receipt #: ${fee.receiptNo}.",
                targetType = "STUDENT",
                targetValue = fee.studentCode,
                sender = "Accounts Dept"
            )
            dao.insertNotification(notify)
        }

        "SUCCESS"
    }

    suspend fun sendNotification(title: String, message: String, targetType: String, targetValue: String) =
        withContext(Dispatchers.IO) {
            val notify = NotificationItem(
                title = title,
                message = message,
                targetType = targetType,
                targetValue = targetValue,
                sender = "Admin Announcement"
            )
            dao.insertNotification(notify)
        }

    suspend fun markNotificationAsRead(id: Int) = withContext(Dispatchers.IO) {
        dao.markNotificationAsRead(id)
    }

    suspend fun checkAndSeedDatabase() = withContext(Dispatchers.IO) {
        val currentStudents = dao.getAllStudents().first()
        if (currentStudents.isEmpty()) {
            SeedData.initialStudents.forEach { dao.insertStudent(it) }
            dao.insertAttendanceList(SeedData.getInitialAttendance())
            SeedData.initialFees.forEach { dao.insertFee(it) }
            SeedData.initialNotifications.forEach { dao.insertNotification(it) }
        }
    }
}

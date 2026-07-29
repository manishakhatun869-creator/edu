package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EduDao {
    // Student Queries
    @Query("SELECT * FROM students ORDER BY className ASC, rollNumber ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE className = :className ORDER BY rollNumber ASC")
    fun getStudentsByClass(className: String): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE studentCode = :code LIMIT 1")
    suspend fun getStudentByCode(code: String): Student?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    // Attendance Queries
    @Query("SELECT * FROM attendance ORDER BY date DESC")
    fun getAllAttendance(): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE studentCode = :studentCode ORDER BY date DESC")
    fun getAttendanceForStudent(studentCode: String): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE date = :date")
    fun getAttendanceByDate(date: String): Flow<List<Attendance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceList(list: List<Attendance>)

    // Fees Queries
    @Query("SELECT * FROM fee_records ORDER BY dueDate DESC")
    fun getAllFees(): Flow<List<FeeRecord>>

    @Query("SELECT * FROM fee_records WHERE studentCode = :studentCode ORDER BY dueDate DESC")
    fun getFeesForStudent(studentCode: String): Flow<List<FeeRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFee(fee: FeeRecord): Long

    @Query("SELECT * FROM fee_records WHERE id = :id LIMIT 1")
    suspend fun getFeeById(id: Int): FeeRecord?

    @Query("UPDATE fee_records SET status = :status, paidDate = :paidDate, paymentMethod = :paymentMethod WHERE id = :id")
    suspend fun updateFeePayment(id: Int, status: String, paidDate: String, paymentMethod: String)

    // Notifications Queries
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationItem>>

    @Query("SELECT * FROM notifications WHERE targetType = 'ALL' OR (targetType = 'CLASS' AND targetValue = :className) OR (targetType = 'STUDENT' AND targetValue = :studentCode) ORDER BY timestamp DESC")
    fun getNotificationsForStudent(studentCode: String, className: String): Flow<List<NotificationItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationItem): Long

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: Int)
}

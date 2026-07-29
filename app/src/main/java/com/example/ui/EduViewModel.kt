package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class Role {
    ADMIN, STUDENT
}

class EduViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = EduRepository(db.eduDao())

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentRole = MutableStateFlow(Role.ADMIN)
    val currentRole: StateFlow<Role> = _currentRole.asStateFlow()

    private val _selectedStudentCode = MutableStateFlow("STU-1007")
    val selectedStudentCode: StateFlow<String> = _selectedStudentCode.asStateFlow()

    // Real-time Push Notification alert popup in UI
    private val _inAppToast = MutableStateFlow<NotificationItem?>(null)
    val inAppToast: StateFlow<NotificationItem?> = _inAppToast.asStateFlow()

    val allStudents: StateFlow<List<Student>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAttendance: StateFlow<List<Attendance>> = repository.allAttendance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFees: StateFlow<List<FeeRecord>> = repository.allFees
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotifications: StateFlow<List<NotificationItem>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Student Profile
    val activeStudent: StateFlow<Student?> = combine(allStudents, _selectedStudentCode) { students, code ->
        students.find { it.studentCode == code }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Active Student Attendance
    val activeStudentAttendance: StateFlow<List<Attendance>> = combine(allAttendance, _selectedStudentCode) { list, code ->
        list.filter { it.studentCode == code }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Student Fees
    val activeStudentFees: StateFlow<List<FeeRecord>> = combine(allFees, _selectedStudentCode) { list, code ->
        list.filter { it.studentCode == code }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Student Notifications
    val activeStudentNotifications: StateFlow<List<NotificationItem>> = combine(
        allNotifications,
        activeStudent
    ) { notifications, student ->
        if (student == null) emptyList()
        else {
            notifications.filter {
                it.targetType == "ALL" ||
                (it.targetType == "CLASS" && it.targetValue == student.className) ||
                (it.targetType == "STUDENT" && it.targetValue == student.studentCode)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.checkAndSeedDatabase()
        }
    }

    fun loginAsAdmin() {
        _currentRole.value = Role.ADMIN
        _isLoggedIn.value = true
    }

    fun loginAsStudent(studentCode: String) {
        _currentRole.value = Role.STUDENT
        _selectedStudentCode.value = studentCode
        _isLoggedIn.value = true
    }

    fun logout() {
        _isLoggedIn.value = false
    }

    fun selectRole(role: Role, studentCode: String? = null) {
        _currentRole.value = role
        if (studentCode != null) {
            _selectedStudentCode.value = studentCode
        }
    }

    fun setSelectedStudentCode(code: String) {
        _selectedStudentCode.value = code
    }

    fun addStudent(
        name: String,
        className: String,
        rollNumber: String,
        parentContact: String,
        email: String,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            val generatedCode = repository.insertStudent(name, className, rollNumber, parentContact, email)
            onSuccess(generatedCode)
        }
    }

    fun updateStudent(student: Student) {
        viewModelScope.launch {
            repository.updateStudent(student)
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            repository.deleteStudent(student)
        }
    }

    fun markBatchAttendance(
        attendanceMap: Map<String, String>, // studentCode -> Status
        date: String,
        className: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val list = attendanceMap.map { (code, status) ->
                Attendance(studentCode = code, date = date, status = status)
            }
            repository.markAttendanceBatch(list, date, className)
            
            // Show personalized push notification toast alert
            val firstCode = attendanceMap.keys.firstOrNull()
            val firstStudent = allStudents.value.find { it.studentCode == firstCode }
            val studentName = firstStudent?.name ?: "Student"
            val status = attendanceMap[firstCode] ?: "PRESENT"

            val alert = NotificationItem(
                title = "Attendance Marked ($className)",
                message = "Today $studentName is $status. Notifications sent to all student panels.",
                targetType = "CLASS",
                targetValue = className
            )
            _inAppToast.value = alert
            onSuccess()
        }
    }

    fun createFeeInvoice(
        studentCode: String,
        title: String,
        amount: Double,
        dueDate: String,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            val receiptNo = repository.createFeeRecord(studentCode, title, amount, dueDate)
            val student = allStudents.value.find { it.studentCode == studentCode }
            val studentName = student?.name ?: studentCode

            val alert = NotificationItem(
                title = "Fee Invoice Issued for $studentName",
                message = "Today fee invoice ($title - ₹$amount) issued for $studentName.",
                targetType = "STUDENT",
                targetValue = studentCode
            )
            _inAppToast.value = alert
            onSuccess(receiptNo)
        }
    }

    fun markFeePaid(feeId: Int, paymentMethod: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val fee = allFees.value.find { it.id == feeId }
            repository.markFeeAsPaid(feeId, paymentMethod)
            val student = allStudents.value.find { it.studentCode == fee?.studentCode }
            val studentName = student?.name ?: fee?.studentCode ?: "Student"

            val alert = NotificationItem(
                title = "Fee Confirmed Paid",
                message = "Today fee payment for $studentName was confirmed ($paymentMethod).",
                targetType = "STUDENT",
                targetValue = fee?.studentCode ?: ""
            )
            _inAppToast.value = alert
            onSuccess()
        }
    }

    fun sendPushNotification(
        title: String,
        message: String,
        targetType: String,
        targetValue: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            repository.sendNotification(title, message, targetType, targetValue)
            val alert = NotificationItem(
                title = "Push Notification Sent!",
                message = "$title -> Delivered to $targetValue",
                targetType = targetType,
                targetValue = targetValue
            )
            _inAppToast.value = alert
            onSuccess()
        }
    }

    fun markNotificationRead(id: Int) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun dismissToast() {
        _inAppToast.value = null
    }
}

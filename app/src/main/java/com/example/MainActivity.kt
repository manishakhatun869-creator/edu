package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.*
import com.example.ui.admin.*
import com.example.ui.components.InAppNotificationBanner
import com.example.ui.components.RoleHeader
import com.example.ui.student.*
import com.example.ui.theme.MyApplicationTheme

import com.example.ui.auth.LoginScreen

class MainActivity : ComponentActivity() {

    private val viewModel: EduViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
                val allStudents by viewModel.allStudents.collectAsStateWithLifecycle()

                if (!isLoggedIn) {
                    LoginScreen(
                        students = allStudents,
                        onLoginAdmin = { viewModel.loginAsAdmin() },
                        onLoginStudent = { studentCode -> viewModel.loginAsStudent(studentCode) }
                    )
                } else {
                    EduManageApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun EduManageApp(viewModel: EduViewModel) {
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val selectedStudentCode by viewModel.selectedStudentCode.collectAsStateWithLifecycle()
    val inAppToast by viewModel.inAppToast.collectAsStateWithLifecycle()

    val allStudents by viewModel.allStudents.collectAsStateWithLifecycle()
    val allAttendance by viewModel.allAttendance.collectAsStateWithLifecycle()
    val allFees by viewModel.allFees.collectAsStateWithLifecycle()
    val allNotifications by viewModel.allNotifications.collectAsStateWithLifecycle()

    val activeStudent by viewModel.activeStudent.collectAsStateWithLifecycle()
    val activeStudentAttendance by viewModel.activeStudentAttendance.collectAsStateWithLifecycle()
    val activeStudentFees by viewModel.activeStudentFees.collectAsStateWithLifecycle()
    val activeStudentNotifications by viewModel.activeStudentNotifications.collectAsStateWithLifecycle()

    val unreadNotifCount = remember(activeStudentNotifications) {
        activeStudentNotifications.count { !it.isRead }
    }

    var selectedTab by remember(currentRole) { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            RoleHeader(
                currentRole = currentRole,
                students = allStudents,
                selectedStudentCode = selectedStudentCode,
                unreadNotificationCount = unreadNotifCount,
                onRoleChange = { role, code ->
                    viewModel.selectRole(role, code)
                    selectedTab = 0
                },
                onStudentSelect = { code ->
                    viewModel.setSelectedStudentCode(code)
                },
                onOpenNotifications = {
                    selectedTab = 3 // Jump to student notifications
                },
                onLogout = {
                    viewModel.logout()
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                contentColor = Color(0xFF1E3A8A),
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("app_navigation_bar"),
                tonalElevation = 8.dp
            ) {
                if (currentRole == Role.ADMIN) {
                    val adminItems = listOf(
                        Triple("Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
                        Triple("Students", Icons.Filled.People, Icons.Outlined.People),
                        Triple("Attendance", Icons.Filled.FactCheck, Icons.Outlined.FactCheck),
                        Triple("Fees", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong),
                        Triple("Alerts", Icons.Filled.Campaign, Icons.Outlined.Campaign)
                    )
                    adminItems.forEachIndexed { index, (label, selectedIcon, unselectedIcon) ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF2563EB),
                                selectedTextColor = Color(0xFF2563EB),
                                indicatorColor = Color(0xFFEFF6FF),
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            ),
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == index) selectedIcon else unselectedIcon,
                                    contentDescription = label
                                )
                            },
                            label = { Text(label, fontSize = 10.sp, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.testTag("admin_nav_$index")
                        )
                    }
                } else {
                    val studentItems = listOf(
                        Triple("Home", Icons.Filled.Home, Icons.Outlined.Home),
                        Triple("Attendance", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
                        Triple("Fees", Icons.Filled.Receipt, Icons.Outlined.Receipt),
                        Triple("Notifications", Icons.Filled.Notifications, Icons.Outlined.Notifications),
                        Triple("Profile", Icons.Filled.Person, Icons.Outlined.Person)
                    )
                    studentItems.forEachIndexed { index, (label, selectedIcon, unselectedIcon) ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFFEC4899),
                                selectedTextColor = Color(0xFFEC4899),
                                indicatorColor = Color(0xFFFCE7F3),
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            ),
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (index == 3 && unreadNotifCount > 0) {
                                            Badge(containerColor = Color(0xFFEC4899)) { Text(unreadNotifCount.toString(), color = Color.White) }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (selectedTab == index) selectedIcon else unselectedIcon,
                                        contentDescription = label
                                    )
                                }
                            },
                            label = { Text(label, fontSize = 10.sp, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.testTag("student_nav_$index")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (currentRole == Role.ADMIN) {
                when (selectedTab) {
                    0 -> AdminDashboard(
                        students = allStudents,
                        attendance = allAttendance,
                        fees = allFees,
                        notifications = allNotifications,
                        onNavigateTab = { selectedTab = it }
                    )
                    1 -> AdminStudentsScreen(
                        students = allStudents,
                        onAddStudent = { name, className, roll, parent, email ->
                            viewModel.addStudent(name, className, roll, parent, email) { generatedCode ->
                                viewModel.setSelectedStudentCode(generatedCode)
                            }
                        },
                        onUpdateStudent = { viewModel.updateStudent(it) },
                        onDeleteStudent = { viewModel.deleteStudent(it) },
                        onSelectStudentForPortal = { code ->
                            viewModel.selectRole(Role.STUDENT, code)
                            selectedTab = 0
                        }
                    )
                    2 -> AdminAttendanceScreen(
                        students = allStudents,
                        attendanceRecords = allAttendance,
                        onSaveBatchAttendance = { map, date, className ->
                            viewModel.markBatchAttendance(map, date, className) {}
                        }
                    )
                    3 -> AdminFeesScreen(
                        students = allStudents,
                        fees = allFees,
                        onCreateFeeInvoice = { code, title, amount, dueDate ->
                            viewModel.createFeeInvoice(code, title, amount, dueDate) {}
                        },
                        onMarkFeePaid = { feeId, method ->
                            viewModel.markFeePaid(feeId, method) {}
                        }
                    )
                    4 -> AdminNotificationScreen(
                        students = allStudents,
                        notificationsHistory = allNotifications,
                        onSendNotification = { title, msg, targetType, targetValue ->
                            viewModel.sendPushNotification(title, msg, targetType, targetValue) {}
                        }
                    )
                }
            } else {
                when (selectedTab) {
                    0 -> StudentDashboardScreen(
                        student = activeStudent,
                        attendance = activeStudentAttendance,
                        fees = activeStudentFees,
                        notifications = activeStudentNotifications,
                        onNavigateTab = { selectedTab = it }
                    )
                    1 -> StudentAttendanceScreen(
                        attendance = activeStudentAttendance
                    )
                    2 -> StudentFeesScreen(
                        student = activeStudent,
                        fees = activeStudentFees,
                        onPayFee = { feeId, method ->
                            viewModel.markFeePaid(feeId, method) {}
                        }
                    )
                    3 -> StudentNotificationsScreen(
                        notifications = activeStudentNotifications,
                        onMarkRead = { viewModel.markNotificationRead(it) }
                    )
                    4 -> StudentProfileScreen(
                        student = activeStudent
                    )
                }
            }

            // Real-time Push Alert Banner Overlay
            InAppNotificationBanner(
                notification = inAppToast,
                onDismiss = { viewModel.dismissToast() }
            )
        }
    }
}

package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Student
import com.example.ui.Role

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleHeader(
    currentRole: Role,
    students: List<Student>,
    selectedStudentCode: String,
    unreadNotificationCount: Int,
    onRoleChange: (Role, String?) -> Unit,
    onStudentSelect: (String) -> Unit,
    onOpenNotifications: () -> Unit,
    onLogout: () -> Unit
) {
    var showStudentDropdown by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 4.dp,
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF1E3A8A), // Deep Blue
                            Color(0xFFBE185D)  // Deep Pink
                        )
                    )
                )
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title & Subtitle
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (currentRole == Role.ADMIN) Icons.Default.AdminPanelSettings else Icons.Default.School,
                            contentDescription = "Role Icon",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "EduManage",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 18.sp
                        )
                        Text(
                            text = if (currentRole == Role.ADMIN) "Admin Panel" else "Student Portal",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                }

                // Switcher Controls & Logout
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Role Toggle Segmented Control
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.Black.copy(alpha = 0.3f)
                    ) {
                        Row(modifier = Modifier.padding(3.dp)) {
                            Box(
                                modifier = Modifier
                                    .testTag("admin_role_tab")
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (currentRole == Role.ADMIN) Color(0xFF2563EB) else Color.Transparent
                                    )
                                    .clickable { onRoleChange(Role.ADMIN, null) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Admin",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .testTag("student_role_tab")
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (currentRole == Role.STUDENT) Color(0xFFEC4899) else Color.Transparent
                                    )
                                    .clickable { onRoleChange(Role.STUDENT, selectedStudentCode) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Student",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    if (currentRole == Role.STUDENT) {
                        Spacer(modifier = Modifier.width(6.dp))
                        // Student Code Switcher
                        Box {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.clickable { showStudentDropdown = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = selectedStudentCode,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Select Student",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showStudentDropdown,
                                onDismissRequest = { showStudentDropdown = false }
                            ) {
                                Text(
                                    text = "Select Student Profile:",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Divider()
                                students.forEach { stu ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(text = "${stu.name} (${stu.studentCode})", fontWeight = FontWeight.SemiBold)
                                                Text(text = "${stu.className} • Roll ${stu.rollNumber}", fontSize = 11.sp, color = Color.Gray)
                                            }
                                        },
                                        onClick = {
                                            onStudentSelect(stu.studentCode)
                                            showStudentDropdown = false
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = if (stu.studentCode == selectedStudentCode) MaterialTheme.colorScheme.primary else Color.Gray
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(4.dp))
                        // Notification Bell
                        Box {
                            IconButton(
                                onClick = onOpenNotifications,
                                modifier = Modifier
                                    .testTag("notification_bell")
                                    .size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Notifications,
                                    contentDescription = "Notifications",
                                    tint = Color.White
                                )
                            }
                            if (unreadNotificationCount > 0) {
                                Badge(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = (-2).dp, y = (2).dp),
                                    containerColor = Color.Red,
                                    contentColor = Color.White
                                ) {
                                    Text(
                                        text = unreadNotificationCount.toString(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Logout Button
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .testTag("btn_header_logout")
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Red.copy(alpha = 0.25f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

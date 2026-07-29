package com.example.ui.auth

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Student

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    students: List<Student>,
    onLoginAdmin: () -> Unit,
    onLoginStudent: (studentCode: String) -> Unit
) {
    var isAdminTab by remember { mutableStateOf(true) }

    // Admin Login State
    var adminEmail by remember { mutableStateOf("admin@school.edu") }
    var adminPassword by remember { mutableStateOf("admin123") }
    var adminError by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Student Login State
    var selectedStudentCode by remember { mutableStateOf(students.firstOrNull()?.studentCode ?: "STU-1007") }
    var studentCodeInput by remember { mutableStateOf("STU-1007") }
    var expandedStudentDropdown by remember { mutableStateOf(false) }
    var studentError by remember { mutableStateOf("") }

    val activeStudent = remember(students, studentCodeInput, selectedStudentCode) {
        students.find { it.studentCode.equals(studentCodeInput.trim(), ignoreCase = true) }
            ?: students.find { it.studentCode == selectedStudentCode }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFEFF6FF), // Soft Sky Blue top
                        Color.White,       // Clean White middle
                        Color(0xFFFDF2F8)  // Soft Blush Pink bottom
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // School Branding Logo Header
            Surface(
                shape = CircleShape,
                color = Color(0xFF2563EB).copy(alpha = 0.12f),
                modifier = Modifier.size(76.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "School Logo",
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(42.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "EduManage Academy",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E3A8A)
            )
            Text(
                text = "School Administration & Student Portal Login",
                fontSize = 13.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Role Selector Switcher Card (Admin vs Student)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFFBCFE8), RoundedCornerShape(16.dp))
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("login_tab_admin")
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isAdminTab) Color(0xFF2563EB) else Color.Transparent
                            )
                            .clickable {
                                isAdminTab = true
                                adminError = ""
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = if (isAdminTab) Color.White else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Admin Login",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isAdminTab) Color.White else Color(0xFF4B5563)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("login_tab_student")
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (!isAdminTab) Color(0xFFEC4899) else Color.Transparent
                            )
                            .clickable {
                                isAdminTab = false
                                studentError = ""
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = if (!isAdminTab) Color.White else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Student Login",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (!isAdminTab) Color.White else Color(0xFF4B5563)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main Login Input Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isAdminTab) {
                        // ADMIN LOGIN PANEL
                        Text(
                            text = "Administrator Access",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF1E1B4B)
                        )
                        Text(
                            text = "Enter Admin Gmail & Password to access administrator portal",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = adminEmail,
                            onValueChange = {
                                adminEmail = it
                                adminError = ""
                            },
                            label = { Text("Admin Gmail / Email") },
                            placeholder = { Text("admin@gmail.com") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF4F46E5)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_email_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = adminPassword,
                            onValueChange = {
                                adminPassword = it
                                adminError = ""
                            },
                            label = { Text("Admin Password") },
                            placeholder = { Text("••••••••") },
                            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = Color(0xFF4F46E5)) },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null
                                    )
                                }
                            },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_password_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        if (adminError.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = adminError, color = Color.Red, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (adminEmail.trim().isNotEmpty() && adminPassword.trim().isNotEmpty()) {
                                    onLoginAdmin()
                                } else {
                                    adminError = "Please enter Admin Gmail and Password"
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_login_admin")
                        ) {
                            Icon(Icons.Default.Login, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Login to Admin Panel", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                adminEmail = "admin@school.edu"
                                adminPassword = "admin123"
                                onLoginAdmin()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Auto-Fill Demo Admin Credentials", fontSize = 13.sp)
                        }

                    } else {
                        // STUDENT LOGIN PANEL
                        Text(
                            text = "Student Portal Access",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF065F46)
                        )
                        Text(
                            text = "Select your student profile or enter your student code",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Quick Student Select Dropdown
                        Text(
                            text = "Select Student Account:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF374151),
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { expandedStudentDropdown = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("select_student_login_dropdown"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Outlined.Person, contentDescription = null, tint = Color(0xFF10B981))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (activeStudent != null) "${activeStudent.name} (${activeStudent.studentCode})" else "Select Student",
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF111827)
                                        )
                                    }
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }

                            DropdownMenu(
                                expanded = expandedStudentDropdown,
                                onDismissRequest = { expandedStudentDropdown = false }
                            ) {
                                Text(
                                    text = "Choose Student Profile:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                                Divider()
                                students.forEach { stu ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(text = "${stu.name} (${stu.studentCode})", fontWeight = FontWeight.Bold)
                                                Text(text = "${stu.className} • Roll ${stu.rollNumber}", fontSize = 11.sp, color = Color.Gray)
                                            }
                                        },
                                        onClick = {
                                            selectedStudentCode = stu.studentCode
                                            studentCodeInput = stu.studentCode
                                            expandedStudentDropdown = false
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.AccountCircle,
                                                contentDescription = null,
                                                tint = Color(0xFF10B981)
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = studentCodeInput,
                            onValueChange = {
                                studentCodeInput = it
                                studentError = ""
                            },
                            label = { Text("Or Enter Student Code / Passkey") },
                            placeholder = { Text("e.g. STU-1007") },
                            leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null, tint = Color(0xFF10B981)) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("student_code_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        if (studentError.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = studentError, color = Color.Red, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                val codeToUse = studentCodeInput.trim().ifEmpty { selectedStudentCode }
                                if (codeToUse.isNotEmpty()) {
                                    onLoginStudent(codeToUse)
                                } else {
                                    studentError = "Please select or enter a student code"
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_login_student")
                        ) {
                            Icon(Icons.Default.Login, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Login to Student Panel", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer info
            Text(
                text = "EduManage Portal • Indian Rupee (₹350/mo) Fee Management System",
                fontSize = 11.sp,
                color = Color.LightGray
            )
        }
    }
}

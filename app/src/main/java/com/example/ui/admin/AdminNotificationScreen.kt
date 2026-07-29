package com.example.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NotificationItem
import com.example.data.Student

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNotificationScreen(
    students: List<Student>,
    notificationsHistory: List<NotificationItem>,
    onSendNotification: (title: String, message: String, targetType: String, targetValue: String) -> Unit
) {
    var targetType by remember { mutableStateOf("ALL") } // "ALL", "CLASS", "STUDENT"
    var selectedClass by remember { mutableStateOf("Class 10-A") }
    var selectedStudentCode by remember { mutableStateOf(students.firstOrNull()?.studentCode ?: "STU-1001") }

    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    val classList = remember(students) {
        val list = students.map { it.className }.distinct().sorted()
        if (list.isEmpty()) listOf("Class 10-A") else list
    }

    var expandedClassDropdown by remember { mutableStateOf(false) }
    var expandedStudentDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Push Notification Center",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Broadcast announcements to whole school, specific class, or individual student",
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Compose Box Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Select Recipient Audience:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Audience Selector Radio Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = targetType == "ALL",
                            onClick = { targetType = "ALL" },
                            modifier = Modifier.testTag("audience_all_radio")
                        )
                        Text("Whole School", fontSize = 12.sp)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = targetType == "CLASS",
                            onClick = { targetType = "CLASS" },
                            modifier = Modifier.testTag("audience_class_radio")
                        )
                        Text("Class Wise", fontSize = 12.sp)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = targetType == "STUDENT",
                            onClick = { targetType = "STUDENT" },
                            modifier = Modifier.testTag("audience_student_radio")
                        )
                        Text("Separate Student", fontSize = 12.sp)
                    }
                }

                // Sub-selectors if Class or Student
                if (targetType == "CLASS") {
                    Box {
                        OutlinedButton(
                            onClick = { expandedClassDropdown = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Target Class: $selectedClass")
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }

                        DropdownMenu(
                            expanded = expandedClassDropdown,
                            onDismissRequest = { expandedClassDropdown = false }
                        ) {
                            classList.forEach { cls ->
                                DropdownMenuItem(
                                    text = { Text(cls) },
                                    onClick = {
                                        selectedClass = cls
                                        expandedClassDropdown = false
                                    }
                                )
                            }
                        }
                    }
                } else if (targetType == "STUDENT") {
                    Box {
                        val st = students.find { it.studentCode == selectedStudentCode }
                        OutlinedButton(
                            onClick = { expandedStudentDropdown = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Target Student: ${st?.name ?: selectedStudentCode} (${st?.className ?: ""})")
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }

                        DropdownMenu(
                            expanded = expandedStudentDropdown,
                            onDismissRequest = { expandedStudentDropdown = false }
                        ) {
                            students.forEach { student ->
                                DropdownMenuItem(
                                    text = { Text("${student.name} (${student.className} - ${student.studentCode})") },
                                    onClick = {
                                        selectedStudentCode = student.studentCode
                                        expandedStudentDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Notification Title *") },
                    placeholder = { Text("e.g. Science Test Scheduled") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("notif_title_input")
                )

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Notification Message Body *") },
                    placeholder = { Text("Enter detailed announcement text...") },
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("notif_message_input")
                )

                val targetValue = when (targetType) {
                    "CLASS" -> selectedClass
                    "STUDENT" -> selectedStudentCode
                    else -> "ALL"
                }

                Button(
                    onClick = {
                        if (title.isNotBlank() && message.isNotBlank()) {
                            onSendNotification(title, message, targetType, targetValue)
                            title = ""
                            message = ""
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("send_push_notification_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Dispatch Push Broadcast Now", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Broadcast History Log
        Text(
            text = "Broadcasted History Log",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (notificationsHistory.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No previous notifications sent.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(notificationsHistory, key = { it.id }) { item ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        text = "${item.targetType}: ${item.targetValue}",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = item.message, fontSize = 12.sp, color = Color.DarkGray)
                        }
                    }
                }
            }
        }
    }
}

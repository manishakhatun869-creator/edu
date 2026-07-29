package com.example.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Attendance
import com.example.data.Student
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAttendanceScreen(
    students: List<Student>,
    attendanceRecords: List<Attendance>,
    onSaveBatchAttendance: (Map<String, String>, String, String) -> Unit
) {
    val todayDateStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var selectedDate by remember { mutableStateOf(todayDateStr) }

    val classList = remember(students) {
        val list = students.map { it.className }.distinct().sorted()
        if (list.isEmpty()) listOf("Class 10-A") else list
    }
    var selectedClass by remember { mutableStateOf(classList.firstOrNull() ?: "Class 10-A") }

    val classStudents = remember(students, selectedClass) {
        students.filter { it.className == selectedClass }.sortedBy { it.rollNumber }
    }

    // Local mutable state map for editing attendance: studentCode -> Status ("PRESENT", "ABSENT", "LATE")
    val attendanceMap = remember(selectedClass, selectedDate, attendanceRecords) {
        val existingMap = attendanceRecords
            .filter { it.date == selectedDate }
            .associate { it.studentCode to it.status }

        val map = mutableStateMapOf<String, String>()
        classStudents.forEach { student ->
            map[student.studentCode] = existingMap[student.studentCode] ?: "PRESENT"
        }
        map
    }

    var showSavedMessage by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Attendance Register",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Mark daily attendance for class",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // Date selector
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.testTag("attendance_date_selector")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = selectedDate,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Class Selector Tabs
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(classList) { className ->
                FilterChip(
                    selected = selectedClass == className,
                    onClick = { selectedClass = className },
                    label = { Text(className, fontWeight = if (selectedClass == className) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Stats & Bulk Action Box
        val presentCount = attendanceMap.values.count { it == "PRESENT" }
        val lateCount = attendanceMap.values.count { it == "LATE" }
        val absentCount = attendanceMap.values.count { it == "ABSENT" }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatusSummaryChip("Present", presentCount, Color(0xFF10B981))
                        StatusSummaryChip("Late", lateCount, Color(0xFFF59E0B))
                        StatusSummaryChip("Absent", absentCount, Color(0xFFEF4444))
                    }

                    // Bulk Actions
                    Row {
                        TextButton(onClick = {
                            classStudents.forEach { attendanceMap[it.studentCode] = "PRESENT" }
                        }) {
                            Text("All Present", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Student Attendance List
        if (classStudents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No students enrolled in $selectedClass", color = Color.Gray)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(classStudents, key = { it.id }) { student ->
                    val currentStatus = attendanceMap[student.studentCode] ?: "PRESENT"

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = student.rollNumber,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = student.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = student.studentCode,
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            // 3-Way Segment Toggle
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.LightGray.copy(alpha = 0.2f))
                                    .padding(2.dp)
                            ) {
                                SegmentOption(
                                    label = "P",
                                    selected = currentStatus == "PRESENT",
                                    activeColor = Color(0xFF10B981),
                                    onClick = { attendanceMap[student.studentCode] = "PRESENT" }
                                )
                                SegmentOption(
                                    label = "L",
                                    selected = currentStatus == "LATE",
                                    activeColor = Color(0xFFF59E0B),
                                    onClick = { attendanceMap[student.studentCode] = "LATE" }
                                )
                                SegmentOption(
                                    label = "A",
                                    selected = currentStatus == "ABSENT",
                                    activeColor = Color(0xFFEF4444),
                                    onClick = { attendanceMap[student.studentCode] = "ABSENT" }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Save Attendance FAB / Button
        Button(
            onClick = {
                onSaveBatchAttendance(attendanceMap.toMap(), selectedDate, selectedClass)
                showSavedMessage = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("save_attendance_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(imageVector = Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Save Attendance & Dispatch Alerts", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
private fun StatusSummaryChip(label: String, count: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = "$label: ", fontSize = 11.sp, color = Color.Gray)
        Text(text = "$count", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun SegmentOption(
    label: String,
    selected: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) activeColor else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) Color.White else Color.Gray
        )
    }
}

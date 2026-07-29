package com.example.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Info
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
import com.example.ui.components.MonthCalendarView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentAttendanceScreen(
    attendance: List<Attendance>
) {
    var viewMode by remember { mutableStateOf("CALENDAR") } // "CALENDAR" or "LIST"
    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "PRESENT", "LATE", "ABSENT"

    var selectedDayDetail by remember { mutableStateOf<Pair<String, Attendance?>?>(null) }

    val presentCount = attendance.count { it.status == "PRESENT" }
    val lateCount = attendance.count { it.status == "LATE" }
    val absentCount = attendance.count { it.status == "ABSENT" }
    val totalRecords = attendance.size
    val attendancePct = if (totalRecords > 0) ((presentCount + lateCount) * 100) / totalRecords else 100

    val filteredList = remember(attendance, selectedFilter) {
        when (selectedFilter) {
            "PRESENT" -> attendance.filter { it.status == "PRESENT" }
            "LATE" -> attendance.filter { it.status == "LATE" }
            "ABSENT" -> attendance.filter { it.status == "ABSENT" }
            else -> attendance
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with View Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Attendance Records",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Calendar & status history",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            SingleChoiceSegmentedButtonRow {
                SegmentedButton(
                    selected = viewMode == "CALENDAR",
                    onClick = { viewMode = "CALENDAR" },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    modifier = Modifier.testTag("calendar_view_toggle"),
                    icon = { Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = "Calendar View", modifier = Modifier.size(16.dp)) },
                    label = { Text("Calendar", fontSize = 11.sp) }
                )
                SegmentedButton(
                    selected = viewMode == "LIST",
                    onClick = { viewMode = "LIST" },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    modifier = Modifier.testTag("list_view_toggle"),
                    icon = { Icon(imageVector = Icons.Default.FormatListNumbered, contentDescription = "List View", modifier = Modifier.size(16.dp)) },
                    label = { Text("List", fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Total Performance Overview Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Overall Percentage", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        text = "$attendancePct%",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (attendancePct >= 75) Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SummaryBadge("Present", presentCount, Color(0xFF10B981))
                    SummaryBadge("Late", lateCount, Color(0xFFF59E0B))
                    SummaryBadge("Absent", absentCount, Color(0xFFEF4444))
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (viewMode == "CALENDAR") {
            // Interactive Month Calendar Grid
            MonthCalendarView(
                attendanceList = attendance,
                onDayClick = { dateKey, record ->
                    selectedDayDetail = Pair(dateKey, record)
                }
            )
        } else {
            // Categorized List View
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "ALL" to "All ($totalRecords)",
                    "PRESENT" to "Present ($presentCount)",
                    "LATE" to "Late ($lateCount)",
                    "ABSENT" to "Absent ($absentCount)"
                ).forEach { (key, label) ->
                    FilterChip(
                        selected = selectedFilter == key,
                        onClick = { selectedFilter = key },
                        label = { Text(label, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No records for $selectedFilter", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredList, key = { it.id }) { item ->
                        val statusColor = when (item.status) {
                            "PRESENT" -> Color(0xFF10B981)
                            "LATE" -> Color(0xFFF59E0B)
                            else -> Color(0xFFEF4444)
                        }

                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = item.date,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    if (item.remarks.isNotBlank()) {
                                        Text(text = item.remarks, fontSize = 11.sp, color = Color.Gray)
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = statusColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = item.status,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Day Detail Dialog when calendar day clicked
    selectedDayDetail?.let { (date, record) ->
        AlertDialog(
            onDismissRequest = { selectedDayDetail = null },
            title = { Text(text = "Attendance for $date", fontWeight = FontWeight.Bold) },
            text = {
                if (record == null) {
                    Text(text = "No official attendance recorded for this date (e.g. Weekend or Holiday).")
                } else {
                    val color = when (record.status) {
                        "PRESENT" -> Color(0xFF10B981)
                        "LATE" -> Color(0xFFF59E0B)
                        else -> Color(0xFFEF4444)
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Status: ", fontWeight = FontWeight.Bold)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = color
                            ) {
                                Text(
                                    text = record.status,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        if (record.remarks.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "Remarks: ${record.remarks}", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedDayDetail = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun SummaryBadge(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "$count", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, fontSize = 10.sp, color = Color.Gray)
    }
}

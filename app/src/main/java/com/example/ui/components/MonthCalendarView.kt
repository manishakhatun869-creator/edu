package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Attendance
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MonthCalendarView(
    attendanceList: List<Attendance>,
    onDayClick: (String, Attendance?) -> Unit
) {
    var calendarInstance by remember { mutableStateOf(Calendar.getInstance()) }
    val monthYearFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val dayKeyFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    // Map attendance by date string "YYYY-MM-DD"
    val attendanceMap = remember(attendanceList) {
        attendanceList.associateBy { it.date }
    }

    // Calculations for current month view
    val displayMonth = remember(calendarInstance.time) { monthYearFormat.format(calendarInstance.time) }
    
    val daysInMonth = remember(calendarInstance.time) {
        val cal = calendarInstance.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0 for Sun
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        Pair(firstDayOfWeek, maxDays)
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Calendar Header: Month Navigator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        val cal = calendarInstance.clone() as Calendar
                        cal.add(Calendar.MONTH, -1)
                        calendarInstance = cal
                    }
                ) {
                    Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Previous Month")
                }

                Text(
                    text = displayMonth,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(
                    onClick = {
                        val cal = calendarInstance.clone() as Calendar
                        cal.add(Calendar.MONTH, 1)
                        calendarInstance = cal
                    }
                ) {
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next Month")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Weekday Headers
            val weekDays = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            Row(modifier = Modifier.fillMaxWidth()) {
                weekDays.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Days Grid
            val (offset, totalDays) = daysInMonth
            val totalCells = offset + totalDays

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(totalCells) { index ->
                    if (index < offset) {
                        // Empty slot before month start
                        Box(modifier = Modifier.aspectRatio(1f))
                    } else {
                        val dayNum = index - offset + 1
                        val dayCal = (calendarInstance.clone() as Calendar).apply {
                            set(Calendar.DAY_OF_MONTH, dayNum)
                        }
                        val dateKey = dayKeyFormat.format(dayCal.time)
                        val record = attendanceMap[dateKey]

                        val statusColor = when (record?.status) {
                            "PRESENT" -> Color(0xFF10B981) // Emerald Green
                            "LATE" -> Color(0xFFF59E0B)    // Amber Yellow
                            "ABSENT" -> Color(0xFFEF4444)  // Rose Red
                            else -> Color.Transparent
                        }

                        val isToday = remember(dateKey) {
                            dayKeyFormat.format(Date()) == dateKey
                        }

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (record != null) statusColor.copy(alpha = 0.15f)
                                    else if (isToday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                    else Color.Transparent
                                )
                                .border(
                                    width = if (isToday) 2.dp else 1.dp,
                                    color = if (isToday) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { onDayClick(dateKey, record) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = dayNum.toString(),
                                    fontSize = 12.sp,
                                    fontWeight = if (isToday || record != null) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                if (record != null) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(statusColor)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Attendance Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(color = Color(0xFF10B981), label = "Present")
                LegendItem(color = Color(0xFFF59E0B), label = "Late")
                LegendItem(color = Color(0xFFEF4444), label = "Absent")
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 11.sp, color = Color.Gray)
    }
}

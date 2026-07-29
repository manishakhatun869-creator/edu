package com.example.ui.admin

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FeeRecord
import com.example.data.Student

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminFeesScreen(
    students: List<Student>,
    fees: List<FeeRecord>,
    onCreateFeeInvoice: (studentCode: String, title: String, amount: Double, dueDate: String) -> Unit,
    onMarkFeePaid: (feeId: Int, paymentMethod: String) -> Unit
) {
    var selectedStatusTab by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var showCreateInvoiceDialog by remember { mutableStateOf(false) }
    var selectedFeeForReceipt by remember { mutableStateOf<FeeRecord?>(null) }

    val statusTabs = listOf("All", "Pending", "Paid", "Overdue")

    val studentMap = remember(students) { students.associateBy { it.studentCode } }

    val filteredFees = remember(fees, selectedStatusTab, searchQuery) {
        fees.filter { fee ->
            val matchesStatus = when (selectedStatusTab) {
                "All" -> true
                "Pending" -> fee.status == "PENDING"
                "Paid" -> fee.status == "PAID"
                "Overdue" -> fee.status == "OVERDUE"
                else -> true
            }

            val student = studentMap[fee.studentCode]
            val matchesSearch = searchQuery.isBlank() ||
                    fee.receiptNo.contains(searchQuery, ignoreCase = true) ||
                    fee.title.contains(searchQuery, ignoreCase = true) ||
                    fee.studentCode.contains(searchQuery, ignoreCase = true) ||
                    (student != null && student.name.contains(searchQuery, ignoreCase = true))

            matchesStatus && matchesSearch
        }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateInvoiceDialog = true },
                icon = { Icon(Icons.Default.AddCard, contentDescription = "Issue Invoice") },
                text = { Text("Issue Fee Invoice", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("issue_fee_fab")
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                        text = "Fees & Accounting",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Track fee collections & generate digital receipts",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                val totalCollected = fees.filter { it.status == "PAID" }.sumOf { it.amount }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFD1FAE5)
                ) {
                    Text(
                        text = "₹${String.format("%.0f", totalCollected)} Paid",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF047857)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search receipt #, title, or student...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Status Filter Tabs
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(statusTabs) { tab ->
                    FilterChip(
                        selected = selectedStatusTab == tab,
                        onClick = { selectedStatusTab = tab },
                        label = { Text(tab, fontWeight = if (selectedStatusTab == tab) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Fee List
            if (filteredFees.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No fee records found", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredFees, key = { it.id }) { fee ->
                        val student = studentMap[fee.studentCode]
                        val isPaid = fee.status == "PAID"

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = fee.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = "${student?.name ?: "Student"} • ${fee.studentCode}",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }

                                    // Amount
                                    Text(
                                        text = "₹${String.format("%.2f", fee.amount)}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 17.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Divider(color = Color.LightGray.copy(alpha = 0.3f))
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Receipt: ${fee.receiptNo}",
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color.Gray
                                        )
                                        Text(
                                            text = "Due: ${fee.dueDate}",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }

                                    // Status Badge & Action
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = when (fee.status) {
                                                "PAID" -> Color(0xFFD1FAE5)
                                                "PENDING" -> Color(0xFFFEF3C7)
                                                else -> Color(0xFFFEE2E2)
                                            }
                                        ) {
                                            Text(
                                                text = fee.status,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when (fee.status) {
                                                    "PAID" -> Color(0xFF047857)
                                                    "PENDING" -> Color(0xFFB45309)
                                                    else -> Color(0xFFB91C1C)
                                                }
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Button(
                                            onClick = { selectedFeeForReceipt = fee },
                                            modifier = Modifier.height(34.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isPaid) MaterialTheme.colorScheme.secondary else Color(0xFF10B981)
                                            )
                                        ) {
                                            Icon(imageVector = Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (isPaid) "View Receipt" else "Collect Fee", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Create Invoice Dialog
    if (showCreateInvoiceDialog) {
        CreateFeeInvoiceDialog(
            students = students,
            onDismiss = { showCreateInvoiceDialog = false },
            onConfirm = { code, title, amount, dueDate ->
                onCreateFeeInvoice(code, title, amount, dueDate)
                showCreateInvoiceDialog = false
            }
        )
    }

    // Receipt View / Collect Fee Dialog
    selectedFeeForReceipt?.let { fee ->
        FeeReceiptDialog(
            fee = fee,
            student = studentMap[fee.studentCode],
            onDismiss = { selectedFeeForReceipt = null },
            onMarkPaid = { paymentMethod ->
                onMarkFeePaid(fee.id, paymentMethod)
                selectedFeeForReceipt = null
            }
        )
    }
}

@Composable
fun CreateFeeInvoiceDialog(
    students: List<Student>,
    onDismiss: () -> Unit,
    onConfirm: (studentCode: String, title: String, amount: Double, dueDate: String) -> Unit
) {
    var selectedStudentCode by remember { mutableStateOf(students.firstOrNull()?.studentCode ?: "STU-1007") }
    var title by remember { mutableStateOf("August 2026 Tuition Fee (₹350)") }
    var amountStr by remember { mutableStateOf("350.00") }
    var dueDate by remember { mutableStateOf("2026-08-10") }

    var expandedStudentDropdown by remember { mutableStateOf(false) }
    val selectedStudent = students.find { it.studentCode == selectedStudentCode }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Issue Fee Invoice (₹350)", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = "Select Student:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Box {
                    OutlinedButton(
                        onClick = { expandedStudentDropdown = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "${selectedStudent?.name ?: selectedStudentCode} (${selectedStudent?.className ?: ""})")
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }

                    DropdownMenu(
                        expanded = expandedStudentDropdown,
                        onDismissRequest = { expandedStudentDropdown = false }
                    ) {
                        students.forEach { st ->
                            DropdownMenuItem(
                                text = { Text("${st.name} (${st.className} - ${st.studentCode})") },
                                onClick = {
                                    selectedStudentCode = st.studentCode
                                    expandedStudentDropdown = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Fee Description / Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount (₹ Indian Rupee)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Due Date (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull() ?: 350.0
                    onConfirm(selectedStudentCode, title, amount, dueDate)
                },
                modifier = Modifier.testTag("confirm_create_fee_button")
            ) {
                Text("Generate Invoice")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

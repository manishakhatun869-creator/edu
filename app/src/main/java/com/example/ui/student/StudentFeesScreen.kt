package com.example.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.Check
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
import com.example.ui.admin.FeeReceiptDialog

@Composable
fun StudentFeesScreen(
    student: Student?,
    fees: List<FeeRecord>,
    onPayFee: ((feeId: Int, paymentMethod: String) -> Unit)? = null
) {
    var selectedFeeForReceipt by remember { mutableStateOf<FeeRecord?>(null) }
    var selectedFeeForPayment by remember { mutableStateOf<FeeRecord?>(null) }
    var filterMode by remember { mutableStateOf("ALL") } // ALL, PAID, UNPAID

    val totalPaid = fees.filter { it.status == "PAID" }.sumOf { it.amount }
    val totalPending = fees.filter { it.status != "PAID" }.sumOf { it.amount }

    val paidCount = fees.count { it.status == "PAID" }
    val unpaidCount = fees.count { it.status != "PAID" }

    val filteredFees = remember(fees, filterMode) {
        when (filterMode) {
            "PAID" -> fees.filter { it.status == "PAID" }
            "UNPAID" -> fees.filter { it.status != "PAID" }
            else -> fees
        }
    }

    // Standard 12 Months mapping for clear month-wise breakdown
    val all12Months = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun", 
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Title Header
        Text(
            text = "12-Month Fee Tracker (₹350/mo)",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Check month-wise fee payment status for standard ₹350 monthly tuition",
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Financial Summary Box
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFD1FAE5)),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Paid ($paidCount Months)", fontSize = 11.sp, color = Color(0xFF065F46), fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₹${String.format("%.2f", totalPaid)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF047857)
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Unpaid ($unpaidCount Months)", fontSize = 11.sp, color = Color(0xFF78350F), fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.HourglassTop, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₹${String.format("%.2f", totalPending)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFB45309)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 12-Month Quick Status Grid Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFDF2F8)),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFFBCFE8), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Annual 12-Month Payment Status (2026):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF831843)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    all12Months.forEachIndexed { idx, mName ->
                        // Match month index to fee record
                        val feeForMonth = fees.find { it.title.contains(mName, ignoreCase = true) }
                            ?: fees.getOrNull(idx)
                        val isPaidMonth = feeForMonth?.status == "PAID"

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isPaidMonth) Color(0xFF10B981) else Color(0xFFEF4444)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isPaidMonth) {
                                    Icon(Icons.Outlined.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                } else {
                                    Text("!", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = mName, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B5563))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Month Filter Chips (ALL, PAID, UNPAID)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filterMode == "ALL",
                onClick = { filterMode = "ALL" },
                label = { Text("All 12 Months (${fees.size})") },
                modifier = Modifier.testTag("filter_all_months")
            )
            FilterChip(
                selected = filterMode == "PAID",
                onClick = { filterMode = "PAID" },
                label = { Text("Paid ($paidCount)") },
                modifier = Modifier.testTag("filter_paid_months")
            )
            FilterChip(
                selected = filterMode == "UNPAID",
                onClick = { filterMode = "UNPAID" },
                label = { Text("Unpaid ($unpaidCount)") },
                modifier = Modifier.testTag("filter_unpaid_months")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Month Fee Invoices List
        if (filteredFees.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No fee invoices match filter.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredFees, key = { it.id }) { fee ->
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (isPaid) Color(0xFFD1FAE5) else Color(0xFFFEE2E2)
                                        ) {
                                            Text(
                                                text = if (isPaid) "PAID" else "UNPAID",
                                                color = if (isPaid) Color(0xFF065F46) else Color(0xFF991B1B),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = fee.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color(0xFF1F2937)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Receipt #: ${fee.receiptNo}",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color.Gray
                                    )
                                }

                                Text(
                                    text = "₹${String.format("%.2f", fee.amount)}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = if (isPaid) Color(0xFF059669) else Color(0xFFDC2626)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = Color.LightGray.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isPaid) "Paid on: ${fee.paidDate} (${fee.paymentMethod ?: "Online"})" else "Due Date: ${fee.dueDate}",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (!isPaid && onPayFee != null) {
                                        Button(
                                            onClick = { selectedFeeForPayment = fee },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.testTag("pay_now_btn_${fee.id}")
                                        ) {
                                            Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = "Pay ₹350 Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Button(
                                        onClick = { selectedFeeForReceipt = fee },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isPaid) MaterialTheme.colorScheme.primary else Color(0xFF6B7280)
                                        ),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.testTag("view_receipt_button_${fee.receiptNo}")
                                    ) {
                                        Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = if (isPaid) "View Receipt" else "Details", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // View Receipt Modal Dialog
    selectedFeeForReceipt?.let { fee ->
        FeeReceiptDialog(
            fee = fee,
            student = student,
            onDismiss = { selectedFeeForReceipt = null }
        )
    }

    // Pay Fee Online Modal Dialog (UPI / Debit / Net Banking)
    selectedFeeForPayment?.let { fee ->
        var paymentMethodChoice by remember { mutableStateOf("UPI / PhonePe / GooglePay") }

        AlertDialog(
            onDismissRequest = { selectedFeeForPayment = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Payment, contentDescription = null, tint = Color(0xFF10B981))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pay Fee Invoice (₹350)", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Invoice: ${fee.title}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "Student: ${student?.name} (${student?.studentCode})", fontSize = 12.sp, color = Color.Gray)
                    Text(text = "Total Amount Due: ₹350.00 Indian Rupee", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF10B981))

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Select Payment Method:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    listOf("UPI / PhonePe / GooglePay", "Debit / Credit Card", "Net Banking", "Cash").forEach { method ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { paymentMethodChoice = method }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = paymentMethodChoice == method,
                                onClick = { paymentMethodChoice = method }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = method, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val feeToPay = fee
                        selectedFeeForPayment = null
                        onPayFee?.invoke(feeToPay.id, paymentMethodChoice)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Confirm Payment (₹350)", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedFeeForPayment = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}


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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Student

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStudentsScreen(
    students: List<Student>,
    onAddStudent: (name: String, className: String, rollNumber: String, parentContact: String, email: String) -> Unit,
    onUpdateStudent: (Student) -> Unit,
    onDeleteStudent: (Student) -> Unit,
    onSelectStudentForPortal: (String) -> Unit
) {
    var selectedClassTab by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var studentToEdit by remember { mutableStateOf<Student?>(null) }

    val clipboardManager = LocalClipboardManager.current
    var lastAddedCode by remember { mutableStateOf<String?>(null) }

    val classList = remember(students) {
        listOf("All") + students.map { it.className }.distinct().sorted()
    }

    val filteredStudents = remember(students, selectedClassTab, searchQuery) {
        students.filter { student ->
            val matchesClass = selectedClassTab == "All" || student.className == selectedClassTab
            val matchesSearch = searchQuery.isBlank() ||
                    student.name.contains(searchQuery, ignoreCase = true) ||
                    student.rollNumber.contains(searchQuery, ignoreCase = true) ||
                    student.studentCode.contains(searchQuery, ignoreCase = true)
            matchesClass && matchesSearch
        }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Student") },
                text = { Text("Add Student", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_student_fab")
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Screen Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Student Directory",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Manage profiles & auto-generated student codes",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${filteredStudents.size} Students",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by name, roll, or STU code...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("student_search_input"),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Class Tabs Filter
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(classList) { tab ->
                    FilterChip(
                        selected = selectedClassTab == tab,
                        onClick = { selectedClassTab = tab },
                        label = { Text(tab, fontWeight = if (selectedClassTab == tab) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Success banner after student code generated
            if (lastAddedCode != null) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFD1FAE5)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Student Added Successfully!", fontWeight = FontWeight.Bold, color = Color(0xFF047857), fontSize = 13.sp)
                            Text(text = "Generated Code: $lastAddedCode (Provide this to student to log in)", fontSize = 11.sp, color = Color(0xFF065F46))
                        }
                        IconButton(onClick = { lastAddedCode = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color(0xFF047857))
                        }
                    }
                }
            }

            // Student List
            if (filteredStudents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PersonOff,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "No students found", color = Color.Gray, fontWeight = FontWeight.Medium)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredStudents, key = { it.id }) { student ->
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
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            modifier = Modifier.size(42.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = student.name.take(1).uppercase(),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 18.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = student.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                            Text(
                                                text = "${student.className} • Roll No: ${student.rollNumber}",
                                                fontSize = 12.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }

                                    // Student Code Badge
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.tertiaryContainer
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = student.studentCode,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copy Code",
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .testTag("copy_code_${student.studentCode}"),
                                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                                            )
                                        }
                                    }
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
                                        text = "Parent: ${if (student.parentContact.isNotBlank()) student.parentContact else "N/A"}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )

                                    Row {
                                        IconButton(
                                            onClick = { onSelectStudentForPortal(student.studentCode) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Login, contentDescription = "Portal View", tint = MaterialTheme.colorScheme.primary)
                                        }
                                        IconButton(
                                            onClick = { studentToEdit = student },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit Student", tint = Color(0xFF2563EB))
                                        }
                                        IconButton(
                                            onClick = { onDeleteStudent(student) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete Student", tint = Color(0xFFEF4444))
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

    // Add Student Dialog
    if (showAddDialog) {
        AddOrEditStudentDialog(
            isEdit = false,
            initialStudent = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, className, roll, parent, email ->
                onAddStudent(name, className, roll, parent, email)
                showAddDialog = false
            }
        )
    }

    // Edit Student Dialog
    if (studentToEdit != null) {
        AddOrEditStudentDialog(
            isEdit = true,
            initialStudent = studentToEdit,
            onDismiss = { studentToEdit = null },
            onConfirm = { name, className, roll, parent, email ->
                studentToEdit?.let { existing ->
                    onUpdateStudent(
                        existing.copy(
                            name = name,
                            className = className,
                            rollNumber = roll,
                            parentContact = parent,
                            email = email
                        )
                    )
                }
                studentToEdit = null
            }
        )
    }
}

@Composable
fun AddOrEditStudentDialog(
    isEdit: Boolean,
    initialStudent: Student?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, className: String, rollNumber: String, parentContact: String, email: String) -> Unit
) {
    var name by remember { mutableStateOf(initialStudent?.name ?: "") }
    var className by remember { mutableStateOf(initialStudent?.className ?: "Class 10-A") }
    var rollNumber by remember { mutableStateOf(initialStudent?.rollNumber ?: "") }
    var parentContact by remember { mutableStateOf(initialStudent?.parentContact ?: "") }
    var email by remember { mutableStateOf(initialStudent?.email ?: "") }

    var nameError by remember { mutableStateOf(false) }
    var rollError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEdit) "Edit Student Details" else "Add New Student",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!isEdit) {
                    Text(
                        text = "Enter Name, Class & Roll Number. A unique Student Login Code will be generated automatically.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text("Student Name *") },
                    isError = nameError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_student_name_input")
                )

                OutlinedTextField(
                    value = className,
                    onValueChange = { className = it },
                    label = { Text("Class Name * (e.g. Class 10-A)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_student_class_input")
                )

                OutlinedTextField(
                    value = rollNumber,
                    onValueChange = {
                        rollNumber = it
                        rollError = false
                    },
                    label = { Text("Roll Number * (e.g. 05)") },
                    isError = rollError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_student_roll_input")
                )

                OutlinedTextField(
                    value = parentContact,
                    onValueChange = { parentContact = it },
                    label = { Text("Parent Contact Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_student_parent_input")
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Student / Parent Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_student_email_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) nameError = true
                    if (rollNumber.isBlank()) rollError = true
                    if (name.isNotBlank() && rollNumber.isNotBlank()) {
                        onConfirm(name, className, rollNumber, parentContact, email)
                    }
                },
                modifier = Modifier.testTag("save_student_button")
            ) {
                Text(if (isEdit) "Update Profile" else "Create & Auto-Generate Code")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

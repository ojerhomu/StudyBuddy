package com.example.studybuddy.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.studybuddy.network.RetrofitInstance
import com.example.studybuddy.network.TaskRequest
import com.example.studybuddy.ui.OnboardingViewModel
import com.example.studybuddy.ui.components.DatePickerDialog
import com.example.studybuddy.ui.components.TimePickerDialog
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(onTaskAdded: () -> Unit, onboardingViewModel: OnboardingViewModel) { // FIX: Make ViewModel a required parameter
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskDescription by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf<LocalDate?>(null) }
    var dueTime by remember { mutableStateOf<LocalTime?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var selectedSubject by remember { mutableStateOf<String?>(null) }
    val subjects = onboardingViewModel.subjectDetailsMap.keys.plus("General")

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // dialogs
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            onDateSelected = { date ->
                dueDate = date
                showDatePicker = false
                showTimePicker = true
            }
        )
    }
    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            onTimeSelected = { time ->
                dueTime = time
                showTimePicker = false
            }
        )
    }

    // MAIN UI
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = newTaskTitle,
            onValueChange = { newTaskTitle = it },
            label = { Text("New Task Title") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = newTaskDescription,
            onValueChange = { newTaskDescription = it },
            label = { Text("Description (Optional)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = selectedSubject ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Subject") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                subjects.forEach { subject ->
                    DropdownMenuItem(
                        text = { Text(subject) },
                        onClick = {
                            selectedSubject = subject
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val dueDateTimeText = if (dueDate != null && dueTime != null) {
            "Due: ${dueDate!!.atTime(dueTime!!).format(DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a"))}"
        } else {
            "No due date set"
        }
        Text(dueDateTimeText)
        Spacer(modifier = Modifier.height(8.dp))

        Row {
            Button(onClick = { showDatePicker = true }, modifier = Modifier.weight(1f)) { Text("Set Due Date") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                if (newTaskTitle.isNotBlank()) {
                    coroutineScope.launch {
                        val dueDateTimeStr = if (dueDate != null && dueTime != null) {
                            dueDate!!.atTime(dueTime!!).format(DateTimeFormatter.ISO_DATE_TIME)
                        } else { null }
                        val subjectToSend = if (selectedSubject == "General") null else selectedSubject
                        val resp = RetrofitInstance.getAuthApi(context).createTask(TaskRequest(newTaskTitle, newTaskDescription.ifBlank { null }, dueDateTimeStr, subjectToSend))
                        if (resp.isSuccessful) {
                            Toast.makeText(context, "Task Added!", Toast.LENGTH_SHORT).show()
                            onTaskAdded()
                        } else {
                            Toast.makeText(context, "Failed to create task: ${resp.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }, modifier = Modifier.weight(1f)) { Text("Add Task") }
        }
    }
}
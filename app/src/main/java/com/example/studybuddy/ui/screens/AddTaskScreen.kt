package com.example.studybuddy.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.studybuddy.network.RetrofitInstance
import com.example.studybuddy.network.TaskRequest
import com.example.studybuddy.ui.components.DatePickerDialog
import com.example.studybuddy.ui.components.TimePickerDialog
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun AddTaskScreen(onTaskAdded: () -> Unit) {
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskDescription by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf<LocalDate?>(null) }
    var dueTime by remember { mutableStateOf<LocalTime?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // dialog
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

    // --- Main UI ---
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
                        val resp = RetrofitInstance.getInstance(context).createTask(TaskRequest(newTaskTitle, newTaskDescription.ifBlank { null }, dueDateTimeStr))
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

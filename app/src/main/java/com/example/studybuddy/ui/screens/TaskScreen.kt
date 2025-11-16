package com.example.studybuddy.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.studybuddy.network.RetrofitInstance
import com.example.studybuddy.network.Task
import com.example.studybuddy.network.TaskRequest
import com.example.studybuddy.ui.components.DatePickerDialog
import com.example.studybuddy.ui.components.TimePickerDialog
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime // Add the missing import
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun TaskScreen() {
    val context = LocalContext.current
    var tasks by remember { mutableStateOf(listOf<Task>()) }
    var errorMessage by remember { mutableStateOf("") }
    var taskToDelete by remember { mutableStateOf<Task?>(null) }

    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskDescription by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf<LocalDate?>(null) }
    var dueTime by remember { mutableStateOf<LocalTime?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // --- Dialogs ---
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
    taskToDelete?.let { task ->
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            title = { Text("Delete Task?") },
            text = { Text("Are you sure you want to delete this task?") },
            confirmButton = {
                Button(onClick = {
                    coroutineScope.launch {
                        val response = RetrofitInstance.getInstance(context).deleteTask(task.id)
                        if (response.isSuccessful) {
                            tasks = tasks.filter { it.id != task.id }
                        } else {
                            errorMessage = "Failed to delete task: ${response.code()}"
                        }
                        taskToDelete = null
                    }
                }) { Text("Yes") }
            },
            dismissButton = {
                Button(onClick = { taskToDelete = null }) { Text("No") }
            }
        )
    }

    // MAIN
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Assignments and Tasks", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        if (errorMessage.isNotEmpty()) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        }

        // TASK LIST
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(tasks) { task ->
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(task.title, style = MaterialTheme.typography.bodyLarge)
                        IconButton(onClick = { taskToDelete = task }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Task")
                        }
                    }
                    task.description?.let {
                        Text(it, style = MaterialTheme.typography.bodyMedium)
                    }
                    task.due_date?.let {
                        val formattedDate = LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME)
                            .format(DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a"))
                        Text("Due: $formattedDate", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Divider()
            }
        }

        Spacer(Modifier.height(16.dp))

        //NEW TASK
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
                            val getTasksResp = RetrofitInstance.getInstance(context).getTasks()
                            if (getTasksResp.isSuccessful) { tasks = getTasksResp.body() ?: emptyList() }
                            newTaskTitle = ""; newTaskDescription = ""; dueDate = null; dueTime = null
                        } else { errorMessage = "Failed to create task: ${resp.code()}" }
                    }
                }
            }, modifier = Modifier.weight(1f)) { Text("Add Task") }
        }
    }
}
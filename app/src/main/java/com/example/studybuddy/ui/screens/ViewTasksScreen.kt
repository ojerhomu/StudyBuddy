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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.studybuddy.network.RetrofitInstance
import com.example.studybuddy.network.Task
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun ViewTasksScreen() {
    val context = LocalContext.current
    var tasks by remember { mutableStateOf(listOf<Task>()) }
    var errorMessage by remember { mutableStateOf("") }
    var taskToDelete by remember { mutableStateOf<Task?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                coroutineScope.launch {
                    val response = RetrofitInstance.getAuthApi(context).getTasks()
                    if (response.isSuccessful) {
                        tasks = response.body() ?: emptyList()
                    } else {
                        errorMessage = "Failed to load tasks: ${response.code()}"
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    taskToDelete?.let { task ->
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            title = { Text("Delete Task?") },
            text = { Text("Are you sure you want to delete '${task.title}'?") },
            confirmButton = {
                Button(onClick = {
                    coroutineScope.launch {
                        val response = RetrofitInstance.getAuthApi(context).deleteTask(task.id)
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

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (errorMessage.isNotEmpty()) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        }

        LazyColumn {
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
    }
}
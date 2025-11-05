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
import kotlinx.coroutines.launch

@Composable
fun TaskScreen() {
    val context = LocalContext.current
    var tasks by remember { mutableStateOf(listOf<Task>()) }
    var errorMessage by remember { mutableStateOf("") }
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskDescription by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    // Load tasks
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val response = RetrofitInstance.getInstance(context).getTasks()
                if (response.isSuccessful && response.body() != null) {
                    tasks = response.body()!!
                } else {
                    errorMessage = "Failed to load tasks: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Assignments and Tasks", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        if (errorMessage.isNotEmpty()) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        }

        // Display tasks in a scrollable list
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(tasks) { task ->
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(task.title, style = MaterialTheme.typography.bodyLarge)
                        IconButton(onClick = {
                            coroutineScope.launch {
                                try {
                                    val deleteResp = RetrofitInstance.getInstance(context).deleteTask(task.id)
                                    if (deleteResp.isSuccessful) {
                                        tasks = tasks.filter { it.id != task.id }
                                    } else {
                                        errorMessage = "Failed to delete task: ${deleteResp.code()}"
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Error deleting task: ${e.message}"
                                }
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Task")
                        }
                    }
                    task.description?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Divider()
            }
        }

        Spacer(Modifier.height(16.dp))

        // Add new task
        OutlinedTextField(
            value = newTaskTitle,
            onValueChange = { newTaskTitle = it },
            label = { Text("New Task Title") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = newTaskDescription,
            onValueChange = { newTaskDescription = it },
            label = { Text("Description (Optional)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                if (newTaskTitle.isNotBlank()) {
                    coroutineScope.launch {
                        try {
                            val resp = RetrofitInstance.getInstance(context).createTask(
                                TaskRequest(
                                    title = newTaskTitle,
                                    description = newTaskDescription.ifBlank { null } // Send null if description is empty
                                )
                            )
                            if (resp.isSuccessful && resp.body() != null) {
                                val createdTask = resp.body()!!
                                tasks = tasks + createdTask
                                newTaskTitle = ""
                                newTaskDescription = ""
                            } else {
                                errorMessage = "Failed to create task: ${resp.code()}"
                            }
                        } catch (e: Exception) {
                            errorMessage = "Error creating task: ${e.message}"
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Task")
        }
    }
}
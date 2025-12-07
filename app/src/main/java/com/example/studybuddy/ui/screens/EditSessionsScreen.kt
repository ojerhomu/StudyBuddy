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
import com.example.studybuddy.network.CalendarEvent
import com.example.studybuddy.network.RetrofitInstance
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun EditSessionsScreen() {
    val context = LocalContext.current
    var studySessions by remember { mutableStateOf<List<CalendarEvent>>(emptyList()) }
    var errorMessage by remember { mutableStateOf("") }
    var sessionToDelete by remember { mutableStateOf<CalendarEvent?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                coroutineScope.launch {
                    val response = RetrofitInstance.getAuthApi(context).getEvents()
                    if (response.isSuccessful) {
                        studySessions = response.body()?.filter { it.event_type == "STUDY_SESSION" } ?: emptyList()
                    } else {
                        errorMessage = "Failed to load sessions: ${response.code()}"
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    sessionToDelete?.let { session ->
        AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            title = { Text("Delete Session?") },
            text = { Text("Are you sure you want to delete '${session.title}'?") },
            confirmButton = {
                Button(onClick = {
                    coroutineScope.launch {
                        val response = RetrofitInstance.getAuthApi(context).deleteEvent(session.id)
                        if (response.isSuccessful) {
                            studySessions = studySessions.filter { it.id != session.id }
                        } else {
                            errorMessage = "Failed to delete session: ${response.code()}"
                        }
                        sessionToDelete = null
                    }
                }) { Text("Yes") }
            },
            dismissButton = {
                Button(onClick = { sessionToDelete = null }) { Text("No") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("My Study Sessions", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        if (errorMessage.isNotEmpty()) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        }

        LazyColumn {
            items(studySessions) { session ->
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(session.title, style = MaterialTheme.typography.bodyLarge)
                        IconButton(onClick = { sessionToDelete = session }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Session")
                        }
                    }
                    val formattedDate = LocalDateTime.parse(session.start_time, DateTimeFormatter.ISO_DATE_TIME)
                        .format(DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a"))
                    Text("Scheduled for: $formattedDate", style = MaterialTheme.typography.bodyMedium)
                }
                Divider()
            }
        }
    }
}
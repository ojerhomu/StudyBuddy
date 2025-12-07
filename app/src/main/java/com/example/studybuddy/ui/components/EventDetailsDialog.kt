package com.example.studybuddy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.studybuddy.network.CalendarEvent
import com.example.studybuddy.network.SubjectDetails
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun EventDetailsDialog( //uhhh did i mess this up??
    event: CalendarEvent,
    subjectDetails: SubjectDetails?,
    onDismissRequest: () -> Unit,
    onDelete: (CalendarEvent) -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Event?") },
            text = { Text("Are you sure you want to delete this event?") },
            confirmButton = {
                Button(onClick = { 
                    onDelete(event)
                    showDeleteConfirmation = false 
                }) { Text("Yes") }
            },
            dismissButton = { Button(onClick = { showDeleteConfirmation = false }) { Text("No") } }
        )
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Box {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(24.dp)
            ) {
                Text(event.title, style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))

                val startTime = LocalDateTime.parse(event.start_time, DateTimeFormatter.ISO_DATE_TIME)
                Text("Due: ${startTime.format(DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a"))}")
                
                if(event.subject != null) {
                    Text("Class: ${event.subject}")
                }
                
                Spacer(modifier = Modifier.height(8.dp))

                event.description?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium)
                }
            }
            IconButton(onClick = { showDeleteConfirmation = true }, modifier = Modifier.align(Alignment.TopEnd)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Event")
            }
        }
    }
}
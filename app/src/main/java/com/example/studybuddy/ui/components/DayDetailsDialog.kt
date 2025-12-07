package com.example.studybuddy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.studybuddy.network.CalendarEvent
import com.example.studybuddy.network.ScheduleInfo
import com.example.studybuddy.network.SubjectDetails
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun DayDetailsDialog(
    date: java.time.LocalDate,
    events: List<CalendarEvent>,
    classSchedule: List<Pair<String, ScheduleInfo>>,
    subjectDetailsMap: Map<String, SubjectDetails>,
    onDismissRequest: () -> Unit,
    onDeleteEvent: (CalendarEvent) -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(24.dp)
        ) {
            Text(
                text = date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")),
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(16.dp))

            // --- Events Section ---
            Text("Events & Tasks", style = MaterialTheme.typography.titleMedium)
            LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                if (events.isEmpty()) {
                    item { Text("No events or tasks due today.", style = MaterialTheme.typography.bodySmall) }
                } else {
                    items(events) { event ->
                        val colorString = subjectDetailsMap[event.subject]?.color ?: "#888888"
                        val color = try { Color(android.graphics.Color.parseColor(colorString)) } catch (e: Exception) { Color.Gray }
                        Column(
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(color.copy(alpha = 0.2f))
                                .padding(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(event.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                IconButton(onClick = { onDeleteEvent(event) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Event")
                                }
                            }
                            val timeFormatter = if (event.event_type == "TASK_DUE_DATE") {
                                DateTimeFormatter.ofPattern("\'Due at\' h:mm a")
                            } else {
                                DateTimeFormatter.ofPattern("h:mm a")
                            }
                            Text(LocalDateTime.parse(event.start_time).format(timeFormatter), style = MaterialTheme.typography.bodySmall)
                            event.subject?.let { Text("For: $it", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold) }
                            event.description?.let { Text(it, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp)) }
                        }
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // --- Class Schedule Section ---
            Text("Today\'s Classes", style = MaterialTheme.typography.titleMedium)
            LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                 if (classSchedule.isEmpty()) {
                    item { Text("No classes scheduled for today.", style = MaterialTheme.typography.bodySmall) }
                } else {
                    items(classSchedule) { (subject, schedule) ->
                        val colorString = subjectDetailsMap[subject]?.color ?: "#888888"
                        val color = try { Color(android.graphics.Color.parseColor(colorString)) } catch (e: Exception) { Color.Gray }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(color.copy(alpha = 0.2f))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(subject, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            if (schedule.startTime != null && schedule.endTime != null) {
                                val startTime = LocalTime.parse(schedule.startTime, DateTimeFormatter.ofPattern("H:mm"))
                                val endTime = LocalTime.parse(schedule.endTime, DateTimeFormatter.ofPattern("H:mm"))
                                Text("${startTime.format(DateTimeFormatter.ofPattern("h:mm a"))} - ${endTime.format(DateTimeFormatter.ofPattern("h:mm a"))}")
                            }
                        }
                    }
                }
            }
        }
    }
}
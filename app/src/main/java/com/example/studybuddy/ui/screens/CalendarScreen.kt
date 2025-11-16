package com.example.studybuddy.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.studybuddy.network.CalendarEvent
import com.example.studybuddy.network.RetrofitInstance
import com.example.studybuddy.ui.components.CalendarView
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun CalendarScreen() {
    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }
    var combinedEvents by remember { mutableStateOf<List<CalendarEvent>>(emptyList()) }
    var errorMessage by remember { mutableStateOf("") }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    // trigger a refresh whenever the user returns to the screen
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                coroutineScope.launch {
                    try {
                        val eventsDeferred = async { RetrofitInstance.getInstance(context).getEvents() }
                        val tasksDeferred = async { RetrofitInstance.getInstance(context).getTasks() }

                        val eventsResponse = eventsDeferred.await()
                        val tasksResponse = tasksDeferred.await()

                        if (eventsResponse.isSuccessful && tasksResponse.isSuccessful) {
                            val events = eventsResponse.body() ?: emptyList()
                            val tasks = tasksResponse.body() ?: emptyList()

                            val taskEvents = tasks.mapNotNull { task ->
                                task.due_date?.let {
                                    CalendarEvent(
                                        id = task.id,
                                        title = task.title,
                                        description = task.description,
                                        start_time = it,
                                        end_time = it,
                                        event_type = "TASK_DUE_DATE",
                                        owner_id = 0,
                                        created_at = ""
                                    )
                                }
                            }
                            combinedEvents = events + taskEvents
                            errorMessage = ""
                        } else {
                            errorMessage = "Failed to load calendar data"
                        }
                    } catch (e: Exception) {
                        errorMessage = "Error: ${e.message}"
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // filter events locally for the currently displayed month
    val eventsForMonth = combinedEvents.filter {
        val eventDateTime = LocalDate.parse(it.start_time, DateTimeFormatter.ISO_DATE_TIME)
        YearMonth.from(eventDateTime) == currentYearMonth
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("My Calendar", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        }

        CalendarView(
            yearMonth = currentYearMonth,
            events = eventsForMonth, // Pass the combined and filtered list
            onPreviousMonth = { currentYearMonth = currentYearMonth.minusMonths(1) },
            onNextMonth = { currentYearMonth = currentYearMonth.plusMonths(1) },
            onDateSelected = { /* TODO: Handle date selection */ }
        )
    }
}
package com.example.studybuddy.ui.screens

import android.util.Log
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.studybuddy.network.CalendarEvent
import com.example.studybuddy.network.RetrofitInstance
import com.example.studybuddy.ui.OnboardingViewModel
import com.example.studybuddy.ui.components.CalendarView
import com.example.studybuddy.ui.components.DayDetailsDialog
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Composable
fun CalendarScreen(onboardingViewModel: OnboardingViewModel) {
    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }
    var combinedEvents by remember { mutableStateOf<List<CalendarEvent>>(emptyList()) }
    var errorMessage by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                coroutineScope.launch {
                    try {
                        val authApi = RetrofitInstance.getAuthApi(context)
                        val eventsDeferred = async { authApi.getEvents() }
                        val tasksDeferred = async { authApi.getTasks() }

                        val eventsResponse = eventsDeferred.await()
                        val tasksResponse = tasksDeferred.await()

                        if (eventsResponse.isSuccessful && tasksResponse.isSuccessful) {
                            val events = eventsResponse.body() ?: emptyList()
                            val tasks = tasksResponse.body() ?: emptyList()
                            val taskEvents = tasks.mapNotNull { task ->
                                task.due_date?.let {
                                    CalendarEvent(task.id, task.title, task.description, it, it, "TASK_DUE_DATE", 0, "", task.subject)
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
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // day details
    selectedDate?.let {
        date ->
        val eventsForDay = combinedEvents.filter {
            try {
                LocalDate.parse(it.start_time, DateTimeFormatter.ISO_DATE_TIME).isEqual(date) 
            } catch (e: DateTimeParseException) {
                false
            }
        }
        val classScheduleForDay = onboardingViewModel.subjectDetailsMap.flatMap { (subject, details) ->
            details.schedule.filter { it.day == date.dayOfWeek }.map { Pair(subject, it) }
        }

        DayDetailsDialog(
            date = date,
            events = eventsForDay,
            classSchedule = classScheduleForDay,
            subjectDetailsMap = onboardingViewModel.subjectDetailsMap,
            onDismissRequest = { selectedDate = null },
            onDeleteEvent = { eventToDelete ->
                coroutineScope.launch {
                    val response = if (eventToDelete.event_type == "TASK_DUE_DATE") {
                        RetrofitInstance.getAuthApi(context).deleteTask(eventToDelete.id)
                    } else {
                        RetrofitInstance.getAuthApi(context).deleteEvent(eventToDelete.id)
                    }
                    if (response.isSuccessful) {
                        combinedEvents = combinedEvents.filter { it.id != eventToDelete.id }
                    }
                }
            }
        )
    }

    val eventsForMonth = combinedEvents.filter { 
        try {
            YearMonth.from(LocalDate.parse(it.start_time, DateTimeFormatter.ISO_DATE_TIME)) == currentYearMonth
        } catch(e: DateTimeParseException) {
            Log.e("CalendarScreen", "Failed to parse event start_time for month filter: ${it.start_time}", e) //handle error for debug
            false
        }
     }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("My Calendar", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        }

        CalendarView(
            yearMonth = currentYearMonth,
            events = eventsForMonth,
            onPreviousMonth = { currentYearMonth = currentYearMonth.minusMonths(1) },
            onNextMonth = { currentYearMonth = currentYearMonth.plusMonths(1) },
            onDateSelected = { date -> selectedDate = date },
            onEventSelected = { event -> 
                try {
                    selectedDate = LocalDate.parse(event.start_time, DateTimeFormatter.ISO_DATE_TIME)
                } catch (e: DateTimeParseException) {
                    // do nothing if the date is invalid
                }
            },
            subjectDetailsMap = onboardingViewModel.subjectDetailsMap,
            classSchedule = onboardingViewModel.subjectDetailsMap
        )
    }
}
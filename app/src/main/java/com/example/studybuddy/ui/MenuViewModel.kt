package com.example.studybuddy.ui

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studybuddy.network.CalendarEvent
import com.example.studybuddy.network.RetrofitInstance
import com.example.studybuddy.network.SubjectDetails
import com.example.studybuddy.network.Task
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

sealed class UpcomingDisplayItem {
    data class DatedItem(val dateTime: LocalDateTime, val description: String) : UpcomingDisplayItem()
    data class ClassItem(val subject: String, val startTime: LocalTime, val endTime: LocalTime) : UpcomingDisplayItem()
}

class MenuViewModel : ViewModel() {


    private val _greeting = mutableStateOf("")
    val greeting: State<String> = _greeting

    private val _soonestTask = mutableStateOf<UpcomingDisplayItem.DatedItem?>(null)
    val soonestTask: State<UpcomingDisplayItem.DatedItem?> = _soonestTask

    private val _todaysClasses = mutableStateOf<List<UpcomingDisplayItem.ClassItem>>(emptyList())
    val todaysClasses: State<List<UpcomingDisplayItem.ClassItem>> = _todaysClasses

    private val _todaysStudySessions = mutableStateOf<List<UpcomingDisplayItem.DatedItem>>(emptyList())
    val todaysStudySessions: State<List<UpcomingDisplayItem.DatedItem>> = _todaysStudySessions

    // dont spam ai
    private var hasLoadedOnce = false

    fun refreshData(
        context: Context,
        subjectDetails: Map<String, SubjectDetails>,
        userName: String?
    ) {
        //call refresh explicitly, allow reloading
        hasLoadedOnce = false
        loadData(context, subjectDetails, userName)
    }

    fun loadData(
        context: Context,
        subjectDetails: Map<String, SubjectDetails>,
        userName: String?
    ) {
        //  already loaded once and have a greeting, don't do it again
        if (hasLoadedOnce && _greeting.value.isNotBlank()) {
            return
        }
        hasLoadedOnce = true

        viewModelScope.launch {
            try {
                val authApi = RetrofitInstance.getAuthApi(context)

                val tasksDeferred = async { authApi.getTasks() }
                val eventsDeferred = async { authApi.getEvents() }

                // gen greeting once
                if (_greeting.value.isBlank()) {
                    // In MenuViewModel.kt, inside the loadData function

                    val greetingText = try {
                        val model = Firebase
                            .ai(backend = GenerativeBackend.googleAI())
                            .generativeModel("gemini-2.5-flash")

                        val namePrompt =
                            if (userName != null) "for a student named $userName" else "for a student"
                        val prompt =
                            "Write a short, upbeat, one-sentence greeting $namePrompt. " +
                                    "Every so often, include a clever and encouraging pun about studying or learning. " +
                                    "Keep it concise." + "Generate the greeting one time"

                        val response = model.generateContent(prompt)

                        // fallback
                        response.text?.takeIf { it.isNotBlank() } ?: "Welcome back!"

                    } catch (e: Exception) {
                        Log.e("MenuViewModel", "AI Greeting failed: ", e)
                        if (userName != null) "Welcome back, $userName!"
                        else "Welcome back! Let's get to it!"
                    }
                    _greeting.value = greetingText

                }

                val tasksResponse = tasksDeferred.await()
                val eventsResponse = eventsDeferred.await()

                if (tasksResponse.isSuccessful && eventsResponse.isSuccessful) {
                    val now = LocalDateTime.now()
                    val tasks = tasksResponse.body() ?: emptyList()
                    val events = eventsResponse.body() ?: emptyList()

                    _soonestTask.value = findSoonestTask(tasks, now)
                    _todaysClasses.value = findTodaysClasses(subjectDetails, now.dayOfWeek)
                    _todaysStudySessions.value = findTodaysStudySessions(events, now.toLocalDate())
                } else {
                    _soonestTask.value = null
                    _todaysClasses.value = emptyList()
                    _todaysStudySessions.value = emptyList()
                }

            } catch (e: Exception) {
                Log.e("MenuViewModel", "Data loading failed: ", e)
                if (_greeting.value.isBlank()) {
                    _greeting.value = "Welcome back! You've got this!"
                }
                _soonestTask.value = null
                _todaysClasses.value = emptyList()
                _todaysStudySessions.value = emptyList()
            }
        }
    }

    private fun findSoonestTask(
        tasks: List<Task>,
        now: LocalDateTime
    ): UpcomingDisplayItem.DatedItem? {
        return tasks
            .mapNotNull { task ->
                try {
                    task.due_date?.let {
                        Pair(task, LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME))
                    }
                } catch (e: DateTimeParseException) {
                    Log.e("MenuViewModel", "Failed to parse task due_date: ${task.due_date}", e)
                    null
                }
            }
            .filter { it.second.isAfter(now) }
            .minByOrNull { it.second }
            ?.let { (task, dateTime) ->
                UpcomingDisplayItem.DatedItem(dateTime, "Task: ${task.title}")
            }
    }

    private fun findTodaysStudySessions(
        events: List<CalendarEvent>,
        today: LocalDate
    ): List<UpcomingDisplayItem.DatedItem> {
        return events
            .filter { it.event_type == "STUDY_SESSION" }
            .mapNotNull { event ->
                try {
                    val eventDateTime =
                        LocalDateTime.parse(event.start_time, DateTimeFormatter.ISO_DATE_TIME)
                    if (eventDateTime.toLocalDate() == today) {
                        UpcomingDisplayItem.DatedItem(eventDateTime, event.title)
                    } else {
                        null
                    }
                } catch (e: DateTimeParseException) {
                    Log.e("MenuViewModel", "Failed to parse study session start_time: ${event.start_time}", e)
                    null
                }
            }
            .sortedBy { it.dateTime }
    }

    private fun findTodaysClasses(
        subjectDetails: Map<String, SubjectDetails>,
        today: DayOfWeek
    ): List<UpcomingDisplayItem.ClassItem> {
        return subjectDetails
            .flatMap { (subject, details) ->
                details.schedule
                    .filter { it.day == today }
                    .mapNotNull { scheduleInfo ->
                        if (scheduleInfo.startTime != null && scheduleInfo.endTime != null) {
                            try {
                                val startTime = LocalTime.parse(scheduleInfo.startTime)
                                val endTime = LocalTime.parse(scheduleInfo.endTime)
                                UpcomingDisplayItem.ClassItem(subject, startTime, endTime)
                            } catch (e: DateTimeParseException) {
                                Log.e(
                                    "MenuViewModel",
                                    "Failed to parse class time: ${scheduleInfo.startTime} or ${scheduleInfo.endTime}",
                                    e
                                )
                                null
                            }
                        } else {
                            null
                        }
                    }
            }
            .sortedBy { it.startTime }
    }
}

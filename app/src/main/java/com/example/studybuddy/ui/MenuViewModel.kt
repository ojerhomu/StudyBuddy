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

    private val _greeting = mutableStateOf("Loading...")
    val greeting: State<String> = _greeting

    private val _soonestTask = mutableStateOf<UpcomingDisplayItem.DatedItem?>(null)
    val soonestTask: State<UpcomingDisplayItem.DatedItem?> = _soonestTask

    private val _todaysClasses = mutableStateOf<List<UpcomingDisplayItem.ClassItem>>(emptyList())
    val todaysClasses: State<List<UpcomingDisplayItem.ClassItem>> = _todaysClasses

    private val _todaysStudySessions = mutableStateOf<List<UpcomingDisplayItem.DatedItem>>(emptyList())
    val todaysStudySessions: State<List<UpcomingDisplayItem.DatedItem>> = _todaysStudySessions

    private var isDataLoaded = false

    fun refreshData(context: Context, subjectDetails: Map<String, SubjectDetails>) {
        isDataLoaded = false
        loadData(context, subjectDetails)
    }
    
    fun loadData(context: Context, subjectDetails: Map<String, SubjectDetails>) {
        if (isDataLoaded) return

        viewModelScope.launch {
            try {
                val authApi = RetrofitInstance.getAuthApi(context)

                val profileDeferred = async { authApi.getProfile() }
                val tasksDeferred = async { authApi.getTasks() }
                val eventsDeferred = async { authApi.getEvents() }

                val profileResponse = profileDeferred.await()
                val userName = if (profileResponse.isSuccessful) profileResponse.body()?.first_name else null

                val greetingText = try {
                    val model = Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel("gemini-2.5-flash")
                    // FIX: update maybe the prompt to encourage puns to make El have a personality lol
                    val namePrompt = if (userName != null) "for a student named $userName" else "for a student"
                    val prompt = "Write a short, upbeat, one-sentence greeting $namePrompt. Every so often, include a clever and encouraging pun about studying or learning. Keep it concise."
                    val response = model.generateContent(prompt)
                    "Welcome back${if (userName != null) ", $userName" else ""}! ${response.text}"
                } catch (e: Exception) {
                    Log.e("MenuViewModel", "AI Greeting failed: ", e)
                    if (userName != null) "Welcome back, $userName!" else "Welcome back! Let\'s get to it!"
                }
                _greeting.value = greetingText

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
                
                isDataLoaded = true

            } catch (e: Exception) {
                Log.e("MenuViewModel", "Data loading failed: ", e)
                _greeting.value = "Welcome back! You\'ve got this!"
                _soonestTask.value = null
                _todaysClasses.value = emptyList()
                _todaysStudySessions.value = emptyList()
            }
        }
    }

    private fun findSoonestTask(tasks: List<Task>, now: LocalDateTime): UpcomingDisplayItem.DatedItem? {
        return tasks
            .mapNotNull { task -> 
                try {
                    task.due_date?.let { Pair(task, LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME)) }
                } catch (e: DateTimeParseException) {
                    Log.e("MenuViewModel", "Failed to parse task due_date: ${task.due_date}", e)
                    null
                }
            }
            .filter { it.second.isAfter(now) }
            .minByOrNull { it.second }?.let { UpcomingDisplayItem.DatedItem(it.second, "Task: ${it.first.title}") }
    }

    private fun findTodaysStudySessions(events: List<CalendarEvent>, today: LocalDate): List<UpcomingDisplayItem.DatedItem> {
        return events
            .filter { it.event_type == "STUDY_SESSION" }
            .mapNotNull { event ->
                try {
                    val eventDateTime = LocalDateTime.parse(event.start_time, DateTimeFormatter.ISO_DATE_TIME)
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

    private fun findTodaysClasses(subjectDetails: Map<String, SubjectDetails>, today: DayOfWeek): List<UpcomingDisplayItem.ClassItem> {
        val timeFormatter = DateTimeFormatter.ofPattern("H:mm")
        return subjectDetails.flatMap { (subject, details) ->
            details.schedule
                .filter { it.day == today }
                .mapNotNull { scheduleInfo ->
                    if (scheduleInfo.startTime != null && scheduleInfo.endTime != null) {
                        try {
                            val startTime = LocalTime.parse(scheduleInfo.startTime, timeFormatter)
                            val endTime = LocalTime.parse(scheduleInfo.endTime, timeFormatter)
                            UpcomingDisplayItem.ClassItem(subject, startTime, endTime)
                        } catch (e: Exception) {
                            null // handle parsing error
                        }
                    } else {
                        null
                    }
                }
        }.sortedBy { it.startTime }
    }
}
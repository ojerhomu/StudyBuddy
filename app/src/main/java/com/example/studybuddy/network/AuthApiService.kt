package com.example.studybuddy.network

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import java.time.DayOfWeek

//data classes for authenticated API

data class ProfileResponse(
    val id: Int,
    val email: String,
    @SerializedName("first_name") val firstName: String?,
    @SerializedName("last_name") val lastName: String?,
    @SerializedName("education_level") val educationLevel: String?,
    @SerializedName("pomodoro_study_minutes") val pomodoroStudyMinutes: Int?,
    @SerializedName("pomodoro_short_break_minutes") val pomodoroShortBreakMinutes: Int?,
    @SerializedName("pomodoro_long_break_minutes") val pomodoroLongBreakMinutes: Int?
)

data class ProfileUpdateRequest(
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("education_level") val educationLevel: String? = null
)

data class PomodoroPreferenceRequest(
    @SerializedName("pomodoro_study_minutes") val studyMinutes: Int,
    @SerializedName("pomodoro_short_break_minutes") val shortBreakMinutes: Int,
    @SerializedName("pomodoro_long_break_minutes") val longBreakMinutes: Int
)

data class Task(val id: Int, val title: String, val description: String?, val due_date: String?, val subject: String?)
data class TaskRequest(val title: String, val description: String?, val due_date: String?, val subject: String?)

data class CalendarEvent(
    val id: Int,
    val title: String,
    val description: String?,
    val start_time: String,
    val end_time: String,
    val event_type: String,
    val owner_id: Int,
    val created_at: String,
    val subject: String? = null
)

data class EventRequest(
    val title: String,
    val description: String?,
    val start_time: String,
    val end_time: String,
    val event_type: String
)

// data classes for schedule

data class ScheduleInfo(val day: DayOfWeek, val startTime: String?, val endTime: String?)
data class SubjectDetails(val schedule: List<ScheduleInfo>, val color: String)
data class UserScheduleResponse(val schedule: Map<String, SubjectDetails>)
data class ApiScheduleSaveRequest(val schedule: Map<String, SubjectDetails>)

data class UpdateChatRequest(val messages: List<ChatMessage>)

// data classes for chat
data class ChatMessage(val message: String, val isFromUser: Boolean)
data class ChatSessionSummary(val id: Int, val title: String, val created_at: String)
data class ChatSessionDetail(val id: Int, val title: String, val messages: List<ChatMessage>)
data class CreateChatRequest(val title: String, val messages: List<ChatMessage>)

/**
 * Interface for API endpoints that REQUIRE authentication.
 */
interface AuthApiService {
    @GET("/profile")
    suspend fun getProfile(): Response<ProfileResponse>

    @PATCH("/profile")
    suspend fun updateProfile(@Body request: ProfileUpdateRequest): Response<ProfileResponse>

    @PATCH("/profile/preferences")
    suspend fun updatePomodoroPreferences(@Body request: PomodoroPreferenceRequest): Response<ProfileResponse>

    @POST("/profile/schedule")
    suspend fun saveSchedule(@Body scheduleData: ApiScheduleSaveRequest): Response<Unit>

    @GET("/profile/schedule")
    suspend fun getSchedule(): Response<UserScheduleResponse>

    @GET("/tasks/")
    suspend fun getTasks(): Response<List<Task>>

    @POST("/tasks/")
    suspend fun createTask(@Body taskRequest: TaskRequest): Response<Task>

    @DELETE("/tasks/{taskId}")
    suspend fun deleteTask(@Path("taskId") taskId: Int): Response<Unit>

    @POST("/calendar/events")
    suspend fun createEvent(@Body eventRequest: EventRequest): Response<CalendarEvent>

    @GET("/calendar/events")
    suspend fun getEvents(): Response<List<CalendarEvent>>

    @DELETE("/calendar/events/{eventId}")
    suspend fun deleteEvent(@Path("eventId") eventId: Int): Response<Unit>

    // chat endpoints
    @GET("/chats/")
    suspend fun getChatSessions(): Response<List<ChatSessionSummary>>

    @POST("/chats/")
    suspend fun saveChatSession(@Body chatRequest: CreateChatRequest): Response<ChatSessionDetail>

    @GET("/chats/{chatId}")
    suspend fun getChatSession(@Path("chatId") chatId: Int): Response<ChatSessionDetail>

    @PUT("chats/{id}")
    suspend fun updateChatSession(
        @Path("id") id: Int,
        @Body chatRequest: UpdateChatRequest
    ): Response<Unit>

    @DELETE("/chats/{chatId}")
    suspend fun deleteChatSession(@Path("chatId") chatId: Int): Response<Unit>
}
package com.example.studybuddy.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

//data classes

data class LoginResponse(
    val access_token: String,
    val token_type: String
)

data class ProfileResponse(val email: String)

data class Task(val id: Int, val title: String, val description: String?, val due_date: String?)
data class TaskRequest(val title: String, val description: String?, val due_date: String?)

data class CalendarEvent(
    val id: Int,
    val title: String,
    val description: String?,
    val start_time: String,
    val end_time: String,
    val event_type: String,
    val owner_id: Int,
    val created_at: String
)

data class EventRequest(
    val title: String,
    val description: String?,
    val start_time: String,
    val end_time: String,
    val event_type: String
)

//  creates a new user
data class UserCreateRequest(val email: String, val password: String)
data class UserCreateResponse(val id: Int, val email: String)


interface ApiService {
    @FormUrlEncoded
    @POST("/login")
    suspend fun login(
        @Field("username") email: String,
        @Field("password") password: String
    ): Response<LoginResponse>

    // Endpoint for creating a new user
    @POST("/users")
    suspend fun register(@Body userCreateRequest: UserCreateRequest): Response<UserCreateResponse>

    @GET("/profile")
    suspend fun getProfile(): Response<ProfileResponse>

    @GET("/tasks/")
    suspend fun getTasks(): Response<List<Task>>

    @POST("/tasks/")
    suspend fun createTask(@Body taskRequest: TaskRequest): Response<Task>

    @DELETE("/tasks/{taskId}")
    suspend fun deleteTask(@Path("taskId") taskId: Int): Response<Unit>

    // Calendar Event Endpoints
    @POST("/calendar/events")
    suspend fun createEvent(@Body eventRequest: EventRequest): Response<CalendarEvent>

    @GET("/calendar/events")
    suspend fun getEvents(): Response<List<CalendarEvent>>

    @DELETE("/calendar/events/{eventId}")
    suspend fun deleteEvent(@Path("eventId") eventId: Int): Response<Unit>

    companion object {
        const val BASE_URL = "http://10.0.2.2:8000"
    }
}
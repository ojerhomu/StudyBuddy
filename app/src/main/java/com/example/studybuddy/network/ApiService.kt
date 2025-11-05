package com.example.studybuddy.network

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


data class LoginRequest(val email: String, val password: String)
data class LoginResponse(@SerializedName("access_token") val accessToken: String?)

data class ProfileResponse(val email: String)

// Add description to Task and TaskRequest
data class Task(val id: Int, val title: String, val dueDate: String?, val description: String?)
data class TaskRequest(val title: String, val description: String? = null)

data class ScheduleRequest(val title: String, val datetime: String)


interface ApiService {
    @POST("/login")
    suspend fun login(@Body req: LoginRequest): Response<LoginResponse>

    @GET("/profile")
    suspend fun getProfile(): Response<ProfileResponse>

    @GET("/tasks")
    suspend fun getTasks(): Response<List<Task>>

    @POST("/tasks")
    suspend fun createTask(@Body taskRequest: TaskRequest): Response<Task>

    @DELETE("/tasks/{taskId}")
    suspend fun deleteTask(@Path("taskId") taskId: Int): Response<Unit>

    @POST("/schedule")
    suspend fun createSchedule(@Body req: ScheduleRequest): Response<Unit>

    @GET("/recommend")
    suspend fun getRecommendations(): Response<List<String>>

    companion object {
        const val BASE_URL = "http://10.0.2.2:8000"
    }
}

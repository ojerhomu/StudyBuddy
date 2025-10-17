package com.example.studybuddy.network


import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST


data class LoginRequest(val username: String, val password: String)
data class LoginResponse(val token: String?)


data class Task(val id: Int, val title: String, val dueDate: String)


data class ScheduleRequest(val title: String, val datetime: String)


interface ApiService {
    @POST("/auth/login")
    suspend fun login(@Body req: LoginRequest): Response<LoginResponse>


    @GET("/tasks")
    suspend fun getTasks(): Response<List<Task>>


    @POST("/schedule")
    suspend fun createSchedule(@Body req: ScheduleRequest): Response<Unit>


    @GET("/recommend")
    suspend fun getRecommendations(): Response<List<String>>
}



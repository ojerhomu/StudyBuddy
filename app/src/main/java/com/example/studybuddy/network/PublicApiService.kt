package com.example.studybuddy.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

// data classes for public api

data class LoginResponse(
    val access_token: String,
    val token_type: String
)

data class UserCreateRequest(val email: String, val password: String)
data class UserCreateResponse(val id: Int, val email: String)


/**
 * Interface for API endpoints that DO NOT require authentication.
 */
interface PublicApiService {

    @FormUrlEncoded
    @POST("/login")
    suspend fun login(
        @Field("username") email: String,
        @Field("password") password: String
    ): Response<LoginResponse>

    // reg expects a JSON body
    @POST("/register")
    suspend fun register(@Body userCreateRequest: UserCreateRequest): Response<UserCreateResponse>

    companion object {
        const val BASE_URL = "http://10.0.2.2:8000"
    }
}
package com.example.studybuddy.network

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    // client for public endpoints (no interceptor)
    private val publicRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(PublicApiService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val publicApi: PublicApiService by lazy {
        publicRetrofit.create(PublicApiService::class.java)
    }

    // authenticated client

    @Volatile
    private var AUTH_INSTANCE: AuthApiService? = null

    fun getAuthApi(context: Context): AuthApiService {
        return AUTH_INSTANCE ?: synchronized(this) {
            val instance = buildAuthApiService(context)
            AUTH_INSTANCE = instance
            instance
        }
    }

    private fun buildAuthApiService(context: Context): AuthApiService {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder()
            .addInterceptor(TokenInterceptor(context.applicationContext))
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(PublicApiService.BASE_URL) // Use the same base URL
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApiService::class.java)
    }
}
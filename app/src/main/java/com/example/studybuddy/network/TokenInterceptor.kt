package com.example.studybuddy.network

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

class TokenInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val builder = originalRequest.newBuilder()

        // token from sharedpref
        val sharedPref = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
        val token = sharedPref.getString("JWT_TOKEN", null)

        // figuring our what's wrong with the tokenss (diagnostics)
        println("TokenInterceptor running for URL: ${originalRequest.url}")
        if (token != null) {
            println("TokenInterceptor: Token found, adding Authorization header.")
            builder.addHeader("Authorization", "Bearer $token")
        } else {
            println("TokenInterceptor: Token is null, request will not be authorized.")
        }

        val newRequest = builder.build()
        return chain.proceed(newRequest)
    }
}
package com.example.studybuddy.network

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

class TokenInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val builder = originalRequest.newBuilder()

        // get token from SharedPreferences
        val sharedPref = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
        val token = sharedPref.getString("JWT_TOKEN", null)

        //for troubleshooting the stupid my profile button
        if (!token.isNullOrEmpty()) {
            //auth header in "Bearer <token>" format or something like that
            builder.addHeader("Authorization", "Bearer $token")
            println("TokenInterceptor: Token found. Value: $token")
        } else {
            println("TokenInterceptor: No token found. Your dumb dumb request will not be authorized")
        }

        val newRequest = builder.build()
        return chain.proceed(newRequest)
    }
}


package com.example.studybuddy.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.studybuddy.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("Loading...") }
    var errorMessage by remember { mutableStateOf("") }

    // load profile from backend
    LaunchedEffect(Unit) {
        try {
            val response = withContext(Dispatchers.IO) {
                RetrofitInstance.getInstance(context).getProfile()
            }
            if (response.isSuccessful && response.body() != null) {
                email = response.body()!!.email
            } else {
                //TODO: FIX THIS ERROR
                errorMessage = "Failed to load profile (THIS IS CURRENTLY BROKEN FIX SOMEHOW!): ${response.code()}"
            }
        } catch (e: Exception) {
            errorMessage = "Error: ${e.message}"
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Profile", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        } else {
            Text(text = "Email: $email")
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                // clear the token from SharedPreferences
                val sharedPref = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    remove("JWT_TOKEN")
                    commit() // commit so it's saved before navigating
                }
                // trigger navigation back to Login
                onLogout()
            }
        ) {
            Text("Logout")
        }
    }
}
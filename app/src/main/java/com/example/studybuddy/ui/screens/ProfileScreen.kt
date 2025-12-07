package com.example.studybuddy.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studybuddy.network.RetrofitInstance
import com.example.studybuddy.ui.OnboardingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.format.DateTimeFormatter

@Composable
fun ProfileScreen(onLogout: () -> Unit, onboardingViewModel: OnboardingViewModel) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("Loading...") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val darkPurple = Color(0xFF3A1B59)

    LaunchedEffect(Unit) {
        try {
            val response = withContext(Dispatchers.IO) {
                RetrofitInstance.getAuthApi(context).getProfile()
            }
            if (response.isSuccessful && response.body() != null) {
                val profile = response.body()!!
                email = profile.email
                firstName = profile.first_name ?: "Not set"
                lastName = profile.last_name ?: "Not set"
            } else {
                errorMessage = "Failed to load profile: ${response.code()}"
            }
        } catch (e: Exception) {
            errorMessage = "Error: ${e.message}"
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Profile", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(24.dp))

        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        } else {
            Text("First Name: $firstName", color = darkPurple, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Last Name: $lastName", color = darkPurple, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Email: $email", color = darkPurple, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        Spacer(Modifier.height(32.dp))
        Divider()
        Spacer(Modifier.height(16.dp))

        Text("My Classes", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(onboardingViewModel.subjectDetailsMap.keys.toList()) {
                subject ->
                val details = onboardingViewModel.subjectDetailsMap[subject]
                if (details != null) {
                    val scheduleText = details.schedule.groupBy { "${it.startTime}-${it.endTime}" }.values.joinToString("\n") { infos ->
                        val firstInfo = infos.first()
                        val days = infos.map { it.day.name.take(3) }.distinct().joinToString(", ")
                        "$days at ${firstInfo.startTime?.format(DateTimeFormatter.ofPattern("h:mm a"))} - ${firstInfo.endTime?.format(DateTimeFormatter.ofPattern("h:mm a"))}"
                    }
                    val color = try { Color(android.graphics.Color.parseColor(details.color)) } catch (e: Exception) { Color.Gray }
                    
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                        Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(color))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(subject, fontWeight = FontWeight.Bold)
                            if(scheduleText.isNotEmpty()) Text(scheduleText, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

        Button(onClick = {
            val sharedPref = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                remove("JWT_TOKEN")
                commit()
            }
            onLogout()
        }) {
            Text("Logout")
        }
    }
}
package com.example.studybuddy.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studybuddy.ui.OnboardingViewModel
import java.time.format.DateTimeFormatter

@Composable
fun ProfileScreen(onLogout: () -> Unit, onboardingViewModel: OnboardingViewModel) {
    val context = LocalContext.current
    val firstName by onboardingViewModel.firstName
    val lastName by onboardingViewModel.lastName
    val email by onboardingViewModel.email

    val darkPurple = Color(0xFF3A1B59)

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Profile", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(24.dp))

        // display name from viewmodel
        Text("First Name: ${firstName ?: "Not set"}", color = darkPurple, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("Last Name: ${lastName ?: "Not set"}", color = darkPurple, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("Email: ${email ?: "Not set"}", color = darkPurple, fontWeight = FontWeight.Bold, fontSize = 18.sp)


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
                        if (firstInfo.startTime != null && firstInfo.endTime != null) {
                            try {
                                val startTime = java.time.LocalTime.parse(firstInfo.startTime)
                                val endTime = java.time.LocalTime.parse(firstInfo.endTime)
                                "$days at ${startTime.format(DateTimeFormatter.ofPattern("h:mm a"))} - ${endTime.format(DateTimeFormatter.ofPattern("h:mm a"))}"
                            } catch(e: Exception) {""}
                        } else {
                            ""
                        }
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
                apply() //  apply for asynchronous save
            }
            onLogout()
        }) {
            Text("Logout")
        }
    }
}
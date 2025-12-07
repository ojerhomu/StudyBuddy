package com.example.studybuddy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.studybuddy.network.ScheduleInfo
import com.example.studybuddy.ui.OnboardingViewModel
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun ScheduleConfirmationScreen(
    onboardingViewModel: OnboardingViewModel,
    onConfirm: () -> Unit,
    onGoBack: () -> Unit
) {
    val scheduleByDate: Map<DayOfWeek, List<Pair<String, ScheduleInfo>>> = remember(onboardingViewModel.subjectDetailsMap) {
        onboardingViewModel.subjectDetailsMap.flatMap { (subject, details) ->
            details.schedule.map { Pair(subject, it) } // FIX: Use Pair instead of Triple
        }.groupBy { it.second.day }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Does everything look correct?", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            scheduleByDate.keys.sorted().forEach { day ->
                val events = scheduleByDate[day] ?: emptyList()
                item {
                    Text(day.name, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                }
                items(events) { (subject, scheduleInfo) ->
                    val colorString = onboardingViewModel.subjectDetailsMap[subject]?.color ?: "#888888"
                    val color = try { Color(android.graphics.Color.parseColor(colorString)) } catch (e: Exception) { Color.Gray }
                    val time = scheduleInfo.startTime?.format(DateTimeFormatter.ofPattern("h:mm a")) ?: ""
                    Text(
                        text = "$subject at $time",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(color.copy(alpha = 0.5f))
                            .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        Row {
            Button(onClick = onGoBack, modifier = Modifier.weight(1f)) { Text("No, go back") }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = onConfirm, modifier = Modifier.weight(1f)) { Text("Yes") }
        }
    }
}
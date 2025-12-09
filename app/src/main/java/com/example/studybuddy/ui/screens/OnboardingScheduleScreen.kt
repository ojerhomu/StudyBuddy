package com.example.studybuddy.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.studybuddy.network.ScheduleInfo
import com.example.studybuddy.network.SubjectDetails
import com.example.studybuddy.ui.OnboardingViewModel
import com.example.studybuddy.ui.components.ColorPickerDialog
import com.example.studybuddy.ui.components.DayOfWeekPickerDialog
import com.example.studybuddy.ui.components.TimeRangePickerDialog
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Composable
fun OnboardingScheduleScreen(
    onboardingViewModel: OnboardingViewModel,
    onScheduleComplete: () -> Unit
) {
    val subjectDetailsMap = onboardingViewModel.subjectDetailsMap
    val asyncSubjects = onboardingViewModel.asyncSubjects
    
    var subjectCurrentlyScheduling by remember { mutableStateOf<String?>(null) }
    var daysCurrentlyScheduling by remember { mutableStateOf<Set<DayOfWeek>>(emptySet()) }
    var showDayPicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (showDayPicker) {
        subjectCurrentlyScheduling?.let { subject ->
            DayOfWeekPickerDialog(
                subjectName = subject,
                onDismissRequest = { showDayPicker = false },
                onDaysSelected = { days ->
                    daysCurrentlyScheduling = days
                    showDayPicker = false
                    showTimePicker = true
                }
            )
        }
    }
    if (showTimePicker) {
        subjectCurrentlyScheduling?.let { subject ->
            TimeRangePickerDialog(
                subjectName = subject,
                onDismissRequest = { showTimePicker = false },
                onTimeRangeSelected = { start, end ->
                    val conflictingSubject = findConflict(subjectDetailsMap, daysCurrentlyScheduling, start, end, subject)
                    if (conflictingSubject != null) {
                        Toast.makeText(context, "You already have $conflictingSubject scheduled for that time!", Toast.LENGTH_LONG).show()
                    } else {
                        val currentDetails = subjectDetailsMap[subject]
                        if (currentDetails != null) {
                            val newSchedule = mutableListOf<ScheduleInfo>()
                            daysCurrentlyScheduling.forEach { day -> newSchedule.add(ScheduleInfo(day, start.format(DateTimeFormatter.ISO_LOCAL_TIME), end.format(DateTimeFormatter.ISO_LOCAL_TIME))) }
                            subjectDetailsMap[subject] = currentDetails.copy(schedule = newSchedule)
                        }
                        showTimePicker = false
                        subjectCurrentlyScheduling = null
                    }
                }
            )
        }
    }
    if (showColorPicker) {
        subjectCurrentlyScheduling?.let { subject ->
            ColorPickerDialog(
                onDismissRequest = { showColorPicker = false },
                onColorSelected = { color ->
                    val currentDetails = subjectDetailsMap[subject]
                    if (currentDetails != null) {
                        val colorString = String.format("#%08X", color.toArgb())
                        subjectDetailsMap[subject] = currentDetails.copy(color = colorString)
                    }
                    showColorPicker = false
                    subjectCurrentlyScheduling = null
                }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Build Your Schedule", style = MaterialTheme.typography.headlineMedium)
        Text("Add your class times for each subject.", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            subjectDetailsMap.keys.forEach { subject ->
                subjectDetailsMap[subject]?.let {
                    details ->
                    SubjectScheduleCard(
                        subjectName = subject,
                        details = details,
                        isAsynchronous = asyncSubjects[subject] ?: false,
                        onAsynchronousChanged = { isChecked -> asyncSubjects[subject] = isChecked },
                        onAddTimeClicked = { 
                            subjectCurrentlyScheduling = subject
                            showDayPicker = true
                        },
                        onSetColorClicked = {
                            subjectCurrentlyScheduling = subject
                            showColorPicker = true
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        Button(
            onClick = onScheduleComplete,
            enabled = true
        ) {
            Text("Done")
        }
    }
}

private fun findConflict(scheduleMap: Map<String, SubjectDetails>, days: Set<DayOfWeek>, newStart: LocalTime, newEnd: LocalTime?, currentSubject: String): String? {
    for (day in days) {
        for ((subject, details) in scheduleMap) {
            if (subject == currentSubject) continue
            for (info in details.schedule) {
                if (info.day == day) {
                    try {
                        if (info.startTime != null && info.endTime != null && newEnd != null) {
                            val existingStart = LocalTime.parse(info.startTime)
                            val existingEnd = LocalTime.parse(info.endTime)
                            if (newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart)) {
                                return subject
                            }
                        }
                    } catch (e: DateTimeParseException) {
                        Log.e("OnboardingSchedule", "Could not parse existing schedule time: ${info.startTime} or ${info.endTime}", e)
                    }
                }
            }
        }
    }
    return null
}

@Composable
private fun SubjectScheduleCard(
    subjectName: String,
    details: SubjectDetails,
    isAsynchronous: Boolean,
    onAsynchronousChanged: (Boolean) -> Unit,
    onAddTimeClicked: () -> Unit,
    onSetColorClicked: () -> Unit
) {
    val timeFormatter = remember { DateTimeFormatter.ofPattern("h:mm a") }
    val color = try { Color(android.graphics.Color.parseColor(details.color)) } catch (e: Exception) { Color.Gray }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(subjectName, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(color))
            }
            Spacer(modifier = Modifier.height(8.dp))

            details.schedule.groupBy { "${it.startTime}-${it.endTime}" }.values.forEach { infos ->
                val firstInfo = infos.first()
                val days = infos.map { it.day.name.take(3) }.distinct().joinToString(", ")
                
                val scheduleText = if (firstInfo.startTime != null && firstInfo.endTime != null) {
                    try {
                        val startTime = LocalTime.parse(firstInfo.startTime)
                        val endTime = LocalTime.parse(firstInfo.endTime)
                        "$days at ${startTime.format(timeFormatter)} - ${endTime.format(timeFormatter)}"
                    } catch (e: DateTimeParseException) {
                        Log.e("SubjectScheduleCard", "Failed to parse schedule time: ${firstInfo.startTime}", e)
                        null
                    }
                } else {
                    null
                }

                if (scheduleText != null) {
                    Text(scheduleText)
                }
            }
            
            if (details.schedule.isNotEmpty()) Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isAsynchronous, onCheckedChange = onAsynchronousChanged)
                Text("This class is asynchronous (no set time)")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Button(onClick = onAddTimeClicked, enabled = !isAsynchronous, modifier = Modifier.weight(1f)) {
                    Text("Add/Edit Class Time")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onSetColorClicked, modifier = Modifier.weight(1f)) {
                    Text("Set Color")
                }
            }
        }
    }
}
package com.example.studybuddy.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.studybuddy.network.EventRequest
import com.example.studybuddy.network.RetrofitInstance
import com.example.studybuddy.ui.components.CalendarView
import com.example.studybuddy.ui.components.TimePickerDialog
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun ScheduleSessionScreen(onSaveSuccess: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }
    var showTimePicker by remember { mutableStateOf(false) }
    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            onTimeSelected = {
                selectedTime = it
                showTimePicker = false
            },
            subjectName = "Study Session"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Session Title") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val date = selectedDate
                val time = selectedTime
                if (date != null && time != null) {
                    coroutineScope.launch {
                        try {
                            val startDateTime = date.atTime(time)
                            val endDateTime = startDateTime.plusHours(1)

                            val response = RetrofitInstance.getAuthApi(context).createEvent(
                                EventRequest(
                                    title = title,
                                    description = null,
                                    start_time = startDateTime.format(DateTimeFormatter.ISO_DATE_TIME),
                                    end_time = endDateTime.format(DateTimeFormatter.ISO_DATE_TIME),
                                    event_type = "STUDY_SESSION"
                                )
                            )
                            if (response.isSuccessful) {
                                Toast.makeText(context, "Session saved!", Toast.LENGTH_SHORT).show()
                                onSaveSuccess()
                            } else {
                                Toast.makeText(context, "Failed to save session: ${response.code()}", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Please select a date and time", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Study Session")
        }

        Spacer(modifier = Modifier.height(16.dp))

        CalendarView(
            yearMonth = currentYearMonth,
            events = emptyList(),
            onPreviousMonth = { currentYearMonth = currentYearMonth.minusMonths(1) },
            onNextMonth = { currentYearMonth = currentYearMonth.plusMonths(1) },
            onDateSelected = { date ->
                selectedDate = date
                showTimePicker = true
            },
            onEventSelected = {},
            subjectDetailsMap = emptyMap(),
            classSchedule = emptyMap()
        )
    }
}
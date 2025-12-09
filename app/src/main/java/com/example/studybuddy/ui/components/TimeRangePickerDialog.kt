package com.example.studybuddy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.time.LocalTime

@Composable
fun TimeRangePickerDialog(
    subjectName: String,
    onDismissRequest: () -> Unit,
    onTimeRangeSelected: (LocalTime, LocalTime) -> Unit
) {
    var startHour by remember { mutableStateOf("09") }
    var startMinute by remember { mutableStateOf("00") }
    var startIsAm by remember { mutableStateOf(true) }

    var endHour by remember { mutableStateOf("10") }
    var endMinute by remember { mutableStateOf("30") }
    var endIsAm by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("What time is $subjectName?", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            // start time picker
            Text("Start Time")
            TimeSelector(hour = startHour, minute = startMinute, isAm = startIsAm, onHourChange = { startHour = it }, onMinuteChange = { startMinute = it }, onAmPmChange = { startIsAm = it })

            Spacer(modifier = Modifier.height(16.dp))

            // end time picker
            Text("End Time")
            TimeSelector(hour = endHour, minute = endMinute, isAm = endIsAm, onHourChange = { endHour = it }, onMinuteChange = { endMinute = it }, onAmPmChange = { endIsAm = it })

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismissRequest) { Text("Cancel") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    val startTime = parseTime(startHour, startMinute, startIsAm)
                    val endTime = parseTime(endHour, endMinute, endIsAm)
                    if (startTime != null && endTime != null) {
                        onTimeRangeSelected(startTime, endTime)
                    }
                }) { Text("Save") }
            }
        }
    }
}

@Composable
private fun TimeSelector(
    hour: String, minute: String, isAm: Boolean,
    onHourChange: (String) -> Unit, onMinuteChange: (String) -> Unit, onAmPmChange: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(value = hour, onValueChange = { if (it.all(Char::isDigit) && it.length <= 2) onHourChange(it) }, label = { Text("HH") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.width(80.dp))
        Text(" : ", style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(value = minute, onValueChange = { if (it.all(Char::isDigit) && it.length <= 2) onMinuteChange(it) }, label = { Text("MM") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.width(80.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Button(onClick = { onAmPmChange(true) }, enabled = !isAm, modifier = Modifier.height(40.dp)) { Text("AM") }
            Spacer(modifier = Modifier.height(4.dp))
            Button(onClick = { onAmPmChange(false) }, enabled = isAm, modifier = Modifier.height(40.dp)) { Text("PM") }
        }
    }
}

private fun parseTime(hourStr: String, minuteStr: String, isAm: Boolean): LocalTime? {
    val hour = hourStr.toIntOrNull() ?: return null
    val minute = minuteStr.toIntOrNull() ?: return null
    val hour24 = when {
        isAm && hour == 12 -> 0 // midnight case
        !isAm && hour < 12 -> hour + 12
        else -> hour
    }
    return LocalTime.of(hour24, minute)
}
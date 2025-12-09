package com.example.studybuddy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    subjectName: String,
    onDismissRequest: () -> Unit,
    onTimeRangeSelected: ((LocalTime, LocalTime) -> Unit)? = null,
    onTimeSelected: ((LocalTime) -> Unit)? = null
) {
    var startHour by remember { mutableStateOf(12) }
    var startMinute by remember { mutableStateOf(0) }
    var startAmPm by remember { mutableStateOf("AM") }

    var endHour by remember { mutableStateOf(1) }
    var endMinute by remember { mutableStateOf(0) }
    var endAmPm by remember { mutableStateOf("PM") }

    val isRange = onTimeRangeSelected != null

    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier.background(MaterialTheme.colorScheme.surface).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Schedule $subjectName", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))

            TimePickerRow("Start Time", startHour, startMinute, startAmPm) { h, m, ap ->
                startHour = h
                startMinute = m
                startAmPm = ap
            }
            if (isRange) {
                Spacer(Modifier.height(8.dp))
                TimePickerRow("End Time", endHour, endMinute, endAmPm) { h, m, ap ->
                    endHour = h
                    endMinute = m
                    endAmPm = ap
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    val finalStartHour = if (startAmPm == "PM" && startHour != 12) startHour + 12 else if (startAmPm == "AM" && startHour == 12) 0 else startHour
                    val finalEndHour = if (endAmPm == "PM" && endHour != 12) endHour + 12 else if (endAmPm == "AM" && endHour == 12) 0 else endHour
                    val startTime = LocalTime.of(finalStartHour, startMinute)

                    if (isRange) {
                        val endTime = LocalTime.of(finalEndHour, endMinute)
                        onTimeRangeSelected?.invoke(startTime, endTime)
                    } else {
                        onTimeSelected?.invoke(startTime)
                    }
                    onDismissRequest()
                }
            ) {
                Text("Set Time")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerRow(
    label: String,
    hour: Int,
    minute: Int,
    amPm: String,
    onTimeChanged: (Int, Int, String) -> Unit
) {
    var showHourDropdown by remember { mutableStateOf(false) }
    var showMinuteDropdown by remember { mutableStateOf(false) }
    var showAmPmDropdown by remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("$label: ", modifier = Modifier.width(80.dp))
        // Hour Dropdown
        ExposedDropdownMenuBox(expanded = showHourDropdown, onExpandedChange = { showHourDropdown = it }) {
            OutlinedTextField(value = "$hour", onValueChange = {}, readOnly = true, modifier = Modifier.menuAnchor().width(70.dp))
            ExposedDropdownMenu(expanded = showHourDropdown, onDismissRequest = { showHourDropdown = false }) {
                (1..12).forEach { h -> DropdownMenuItem(text = { Text("$h") }, onClick = { onTimeChanged(h, minute, amPm); showHourDropdown = false }) }
            }
        }
        Text(":")
        // Minute Dropdown
        ExposedDropdownMenuBox(expanded = showMinuteDropdown, onExpandedChange = { showMinuteDropdown = it }) {
            OutlinedTextField(value = String.format("%02d", minute), onValueChange = {}, readOnly = true, modifier = Modifier.menuAnchor().width(70.dp))
            ExposedDropdownMenu(expanded = showMinuteDropdown, onDismissRequest = { showMinuteDropdown = false }) {
                (0..59 step 5).forEach { m -> DropdownMenuItem(text = { Text(String.format("%02d", m)) }, onClick = { onTimeChanged(hour, m, amPm); showMinuteDropdown = false }) }
            }
        }
        // AM/PM Dropdown
        ExposedDropdownMenuBox(expanded = showAmPmDropdown, onExpandedChange = { showAmPmDropdown = it }) {
            OutlinedTextField(value = amPm, onValueChange = {}, readOnly = true, modifier = Modifier.menuAnchor().width(80.dp))
            ExposedDropdownMenu(expanded = showAmPmDropdown, onDismissRequest = { showAmPmDropdown = false }) {
                DropdownMenuItem(text = { Text("AM") }, onClick = { onTimeChanged(hour, minute, "AM"); showAmPmDropdown = false })
                DropdownMenuItem(text = { Text("PM") }, onClick = { onTimeChanged(hour, minute, "PM"); showAmPmDropdown = false })
            }
        }
    }
}

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
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onTimeSelected: (LocalTime) -> Unit
) {
    var hour by remember { mutableStateOf("12") }
    var minute by remember { mutableStateOf("00") }
    var isAm by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Text("Select Time", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = hour,
                    onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 2) hour = it },
                    label = { Text("HH") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(80.dp)
                )
                Text(" : ", style = MaterialTheme.typography.headlineMedium)
                OutlinedTextField(
                    value = minute,
                    onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 2) minute = it },
                    label = { Text("MM") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(80.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Button(onClick = { isAm = true }) { Text("AM") }
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(onClick = { isAm = false }) { Text("PM") }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismissRequest) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    val hour24 = (hour.toIntOrNull() ?: 0).let {
                        if (isAm && it == 12) 0 else if (!isAm && it != 12) it + 12 else it
                    }
                    val finalTime = LocalTime.of(hour24, minute.toIntOrNull() ?: 0)
                    onTimeSelected(finalTime)
                }) {
                    Text("OK")
                }
            }
        }
    }
}
package com.example.studybuddy.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun StartSessionScreen(onNext: (Long) -> Unit) {
    var hours by remember { mutableStateOf("0") }
    var minutes by remember { mutableStateOf("25") }
    var seconds by remember { mutableStateOf("0") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "How long would you like to focus on studying for?",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(32.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            TimeInput(label = "Hours", value = hours, onValueChange = { hours = it })
            Spacer(modifier = Modifier.width(8.dp))
            TimeInput(label = "Minutes", value = minutes, onValueChange = { minutes = it })
            Spacer(modifier = Modifier.width(8.dp))
            TimeInput(label = "Seconds", value = seconds, onValueChange = { seconds = it })
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = {
            val h = hours.toLongOrNull() ?: 0
            val m = minutes.toLongOrNull() ?: 0
            val s = seconds.toLongOrNull() ?: 0
            val totalSeconds = (h * 3600) + (m * 60) + s
            onNext(totalSeconds)
        }) {
            Text("Next")
        }
    }
}

@Composable
private fun TimeInput(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = {
            // allow only digits and keep it within a reasonable length
            if (it.all { char -> char.isDigit() } && it.length <= 2) {
                onValueChange(it)
            }
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.width(90.dp)
    )
}
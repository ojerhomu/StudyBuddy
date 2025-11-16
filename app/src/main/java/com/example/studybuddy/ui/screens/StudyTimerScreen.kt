package com.example.studybuddy.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun StudyTimerScreen(
    studyDurationSeconds: Long,
    breakDurationSeconds: Long,
    isBreak: Boolean = false, // to determine which timer to start
    onBreakStart: () -> Unit,
    onFinish: () -> Unit,
    onCancel: () -> Unit // callback for the cancel button
) {
    var remainingTime by remember { mutableStateOf(if (isBreak) breakDurationSeconds else studyDurationSeconds) }
    var isPaused by remember { mutableStateOf(false) }
    val timerTitle = if (isBreak) "Break Time" else "Study Time"

    LaunchedEffect(key1 = remainingTime, key2 = isPaused) {
        while (remainingTime > 0 && !isPaused) {
            delay(1000)
            remainingTime--
        }

        if (remainingTime == 0L) {
            if (!isBreak) {
                onBreakStart() // prompt for break
            } else {
                onFinish() // break timer finished, go to post break screen
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(timerTitle, style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(32.dp))

        // Format time as HH:MM:SS
        val hours = remainingTime / 3600
        val minutes = (remainingTime % 3600) / 60
        val seconds = remainingTime % 60
        Text(
            text = "%02d:%02d:%02d".format(hours, minutes, seconds),
            style = MaterialTheme.typography.displayLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row {
            Button(onClick = { isPaused = !isPaused }) {
                Text(if (isPaused) "Resume" else "Pause")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = onCancel) { // the new onCancel callback
                Text("Cancel")
            }
        }
    }
}
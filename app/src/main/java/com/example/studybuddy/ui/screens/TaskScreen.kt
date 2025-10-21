package com.example.studybuddy.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
// i have (placeholder) where there's placeholders

@Composable
fun TaskScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Assignments & Tasks")
        Spacer(Modifier.height(8.dp))
        Text("(placeholder list) \n- Math HW 3\n- Other things idk")
        Spacer(Modifier.height(12.dp))
        Button(onClick = { /* navigate to add task */ }) { Text("Add Task") }
    }
}


@Composable
fun PracticeScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Practice / Quiz")
        Spacer(Modifier.height(8.dp))
        Text("(placeholder) generate practice questions with backend")
    }
}


@Composable
fun SettingsScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Settings")
        Spacer(Modifier.height(8.dp))
        Text("Theme, notifications, account settings (placeholders)")
    }
}
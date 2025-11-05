package com.example.studybuddy.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun MenuScreen(onNavigate: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Study Buddy — Menu")
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onNavigate("scheduler") }) { Text("Study Scheduler") }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onNavigate("tasks") }) { Text("Assignments & Tasks") }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onNavigate("practice") }) { Text("Practice / Quiz") }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onNavigate("settings") }) { Text("Settings") }
        Button(modifier = Modifier.fillMaxWidth(), onClick = { onNavigate("profile") }) {
            Text("My Profile")
        }
    }
}
//ui for menus to navigate to other screens
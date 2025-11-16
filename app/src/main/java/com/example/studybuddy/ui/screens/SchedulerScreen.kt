package com.example.studybuddy.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

//this isn't in use yet but it's when the AI helps you create a study session maybe delete this depending on what we do
@Composable
fun SchedulerScreen() {
    var sessionTitle by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Create a Study Session for Me")
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = sessionTitle, onValueChange = { sessionTitle = it }, label = { Text("Title") })
        Spacer(Modifier.height(12.dp))
        Button(onClick = {
// TODO: need to POST to /schedule
        }) { Text("Save Session") }
    }
}
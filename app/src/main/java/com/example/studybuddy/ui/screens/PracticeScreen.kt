package com.example.studybuddy.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun PracticeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Practice & Quiz",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )
        Text(
            text = "How would you like to practice today?",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { navController.navigate("chat_history") },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Homework Help")
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate("quiz_screen") }, // nav to new quiz
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Quiz Me!")
        }
    }
}
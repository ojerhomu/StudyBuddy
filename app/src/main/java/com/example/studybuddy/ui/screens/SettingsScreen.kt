package com.example.studybuddy.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun SettingsScreen(navController: NavController) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Edit Schedule") },
            text = { Text("What would you like to change?") },
            confirmButton = {
                Button(
                    onClick = { 
                        navController.navigate("change_subjects")
                        showDialog = false 
                    }
                ) {
                    Text("Add/Remove Classes") // FIX: changed button text
                }
            },
            dismissButton = {
                Button(
                    onClick = { 
                        navController.navigate("edit_schedule")
                        showDialog = false
                    }
                ) {
                    Text("Change Class Times/Days")
                }
            }
        )
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Edit Class Schedule",
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDialog = true }
                .padding(vertical = 16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
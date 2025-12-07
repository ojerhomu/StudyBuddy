package com.example.studybuddy.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.studybuddy.ui.OnboardingViewModel

@Composable
fun ChangeSubjectsScreen(
    navController: NavController, 
    onboardingViewModel: OnboardingViewModel, // FIX THIS: make ViewModel a required parameter
    onSubjectsChanged: () -> Unit
) {
    var subjectToDelete by remember { mutableStateOf<String?>(null) }
    val subjects = onboardingViewModel.subjectDetailsMap.keys.toList()

    // save changes when the user leaves the screen
    DisposableEffect(Unit) {
        onDispose {
            onSubjectsChanged()
        }
    }

    subjectToDelete?.let { subject ->
        AlertDialog(
            onDismissRequest = { subjectToDelete = null },
            title = { Text("Remove Class?") },
            text = { Text("Are you sure you want to remove '$subject'? This will delete all its scheduled times and tasks.") },
            confirmButton = {
                Button(onClick = { 
                    onboardingViewModel.removeSubject(subject)
                    subjectToDelete = null
                }) { Text("Yes, Remove") }
            },
            dismissButton = { Button(onClick = { subjectToDelete = null }) { Text("Cancel") } }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Add/Remove Classes", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(subjects) { subject ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(subject, style = MaterialTheme.typography.bodyLarge)
                    IconButton(onClick = { subjectToDelete = subject }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove Class")
                    }
                }
                Divider()
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        if (subjects.size >= 7) {
            Text(
                text = "You have reached the maximum of 7 classes.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
        }
        
        Button(
            onClick = { navController.navigate("onboarding_subjects") }, 
            modifier = Modifier.fillMaxWidth(),
            enabled = subjects.size < 7 // can't use button when limit is reached
        ) {
            Text("Add a Class")
        }
    }
}
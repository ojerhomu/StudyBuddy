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
    onboardingViewModel: OnboardingViewModel,
    onSubjectsChanged: () -> Unit // save data
) {
    var subjectToDelete by remember { mutableStateOf<String?>(null) }
    val subjects = onboardingViewModel.subjectDetailsMap.keys.toList()

    // remove dispossible effect

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
        
        Row {
            Button(
                onClick = { navController.navigate("onboarding_subjects") }, 
                modifier = Modifier.weight(1f),
                enabled = subjects.size < 7
            ) {
                Text("Add a Class")
            }
            Spacer(modifier = Modifier.width(16.dp))
            // FIX: add a done button to save changes
            Button(
                onClick = { 
                    onSubjectsChanged()
                    navController.popBackStack() 
                }, 
                modifier = Modifier.weight(1f)
            ) {
                Text("Done")
            }
        }
    }
}
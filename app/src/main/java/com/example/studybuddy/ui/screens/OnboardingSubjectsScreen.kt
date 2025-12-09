package com.example.studybuddy.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.studybuddy.ui.OnboardingViewModel

data class SubjectCategory(val name: String, val courses: List<String>)

val subjectCategories = listOf(
    SubjectCategory("Math", listOf("Pre-Algebra", "Algebra 1", "Algebra 2", "Geometry", "Pre-Calculus", "Calculus 1", "Calculus 2", "Statistics", "Trigonometry")),
    SubjectCategory("Science", listOf("Biology", "Chemistry", "Physics", "Computer Science 1", "Computer Science 2")),
    SubjectCategory("English", listOf("English Composition 1", "English Composition 2", "Literature")),
    SubjectCategory("Social Studies", listOf("World History", "U.S. History", "Government", "Economics")),
    SubjectCategory("Other", listOf("Spanish", "French"))
)

@Composable
fun OnboardingSubjectsScreen(
    onboardingViewModel: OnboardingViewModel,
    onNext: () -> Unit,
    shouldSaveOnDispose: Boolean = false
) {
    val context = LocalContext.current

    DisposableEffect(shouldSaveOnDispose) {
        onDispose {
            if (shouldSaveOnDispose) {
                onboardingViewModel.saveUserSchedule(context)
            }
        }
    }

    // IMPLEMENT launched effect to safely initialize state and avoid race conditions
    var selectedSubjects by remember { mutableStateOf<Set<String>>(emptySet()) }
    LaunchedEffect(onboardingViewModel.subjectDetailsMap.keys) {
        selectedSubjects = onboardingViewModel.subjectDetailsMap.keys
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("What subjects are you taking?", style = MaterialTheme.typography.headlineSmall)
        Text("(Select up to 7)", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(16.dp))

        subjectCategories.forEach { category ->
            SubjectDropdown(
                category = category,
                selectedSubjects = selectedSubjects,
                onSubjectSelected = { subject, isSelected ->
                    selectedSubjects = if (isSelected) {
                        if (selectedSubjects.size < 7) selectedSubjects + subject else selectedSubjects
                    } else {
                        selectedSubjects - subject
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                onboardingViewModel.setSubjects(selectedSubjects.toList())
                onNext()
            },
            enabled = selectedSubjects.isNotEmpty()
        ) {
            Text("Next")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubjectDropdown(
    category: SubjectCategory,
    selectedSubjects: Set<String>,
    onSubjectSelected: (String, Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = "${category.name} (${category.courses.count { it in selectedSubjects }})".takeIf { category.courses.any { it in selectedSubjects } } ?: category.name,
            onValueChange = {},
            readOnly = true,
            label = { Text(category.name) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )

        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            category.courses.forEach { course ->
                DropdownMenuItem(
                    text = { Text(course) },
                    onClick = { onSubjectSelected(course, !selectedSubjects.contains(course)) },
                    leadingIcon = {
                        if (selectedSubjects.contains(course)) {
                            Checkbox(checked = true, onCheckedChange = null)
                        } else {
                            Checkbox(checked = false, onCheckedChange = null)
                        }
                    }
                )
            }
        }
    }
}
package com.example.studybuddy.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.studybuddy.network.ProfileResponse
import com.example.studybuddy.network.RetrofitInstance
import com.example.studybuddy.ui.OnboardingViewModel
import com.example.studybuddy.ui.QuizViewModel
import com.example.studybuddy.ui.components.ChatUi // FIX: import the new generic UI
import com.example.studybuddy.ui.components.SubjectSelectionScreen

@Composable
fun QuizScreen(
    onboardingViewModel: OnboardingViewModel,
    quizViewModel: QuizViewModel = viewModel()
) {
    var selectedSubject by remember { mutableStateOf<String?>(null) }
    var topics by remember { mutableStateOf("") }
    var quizStarted by remember { mutableStateOf(false) }
    var profile by remember { mutableStateOf<ProfileResponse?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val response = RetrofitInstance.getAuthApi(context).getProfile()
        if (response.isSuccessful) {
            profile = response.body()
        }
    }

    when {
        !quizStarted && selectedSubject == null -> {
            SubjectSelectionScreen(
                title = "What subject do you want to be quizzed on?",
                subjects = onboardingViewModel.subjectDetailsMap.keys.toList()
            ) {
                subject -> selectedSubject = subject
            }
        }
        !quizStarted && selectedSubject != null -> {
            TopicSelectionScreen(subject = selectedSubject!!, onStartQuiz = {
                topicList ->
                topics = topicList
                quizStarted = true
                val userName = profile?.first_name ?: "User"
                quizViewModel.startQuiz(selectedSubject!!, topics, userName)
            })
        }
        quizStarted -> {
            // FIX: use  ChatUi
            ChatUi(
                messages = quizViewModel.messages,
                userName = profile?.first_name ?: "You",
                onSendMessage = { quizViewModel.sendMessage(it) }
            )
        }
    }
}

@Composable
private fun TopicSelectionScreen(subject: String, onStartQuiz: (String) -> Unit) {
    var topics by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("What topics in $subject do you want to be quizzed on?", style = MaterialTheme.typography.headlineMedium)
        Text("(e.g., Unit 1, Cell Division, The Cold War)", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = topics,
            onValueChange = { topics = it },
            label = { Text("Enter topics, separated by commas") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onStartQuiz(topics) }, enabled = topics.isNotBlank()) {
            Text("Start Quiz!")
        }
    }
}

// I removed QuizChatScreen  and replaced with the generic ChatUi
package com.example.studybuddy.ui.screens

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.studybuddy.ui.ChatViewModel
import com.example.studybuddy.ui.OnboardingViewModel
import com.example.studybuddy.ui.components.ChatUi
import com.example.studybuddy.ui.components.SubjectSelectionScreen

@Composable
fun HomeworkHelpScreen(
    onboardingViewModel: OnboardingViewModel,
    chatViewModel: ChatViewModel = viewModel(),
    chatId: String?
) {
    val context = LocalContext.current
    val userFirstName by onboardingViewModel.firstName
    var chatStarted by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = chatId) {
        if (chatId != null && chatId != "new") {
            chatViewModel.loadChatSession(context, chatId.toInt())
            chatStarted = true
        } else {
            // reset for a new chat
            chatViewModel.startConversation("", userFirstName ?: "User")
            chatStarted = false
        }
    }

    // save the chat session on dispose if it's a new chat
    DisposableEffect(chatId) {
        onDispose {
            if (chatId == "new" && chatViewModel.messages.size > 1) {
                chatViewModel.saveChat(context)
            }
        }
    }

    if (!chatStarted) {
        SubjectSelectionScreen(
            title = "What subject do you need help with?",
            subjects = onboardingViewModel.subjectDetailsMap.keys.toList()
        ) {
            subject ->
            chatViewModel.startConversation(subject, userFirstName ?: "User")
            chatStarted = true
        }
    } else {
        ChatUi(
            messages = chatViewModel.messages,
            userName = userFirstName ?: "You",
            onSendMessage = { message -> chatViewModel.sendMessage(message) } // correct call
        )
    }
}

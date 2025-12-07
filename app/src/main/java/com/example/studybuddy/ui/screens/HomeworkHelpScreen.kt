package com.example.studybuddy.ui.screens

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.studybuddy.network.ProfileResponse
import com.example.studybuddy.network.RetrofitInstance
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
    var profile by remember { mutableStateOf<ProfileResponse?>(null) }
    var chatStarted by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = chatId) {
        val response = RetrofitInstance.getAuthApi(context).getProfile()
        if (response.isSuccessful) {
            profile = response.body()
        }

        if (chatId != null && chatId != "new") {
            chatViewModel.loadChatSession(context, chatId.toInt())
            chatStarted = true
        } else {
            chatStarted = false
        }
    }
    
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
            val userName = profile?.first_name ?: "User"
            chatViewModel.startConversation(subject, userName)
            chatStarted = true
        }
    } else {
        profile?.let {
            // FIX THISSSS: Use the correct ChatViewModel,  ChatUi
            ChatUi(
                messages = chatViewModel.messages,
                userName = it.first_name ?: "You",
                onSendMessage = { chatViewModel.sendMessage(it) }
            )
        }
    }
}

package com.example.studybuddy.ui

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studybuddy.network.ChatMessage // add missing importhhak;jsdh
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import kotlinx.coroutines.launch

class QuizViewModel : ViewModel() {

    private val _messages = mutableStateListOf<ChatMessage>()
    val messages: List<ChatMessage> = _messages

    private val generativeModel = Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
        modelName = "gemini-2.0-flash-001",
        systemInstruction = content(role = "system") {
            text("""
                You are El, a friendly and encouraging study buddy. Your goal is to quiz the user to help them study.
                Here is the flow you must follow:
                1. The user will provide their name, a subject, and a list of topics. Greet them by name and confirm you see the topics.
                2. Announce that you will create a 10-question quiz on the first topic.
                3. Present all 10 questions to the user in a single, numbered list. The questions can be multiple-choice, fill-in-the-blank, or short-answer.
                4. After listing the questions, ask the user to answer question 1. Wait for their answer, then ask for the answer to question 2, and so on, until all 10 questions are answered. Do not tell them if they are right or wrong yet.
                5. Once all questions are answered, review their answers. In a friendly and encouraging tone, state which question numbers they got wrong. Do not reveal the correct answers yet.
                6. Ask the user if they would like to review the questions they missed.
                7. If the user says yes, go through each incorrect question one by one. For each one, provide the correct answer and a brief, clear explanation of why it's correct.
                8. After reviewing, or if the user initially said no to reviewing, ask them if they would like to start a quiz on the next topic from their list.
            """.trimIndent())
        }
    )

    private var chat = generativeModel.startChat(history = emptyList())

    fun sendMessage(userMessage: String) {
        _messages.add(ChatMessage(userMessage, true))

        viewModelScope.launch {
            try {
                val response = chat.sendMessage(userMessage)
                response.text?.let {
                    _messages.add(ChatMessage(it, false))
                }
            } catch (e: Exception) {
                Log.e("QuizViewModel", "Error sending message", e)
                _messages.add(ChatMessage("Sorry, I had a problem processing that. Let\'s try again.", false))
            }
        }
    }

    fun startQuiz(subject: String, topics: String, userName: String) {
        _messages.clear()
        val initialMessage = "My name is $userName. I want to be quizzed on the subject of $subject. The topics are: $topics."
        sendMessage(initialMessage)
    }
}
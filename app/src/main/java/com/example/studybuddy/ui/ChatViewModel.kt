package com.example.studybuddy.ui

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studybuddy.network.ChatMessage // FIX: import the correct ChatMessage class
import com.example.studybuddy.network.CreateChatRequest
import com.example.studybuddy.network.RetrofitInstance
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.QuotaExceededException
import com.google.firebase.ai.type.content
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter



class ChatViewModel : ViewModel() {

    private val _messages = mutableStateListOf<ChatMessage>()
    val messages: List<ChatMessage> = _messages
    private var sessionId: Int? = null

    private val generativeModel = Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
        modelName = "gemini-2.5-flash",
        systemInstruction = content(role = "system") {
            text("You are El, a friendly and encouraging study buddy. Your goal is to help students with their homework by guiding them to the answer, not by giving it away. Ask questions, provide hints, and break down problems into smaller steps. Always be patient and positive. The user's name will be provided at the start of the conversation; use it occasionally.")
        }
    )

    private var chat = generativeModel.startChat(history = emptyList())

    fun loadChatSession(context: Context, chatId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.getAuthApi(context).getChatSession(chatId)
                if (response.isSuccessful && response.body() != null) {
                    _messages.clear()
                    _messages.addAll(response.body()!!.messages)
                    sessionId = response.body()!!.id
                } else {
                    _messages.add(ChatMessage("Could not load this chat session.", false))
                }
            } catch (e: Exception) {
                _messages.add(ChatMessage("An error occurred while loading the chat.", false))
            }
        }
    }

    fun sendMessage(userMessage: String) {
        _messages.add(ChatMessage(userMessage, true))

        viewModelScope.launch {
            try {
                val response = chat.sendMessage(userMessage)
                response.text?.let {
                    _messages.add(ChatMessage(it, false))
                }
            } catch (e: QuotaExceededException) {
                _messages.add(ChatMessage("I'm a bit busy at the moment. Let me think for a moment...", false))
            } catch (e: Exception) {
                _messages.add(ChatMessage("Sorry, I had a problem processing that. Let\'s try again.", false))
            }
        }
    }

    fun startConversation(subject: String, userName: String) {
        _messages.clear()
        sessionId = null
        val initialMessage = "My name is $userName and I need some help with my $subject homework."
        
        viewModelScope.launch {
             try {
                chat = generativeModel.startChat(history = emptyList())
                val response = chat.sendMessage(initialMessage)
                response.text?.let {
                    _messages.add(ChatMessage(it, false))
                }
            } catch (e: Exception) {
                 _messages.add(ChatMessage("Sorry, I had a problem starting our chat. Please select a subject again.", false))
            }
        }
    }

    fun saveChat(context: Context) {
        if (sessionId != null) return // dont save an already saved chat

        viewModelScope.launch {
            try {
                val title = "Chat from ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))}"
                RetrofitInstance.getAuthApi(context).saveChatSession(CreateChatRequest(title, _messages.toList()))
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to save chat session", e)
            }
        }
    }
}
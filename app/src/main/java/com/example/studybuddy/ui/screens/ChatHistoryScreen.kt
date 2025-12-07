package com.example.studybuddy.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.example.studybuddy.network.ChatSessionSummary
import com.example.studybuddy.network.RetrofitInstance
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun ChatHistoryScreen(navController: NavController) {
    var chatSessions by remember { mutableStateOf<List<ChatSessionSummary>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var sessionToDelete by remember { mutableStateOf<ChatSessionSummary?>(null) }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                coroutineScope.launch {
                    try {
                        val response = RetrofitInstance.getAuthApi(context).getChatSessions()
                        if (response.isSuccessful) {
                            chatSessions = response.body()?.sortedByDescending { it.created_at }?.take(10) ?: emptyList()
                        } else {
                            errorMessage = "Failed to load chat history."
                        }
                    } catch (e: Exception) {
                        errorMessage = "An error occurred while loading chat history."
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // confirm delete
    sessionToDelete?.let { session ->
        AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            title = { Text("Delete Chat?") },
            text = { Text("Are you sure you want to delete this chat session? This action cannot be undone.") },
            confirmButton = {
                Button(onClick = {
                    coroutineScope.launch {
                        try {
                            val response = RetrofitInstance.getAuthApi(context).deleteChatSession(session.id)
                            if (response.isSuccessful) {
                                chatSessions = chatSessions.filter { it.id != session.id }
                            }
                        } catch (e: Exception) {
                            // handle error
                        }
                        sessionToDelete = null
                    }
                }) { Text("Yes, Delete") }
            },
            dismissButton = { Button(onClick = { sessionToDelete = null }) { Text("Cancel") } }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Homework Help", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate("homework_help/new") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start a New Chat")
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (errorMessage != null) {
            Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item { 
                    Text("Previous Chats", style = MaterialTheme.typography.titleMedium)
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
                if (chatSessions.isEmpty()) {
                    item {
                        Text("No saved chats yet.", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 16.dp))
                    }
                } else {
                    items(chatSessions) {
                        session ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navController.navigate("homework_help/${session.id}") }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val formattedDate = try {
                                LocalDateTime.parse(session.created_at, DateTimeFormatter.ISO_DATE_TIME).format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))
                            } catch (e: Exception) {
                                session.created_at
                            }
                            Text(text = "Chat from $formattedDate")
                            IconButton(onClick = { sessionToDelete = session }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Chat")
                            }
                        }
                        Divider()
                    }
                }
            }
        }
    }
}
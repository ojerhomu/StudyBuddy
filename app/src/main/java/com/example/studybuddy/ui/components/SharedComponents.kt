package com.example.studybuddy.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.studybuddy.R
import com.example.studybuddy.network.ChatMessage // MAKE THE IMPORT PATH CORRECT AA

@Composable
fun SubjectSelectionScreen(title: String, subjects: List<String>, onSubjectSelected: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        subjects.forEach { subject ->
            Button(onClick = { onSubjectSelected(subject) }, modifier = Modifier.fillMaxWidth(0.8f)) {
                Text(subject)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun ChatUi(
    messages: List<ChatMessage>,
    userName: String,
    onSendMessage: (String) -> Unit
) {
    var userInput by remember { mutableStateOf("") }
    val elColor = Color(0xFF6A0DAD)
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .imePadding(),
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            state = listState
        ) {
            items(messages) { chatMessage ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    if (chatMessage.isFromUser) {
                        Text("$userName: ", fontWeight = FontWeight.Bold)
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.el_icon),
                            contentDescription = "El Icon",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = chatMessage.message,
                        color = if (chatMessage.isFromUser) LocalContentColor.current else elColor,
                    )
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(value = userInput, onValueChange = { userInput = it }, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { 
                val messageToSend = userInput
                if (messageToSend.isNotBlank()) {
                    onSendMessage(messageToSend)
                    userInput = ""
                }
             }) {
                Text("Send")
            }
        }
    }
}
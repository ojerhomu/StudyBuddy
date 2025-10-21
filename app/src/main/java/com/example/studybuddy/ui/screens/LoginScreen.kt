//TODO: typing todo like that makes a little notification, that's pretty cool! Use this from now on
package com.example.studybuddy.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend



@Composable
fun LoginScreen(onLogin: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }


    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text("Study Buddy: Login")
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") })
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") })
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
// TODO: call backend auth API. For this prototype, just navigate.
            onLogin()
        }) {
            Text("Login")
        }


        // testing crap
        var generatedText by remember { mutableStateOf("Loading...") }

        LaunchedEffect(Unit) {
            val model = Firebase.ai(backend = GenerativeBackend.googleAI())
                .generativeModel("gemini-2.5-flash")

            val prompt = "In a sentence or two, greet the user and ask a question about what or how they will be focusing on studying that day."
            try {
                val response = model.generateContent(prompt)
                generatedText = response.text ?: "No response from model."
            } catch (e: Exception) {
                generatedText = "Failed to load content."
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = generatedText)
    }
}
package com.example.studybuddy.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.studybuddy.network.RetrofitInstance
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LoginScreen(onLogin: () -> Unit, onNavigateToRegister: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text("Study Buddy: Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email, 
            onValueChange = { email = it }, 
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password, 
            onValueChange = { password = it }, 
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = RetrofitInstance.getInstance(context).login(email, password)
                        withContext(Dispatchers.Main) {
                            if (response.isSuccessful) {
                                val token = response.body()?.access_token
                                if (token != null) {
                                    val sharedPref = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
                                    with(sharedPref.edit()) {
                                        putString("JWT_TOKEN", token)
                                        commit()
                                    }
                                    onLogin()
                                } else {
                                    Toast.makeText(context, "Login failed: Token not received.", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "Invalid credentials", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onNavigateToRegister) {
            Text("Don't have an account? Register")
        }

        Spacer(modifier = Modifier.height(24.dp))

        var generatedText by remember { mutableStateOf("Loading...") }
        LaunchedEffect(Unit) {
            try {
                val model = Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel("gemini-2.5-flash")
                val prompt = "In a sentence or two, greet the user and ask a question about what or how they will be focusing on studying that day."
                generatedText = model.generateContent(prompt).text ?: "No response from model."
            } catch (e: Exception) {
                generatedText = "Failed to load content."
            }
        }
        Text(text = generatedText)
    }
}
package com.example.studybuddy.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.studybuddy.network.LoginRequest
import com.example.studybuddy.network.RetrofitInstance
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LoginScreen(onLogin: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text("Study Buddy: Login")
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") })
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitInstance.getInstance(context).login(LoginRequest(email, password))

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            val token = response.body()?.accessToken // the correct property name
                            if (token != null) {
                                // save the stupid token to SharedPreferences
                                val sharedPref = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
                                with(sharedPref.edit()) {
                                    putString("JWT_TOKEN", token)
                                    commit() // use commit() to save synchronously
                                }
                                println("Login successful, token: $token")
                                Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
                                onLogin() // go to the main menu
                            } else {
                                Toast.makeText(context, "Login failed: Token not received.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            val errorBody = response.errorBody()?.string()
                            println("Login failed with code: ${response.code()}, error: $errorBody")
                            Toast.makeText(context, "Invalid credentials", Toast.LENGTH_SHORT).show()
                        }
                    }

                } catch (e: Exception) {
                    println("Login exception: ${e.message}")
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // testing crap
        var generatedText by remember { mutableStateOf("Loading...") }
        LaunchedEffect(Unit) {
            try {
                val model = Firebase.ai(backend = GenerativeBackend.googleAI())
                    .generativeModel("gemini-2.5-flash")

                val prompt = "In a sentence or two, greet the user and ask a question about what or how they will be focusing on studying that day."
                val response = model.generateContent(prompt)
                generatedText = response.text ?: "No response from model."
            } catch (e: Exception) {
                generatedText = "Failed to load content."
            }
        }
        Text(text = generatedText)
    }
}
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.studybuddy.network.RetrofitInstance
import com.example.studybuddy.network.UserCreateRequest
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(onRegisterSuccess: () -> Unit, onBackToLogin: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Create an Account", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = {
            coroutineScope.launch {
                try {
                    val registerResponse = RetrofitInstance.publicApi.register(UserCreateRequest(email, password))
                    if (registerResponse.isSuccessful) {
                        // auto login after successful registration
                        val loginResponse = RetrofitInstance.publicApi.login(email, password)
                        if (loginResponse.isSuccessful) {
                            val token = loginResponse.body()?.access_token
                            if (token != null) {
                                val sharedPref = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
                                sharedPref.edit().putString("JWT_TOKEN", token).commit()
                                // go to onboarding only after token is saved
                                onRegisterSuccess()
                            } else {
                                Toast.makeText(context, "Auto-login failed: Token not received.", Toast.LENGTH_LONG).show()
                            }
                        } else {
                             Toast.makeText(context, "Auto-login failed after registration.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        val errorBody = registerResponse.errorBody()?.string()
                        Toast.makeText(context, "Registration failed: $errorBody", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }) {
            Text("Register")
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onBackToLogin) {
            Text("Back to Login")
        }
    }
}
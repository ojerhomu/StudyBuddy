//TODO: typing todo like that makes a little notification, that's pretty cool! Use this from now on
package com.example.studybuddy.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun LoginScreen(onLogin: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }


    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
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
    }
}
package com.example.studybuddy.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.studybuddy.R
import com.example.studybuddy.ui.components.SpeechBubble

@Composable
fun OnboardingWelcomeScreen(onFinish: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // use the new speechbubble composable with the updated text
        SpeechBubble(
            text = "Hey! I'm El and welcome to StudyBuddy, I'll be here to help you study, organize, and succeed in your academic journey! Go ahead and explore the app's features!"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // use a  image composable for the PNG
        Image(
            painter = painterResource(id = R.drawable.mascot_el), //call from drawable put all drawings here
            contentDescription = "Mascot El",
            modifier = Modifier.size(200.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onFinish) {
            Text("Let's Get Started!")
        }
    }
}
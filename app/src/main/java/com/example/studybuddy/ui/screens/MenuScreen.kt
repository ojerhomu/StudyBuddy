package com.example.studybuddy.ui.screens

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.example.studybuddy.R
import com.example.studybuddy.ui.MenuViewModel
import com.example.studybuddy.ui.OnboardingViewModel
import com.example.studybuddy.ui.UpcomingDisplayItem
import com.example.studybuddy.ui.components.SpeechBubbleWithContent
import java.time.format.DateTimeFormatter

@Composable
fun MenuScreen(
    onNavigate: (String) -> Unit,
    menuViewModel: MenuViewModel = viewModel(),
    onboardingViewModel: OnboardingViewModel
) {
    val context = LocalContext.current
    val imageLoader = ImageLoader.Builder(context)
        .components {
            if (Build.VERSION.SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()

    val subjectDetailsMap = onboardingViewModel.subjectDetailsMap

    LaunchedEffect(subjectDetailsMap.isNotEmpty()) {
        if (subjectDetailsMap.isNotEmpty()) {
            menuViewModel.loadData(context, subjectDetailsMap)
        }
    }

    val greeting by menuViewModel.greeting
    val soonestTask by menuViewModel.soonestTask
    val todaysClasses by menuViewModel.todaysClasses
    val todaysStudySessions by menuViewModel.todaysStudySessions

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()), // FIX: Make the entire screen scrollable because that speech bubble can get big as shit
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SpeechBubbleWithContent {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Here\'s what\'s coming up:", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))

                if (soonestTask == null && todaysClasses.isEmpty() && todaysStudySessions.isEmpty()) {
                    Text("Nothing on your schedule yet!", style = MaterialTheme.typography.bodyMedium)
                } else {
                    soonestTask?.let {
                        val formattedDate = it.dateTime.format(DateTimeFormatter.ofPattern("MMM d, h:mm a"))
                        Text("• ${it.description} on $formattedDate", modifier = Modifier.padding(vertical = 4.dp))
                    }
                    if (todaysClasses.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Today\'s Classes:", style = MaterialTheme.typography.titleMedium)
                        todaysClasses.forEach { item ->
                            val formattedTime = "${item.startTime.format(DateTimeFormatter.ofPattern("h:mm a"))} - ${item.endTime.format(DateTimeFormatter.ofPattern("h:mm a"))}"
                            Text("• ${item.subject}: $formattedTime", modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                    if (todaysStudySessions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Today\'s Study Sessions:", style = MaterialTheme.typography.titleMedium)
                        todaysStudySessions.forEach { item ->
                            val formattedTime = item.dateTime.format(DateTimeFormatter.ofPattern("h:mm a"))
                            Text("• ${item.description} at $formattedTime", modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        AsyncImage(
            model = R.drawable.mascot_el1,
            imageLoader = imageLoader,
            contentDescription = "Mascot El",
            modifier = Modifier.size(200.dp)
        )

        // buttons are apart of scrollable
        Column(
            modifier = Modifier.fillMaxWidth(0.81f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(modifier = Modifier.fillMaxWidth(), onClick = { onNavigate("study_scheduler_menu") }) { Text("Study Scheduler") }
            Spacer(modifier = Modifier.height(12.dp))
            Button(modifier = Modifier.fillMaxWidth(), onClick = { onNavigate("task_submenu") }) { Text("Assignments & Tasks") }
            Spacer(modifier = Modifier.height(12.dp))
            Button(modifier = Modifier.fillMaxWidth(), onClick = { onNavigate("calendar") }) { Text("Calendar") }
            Spacer(modifier = Modifier.height(12.dp))
            Button(modifier = Modifier.fillMaxWidth(), onClick = { onNavigate("practice") }) { Text("Practice / Quiz") }
            Spacer(modifier = Modifier.height(12.dp))
            Button(modifier = Modifier.fillMaxWidth(), onClick = { onNavigate("settings") }) { Text("Settings") }
            Spacer(modifier = Modifier.height(12.dp))
            Button(modifier = Modifier.fillMaxWidth(), onClick = { onNavigate("profile") }) { Text("My Profile") }
        }
    }
}
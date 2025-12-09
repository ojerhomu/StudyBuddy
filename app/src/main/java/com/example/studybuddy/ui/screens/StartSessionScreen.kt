package com.example.studybuddy.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.studybuddy.R
import com.example.studybuddy.ui.OnboardingViewModel
import com.example.studybuddy.ui.TimerViewModel
import com.example.studybuddy.ui.components.DurationPickerDialog
import com.example.studybuddy.ui.components.SpeechBubbleWithContent
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

enum class TimerState { STUDY, SHORT_BREAK, LONG_BREAK }

@Composable
fun StartSessionScreen(
    onboardingViewModel: OnboardingViewModel,
    timerViewModel: TimerViewModel = viewModel()
) {
    val context = LocalContext.current

    // make sure user profile (and pomodoro prefs) are loaded when this screen appears
    LaunchedEffect(Unit) {
        onboardingViewModel.loadUserProfile(context)
        // if you also want schedule here, uncomment:
        // onboardingViewModel.loadUserSchedule(context)
    }

    var currentOnboardingStep by remember { mutableStateOf(0) }
    val userFirstName by onboardingViewModel.firstName

    // read pomodoro prefs safely (treat null as 0)
    val studyMinutes = onboardingViewModel.pomodoroStudyMinutes.value ?: 0
    val shortBreakMinutes = onboardingViewModel.pomodoroShortBreakMinutes.value ?: 0
    val longBreakMinutes = onboardingViewModel.pomodoroLongBreakMinutes.value ?: 0

    // consider preferences "set" only when they are non-zero
    val preferencesSet =
        studyMinutes != 0 && shortBreakMinutes != 0 && longBreakMinutes != 0

    // initialize local durations from VM values,
    // and re-init when the backend values change from 0 -> real values
    var studyDuration by remember(studyMinutes) { mutableStateOf(studyMinutes.minutes) }
    var shortBreakDuration by remember(shortBreakMinutes) { mutableStateOf(shortBreakMinutes.minutes) }
    var longBreakDuration by remember(longBreakMinutes) { mutableStateOf(longBreakMinutes.minutes) }

    var showDurationPickerFor by remember { mutableStateOf<String?>(null) }
    var editPreferences by remember { mutableStateOf(false) }
    var showTimerScreen by remember { mutableStateOf(false) }

    var timerState by remember { mutableStateOf(TimerState.STUDY) }
    var sessionCount by remember { mutableStateOf(0) }
    var showTimeUpDialog by remember { mutableStateOf(false) }
    var showLongBreakDialog by remember { mutableStateOf(false) }

    val time by timerViewModel.time.collectAsState()
    val isRunning by timerViewModel.isRunning.collectAsState()
    val wasRunning = remember { mutableStateOf(isRunning) }

    LaunchedEffect(isRunning) {
        if (wasRunning.value && !isRunning && time == Duration.ZERO) {
            showTimeUpDialog = true
        }
        wasRunning.value = isRunning
    }

    if (showDurationPickerFor != null) {
        DurationPickerDialog(
            title = "Set $showDurationPickerFor",
            onDismissRequest = { showDurationPickerFor = null },
            onDurationSet = { hours, minutes ->
                val duration = hours.hours + minutes.minutes
                when (showDurationPickerFor) {
                    "Study Duration" -> studyDuration = duration
                    "Short Break" -> shortBreakDuration = duration
                    "Long Break" -> longBreakDuration = duration
                }
                showDurationPickerFor = null
            },
            showHours = showDurationPickerFor != "Short Break"
        )
    }

    if (showTimeUpDialog) {
        TimeUpDialog(
            onDismiss = {
                showTimeUpDialog = false
                timerViewModel.reset()
                timerState = TimerState.STUDY
                sessionCount = 0
                showTimerScreen = false
            },
            onConfirm = {
                showTimeUpDialog = false
                if (timerState == TimerState.STUDY) {
                    sessionCount++
                    if (sessionCount % 3 == 0) {
                        showLongBreakDialog = true
                    } else {
                        timerState = TimerState.SHORT_BREAK
                        timerViewModel.setTime(shortBreakDuration)
                        timerViewModel.start()
                    }
                } else {
                    timerState = TimerState.STUDY
                    timerViewModel.setTime(studyDuration)
                    timerViewModel.start()
                }
            },
            timerState = timerState
        )
    }

    if (showLongBreakDialog) {
        LongBreakDialog(
            onDismiss = {
                showLongBreakDialog = false
                timerViewModel.reset()
                timerState = TimerState.STUDY
                sessionCount = 0
                showTimerScreen = false
            },
            onLongBreak = {
                showLongBreakDialog = false
                sessionCount = 0
                timerState = TimerState.LONG_BREAK
                timerViewModel.setTime(longBreakDuration)
                timerViewModel.start()
            },
            onShortBreak = {
                showLongBreakDialog = false
                timerState = TimerState.SHORT_BREAK
                timerViewModel.setTime(shortBreakDuration)
                timerViewModel.start()
            }
        )
    }

    if (preferencesSet && !editPreferences) {
        if (showTimerScreen) {
            when (timerState) {
                TimerState.STUDY -> TimerScreen(
                    time = time,
                    title = "Study Session",
                    onStart = { timerViewModel.start() },
                    onPause = { timerViewModel.pause() },
                    onQuit = {
                        timerViewModel.reset()
                        timerState = TimerState.STUDY
                        sessionCount = 0
                        showTimerScreen = false
                    },
                    isRunning = isRunning,
                    imageRes = R.drawable.el_study,
                    backgroundColor = Color(0xFFFDE9E9)
                )

                TimerState.SHORT_BREAK -> TimerScreen(
                    time = time,
                    title = "Short Break",
                    onStart = { timerViewModel.start() },
                    onPause = { timerViewModel.pause() },
                    onQuit = {
                        timerViewModel.reset()
                        timerState = TimerState.STUDY
                        sessionCount = 0
                        showTimerScreen = false
                    },
                    isRunning = isRunning,
                    imageRes = R.drawable.el_break,
                    backgroundColor = Color(0xFFE9F1FD)
                )

                TimerState.LONG_BREAK -> TimerScreen(
                    time = time,
                    title = "Long Break",
                    onStart = { timerViewModel.start() },
                    onPause = { timerViewModel.pause() },
                    onQuit = {
                        timerViewModel.reset()
                        timerState = TimerState.STUDY
                        sessionCount = 0
                        showTimerScreen = false
                    },
                    isRunning = isRunning,
                    imageRes = R.drawable.el_break,
                    backgroundColor = Color(0xFFE9FDEB)
                )
            }
        } else {
            ReadyToStartScreen(
                studyDuration = studyDuration,
                shortBreakDuration = shortBreakDuration,
                longBreakDuration = longBreakDuration,
                onStart = {
                    timerViewModel.setTime(studyDuration)
                    timerViewModel.start()
                    showTimerScreen = true
                },
                onEdit = { editPreferences = true }
            )
        }
    } else {
        // onboarding / preference setting flow
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (currentOnboardingStep < 2 && !preferencesSet) {
                SpeechBubbleWithContent {
                    Text(
                        when (currentOnboardingStep) {
                            0 -> "Hey ${userFirstName ?: "there"}, let's go ahead and lock-in on studying with no distractions on your phone! I'll use a method called the pomodoro timer which focuses on focused study sessions followed by short breaks. After about three study sessions, you'll be rewarded with a longer break!"
                            else -> "Let's start by setting your study preferences which you can change at any time!"
                        }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                AsyncImage(
                    model = R.drawable.mascot_el1,
                    contentDescription = "Mascot El",
                    modifier = Modifier.size(200.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { currentOnboardingStep++ }) {
                    Text("Next")
                }
            } else {
                // pref setting
                PreferenceItem(
                    "How long would you like to study for?",
                    studyDuration
                ) { showDurationPickerFor = "Study Duration" }
                PreferenceItem(
                    "How long do you want your short break to be?",
                    shortBreakDuration
                ) { showDurationPickerFor = "Short Break" }
                PreferenceItem(
                    "How long do you want your long break to be?",
                    longBreakDuration
                ) { showDurationPickerFor = "Long Break" }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        onboardingViewModel.savePomodoroPreferences(
                            context,
                            studyDuration.inWholeMinutes.toInt(),
                            shortBreakDuration.inWholeMinutes.toInt(),
                            longBreakDuration.inWholeMinutes.toInt()
                        )
                        editPreferences = false
                    },
                    enabled = studyDuration.inWholeMinutes > 0 &&
                            shortBreakDuration.inWholeMinutes > 0 &&
                            longBreakDuration.inWholeMinutes > 0
                ) {
                    Text("Save")
                }
            }
        }
    }
}

@Composable
private fun ReadyToStartScreen(
    studyDuration: Duration,
    shortBreakDuration: Duration,
    longBreakDuration: Duration,
    onStart: () -> Unit,
    onEdit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Are you ready to start your study session?",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        PreferenceItem("Study Duration", studyDuration) {}
        PreferenceItem("Short Break", shortBreakDuration) {}
        PreferenceItem("Long Break", longBreakDuration) {}
        Spacer(modifier = Modifier.height(32.dp))
        Row {
            Button(onClick = onStart) {
                Text("Start")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = onEdit) {
                Text("Edit")
            }
        }
    }
}

@Composable
private fun PreferenceItem(text: String, duration: Duration, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(vertical = 12.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = duration.toComponents { hours, minutes, _, _ ->
                if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
            },
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
    }
}

@Composable
private fun TimerScreen(
    time: Duration,
    title: String,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onQuit: () -> Unit,
    isRunning: Boolean,
    imageRes: Int,
    backgroundColor: Color
) {
    val coroutineScope = rememberCoroutineScope()
    var encouragement by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val model = Firebase.ai(backend = GenerativeBackend.googleAI())
                    .generativeModel("gemini-2.5-flash")
                val prompt =
                    "Either: Write a sentence or two of encouragement for a student who's currently studying with a timer OR give a fun fact about how the student studying is statistically improving their chances of getting a better grade!"
                encouragement = model.generateContent(prompt).text ?: "Keep going!"
            } catch (e: Exception) {
                Log.e("TimerScreen", "Failed to generate encouragement", e)
                encouragement = "You're doing great!"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        SpeechBubbleWithContent {
            Text(text = encouragement)
        }
        AsyncImage(
            model = imageRes,
            contentDescription = "Mascot El",
            modifier = Modifier.size(200.dp)
        )
        Text(text = title, style = MaterialTheme.typography.headlineLarge)
        Text(
            text = time.toComponents { hours, minutes, seconds, _ ->
                String.format("%02d:%02d:%02d", hours, minutes, seconds)
            },
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 80.sp)
        )
        Row {
            Button(onClick = if (isRunning) onPause else onStart) {
                Text(if (isRunning) "Pause" else "Start")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = onQuit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Quit")
            }
        }
    }
}

@Composable
private fun TimeUpDialog(onDismiss: () -> Unit, onConfirm: () -> Unit, timerState: TimerState) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Time's Up!") },
        text = {
            Text(
                if (timerState == TimerState.STUDY)
                    "Time for a break!"
                else
                    "Time to get back to it!"
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(
                    if (timerState == TimerState.STUDY)
                        "Start Break"
                    else
                        "Go Back to Studying"
                )
            }
        }
    )
}

@Composable
private fun LongBreakDialog(
    onDismiss: () -> Unit,
    onLongBreak: () -> Unit,
    onShortBreak: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("You've earned a long break!") },
        text = { Text("You've completed 3 study sessions!") },
        confirmButton = {
            Button(onClick = onLongBreak) {
                Text("Start Long Break")
            }
        },
        dismissButton = {
            Button(onClick = onShortBreak) {
                Text("Go to Short Break Instead")
            }
        }
    )
}

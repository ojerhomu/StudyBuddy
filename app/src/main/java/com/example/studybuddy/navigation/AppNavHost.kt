package com.example.studybuddy.navigation

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
//screw it just import everything!!
import com.example.studybuddy.ui.screens.*

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Check if token exists to determine the start destination
    val sharedPref = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
    val token = sharedPref.getString("JWT_TOKEN", null)
    val startDestination = if (token != null) "menu" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(
                onLogin = { navController.navigate("menu") { popUpTo("login") { inclusive = true } } },
                onNavigateToRegister = { navController.navigate("register") }
            )
        }
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = { navController.navigate("login") { popUpTo("login") { inclusive = true } } },
                onBackToLogin = { navController.popBackStack() }
            )
        }
        composable("menu") { MenuScreen(onNavigate = { route -> navController.navigate(route) }) }

        // Study Scheduler Submenu
        composable("study_scheduler_menu") { StudySchedulerMenuScreen(onNavigate = { route -> navController.navigate(route) }) }

        // Study Session Flow
        composable("start_session") {
            StartSessionScreen(onNext = { studyDuration ->
                navController.navigate("break_duration_prompt/$studyDuration")
            })
        }
        composable(
            route = "break_duration_prompt/{studyDuration}",
            arguments = listOf(navArgument("studyDuration") { type = NavType.LongType })
        ) {
            val studyDuration = it.arguments?.getLong("studyDuration") ?: 0L
            BreakDurationScreen(studyDuration = studyDuration) {
                study, breakTime -> navController.navigate("study_timer/$study/$breakTime/false")
            }
        }
        composable(
            route = "study_timer/{studyDuration}/{breakDuration}/{isBreak}",
            arguments = listOf(
                navArgument("studyDuration") { type = NavType.LongType },
                navArgument("breakDuration") { type = NavType.LongType },
                navArgument("isBreak") { type = NavType.BoolType }
            )
        ) {
            val studyDuration = it.arguments?.getLong("studyDuration") ?: 0L
            val breakDuration = it.arguments?.getLong("breakDuration") ?: 0L
            val isBreak = it.arguments?.getBoolean("isBreak") ?: false
            StudyTimerScreen(
                studyDurationSeconds = studyDuration,
                breakDurationSeconds = breakDuration,
                isBreak = isBreak,
                onBreakStart = { navController.navigate("break_prompt/$studyDuration/$breakDuration") },
                onFinish = { navController.navigate("post_break") { popUpTo("menu") } },
                onCancel = { navController.navigate("menu") { popUpTo("menu") { inclusive = true } } }
            )
        }
        composable(
            route = "break_prompt/{studyDuration}/{breakDuration}",
            arguments = listOf(
                navArgument("studyDuration") { type = NavType.LongType },
                navArgument("breakDuration") { type = NavType.LongType }
            )
        ) {
            val studyDuration = it.arguments?.getLong("studyDuration") ?: 0L
            val breakDuration = it.arguments?.getLong("breakDuration") ?: 0L
            BreakPromptScreen(
                onStartBreak = { navController.navigate("study_timer/$studyDuration/$breakDuration/true") {
                    popUpTo("break_prompt/$studyDuration/$breakDuration") { inclusive = true }
                } }
            )
        }
        composable("post_break") {
            PostBreakScreen(
                onRestart = { navController.navigate("start_session") { popUpTo("menu") } },
                onFinish = { navController.navigate("menu") { popUpTo("menu") } }
            )
        }

        // New Task Management Flow
        composable("task_submenu") { TaskSubMenuScreen(onNavigate = { route -> navController.navigate(route) }) }
        composable("add_task") { AddTaskScreen(onTaskAdded = { navController.popBackStack() }) }
        composable("view_tasks") { ViewTasksScreen() }

        composable("schedule_session") { ScheduleSessionScreen(onSaveSuccess = { navController.popBackStack() }) }
        composable("edit_sessions") { EditSessionsScreen() }

        composable("calendar") { CalendarScreen() }
        composable("practice") { PracticeScreen() }
        composable("settings") { SettingsScreen() }
        composable("profile") {
            ProfileScreen(onLogout = {
                navController.navigate("login") {
                    popUpTo("menu") { inclusive = true }
                    launchSingleTop = true
                }
            })
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("$title (Placeholder)")
    }
}

@Composable
fun BreakPromptScreen(onStartBreak: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Time for a break!", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onStartBreak) {
            Text("OK")
        }
    }
}
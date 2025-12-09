package com.example.studybuddy.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.studybuddy.ui.OnboardingViewModel
import com.example.studybuddy.ui.screens.*

@Composable
fun AppNavHost(onboardingViewModel: OnboardingViewModel) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val sharedPref = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
    val token = sharedPref.getString("JWT_TOKEN", null)
    val startDestination = if (token != null) "menu" else "login"

    // when a freaking user is logged in, load their profile and schedule data
    if (token != null) {
        LaunchedEffect(Unit) {
            onboardingViewModel.loadUserProfile(context)
            onboardingViewModel.loadUserSchedule(context)
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(
                onLogin = { navController.navigate("menu") { popUpTo("login") { inclusive = true } } },
                onNavigateToRegister = { navController.navigate("register") }
            )
        }
        composable("register") {
            RegisterScreen(
                onboardingViewModel = onboardingViewModel,
                onRegisterSuccess = { navController.navigate("onboarding_name") },
                onBackToLogin = { navController.popBackStack() }
            )
        }

        // Onboarding & Settings Flow
        composable("onboarding_name") { OnboardingNameScreen(onboardingViewModel = onboardingViewModel) { navController.navigate("onboarding_education") } }
        composable("onboarding_education") { OnboardingEducationScreen { _ -> navController.navigate("onboarding_subjects") } }

        composable("onboarding_subjects") {
            val previousRoute = navController.previousBackStackEntry?.destination?.route
            OnboardingSubjectsScreen(
                onboardingViewModel = onboardingViewModel,
                onNext = {
                    if (previousRoute == "change_subjects") {
                        navController.popBackStack()
                    } else {
                        navController.navigate("onboarding_schedule")
                    }
                },
                shouldSaveOnDispose = (previousRoute == "change_subjects")
            )
        }

        composable("onboarding_schedule") {
            OnboardingScheduleScreen(onboardingViewModel = onboardingViewModel) {
                onboardingViewModel.saveUserSchedule(context)
                navController.navigate("onboarding_confirmation")
            }
        }
        composable("onboarding_confirmation") {
            ScheduleConfirmationScreen(
                onboardingViewModel = onboardingViewModel,
                onConfirm = { navController.navigate("onboarding_welcome") },
                onGoBack = { navController.popBackStack() }
            )
        }
        composable("onboarding_welcome") {
            OnboardingWelcomeScreen(onFinish = { navController.navigate("menu") { popUpTo("login") { inclusive = true } } })
        }
        composable("settings") { SettingsScreen(navController = navController) }
        composable("change_subjects") {
            ChangeSubjectsScreen(navController = navController, onboardingViewModel = onboardingViewModel) {
                onboardingViewModel.saveUserSchedule(context)
            }
        }
        composable("edit_schedule") {
            OnboardingScheduleScreen(onboardingViewModel = onboardingViewModel) {
                onboardingViewModel.saveUserSchedule(context)
                navController.popBackStack()
            }
        }

        // the main App Screens
        composable("menu") { MenuScreen(onNavigate = { route -> navController.navigate(route) }, onboardingViewModel = onboardingViewModel) }
        composable("calendar") { CalendarScreen(onboardingViewModel = onboardingViewModel) }
        composable("profile") { ProfileScreen(onLogout = { navController.navigate("login") { popUpTo("menu") { inclusive = true }; launchSingleTop = true } }, onboardingViewModel = onboardingViewModel) }
        composable("add_task") { AddTaskScreen(onTaskAdded = { navController.popBackStack() }, onboardingViewModel = onboardingViewModel) }
        composable("study_scheduler_menu") { StudySchedulerMenuScreen(onNavigate = { route -> navController.navigate(route) }) }
        composable("schedule_session") { ScheduleSessionScreen(onSaveSuccess = { navController.popBackStack() }) }
        composable("edit_sessions") { EditSessionsScreen() }
        composable("task_submenu") { TaskSubMenuScreen(onNavigate = { route -> navController.navigate(route) }) }
        composable("view_tasks") { ViewTasksScreen() }
        composable("start_session") { StartSessionScreen(onboardingViewModel) } // FIX: Add route and screen

        // flow for quiz and hw help
        composable("practice") { PracticeScreen(navController = navController) }
        composable("chat_history") { ChatHistoryScreen(navController = navController) }
        composable(
            route = "homework_help/{chatId}",
            arguments = listOf(navArgument("chatId") { type = NavType.StringType })
        ) {
            backStackEntry ->
            HomeworkHelpScreen(
                onboardingViewModel = onboardingViewModel,
                chatId = backStackEntry.arguments?.getString("chatId")
            )
        }
        composable("quiz_screen") { QuizScreen(onboardingViewModel = onboardingViewModel) }
    }
}
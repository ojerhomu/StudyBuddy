package com.example.studybuddy.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
//screw it just import everything!!
import com.example.studybuddy.ui.screens.LoginScreen
import com.example.studybuddy.ui.screens.MenuScreen
import com.example.studybuddy.ui.screens.PracticeScreen
import com.example.studybuddy.ui.screens.SchedulerScreen
import com.example.studybuddy.ui.screens.SettingsScreen
import com.example.studybuddy.ui.screens.TaskScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        //anything imported call here, use simple names when u do import them, no need to call
        composable("login") { LoginScreen(onLogin = { navController.navigate("menu") }) }
        composable("menu") { MenuScreen(onNavigate = { route -> navController.navigate(route) }) }
        composable("scheduler") { SchedulerScreen() }
        composable("tasks") { TaskScreen() }
        composable("practice") { PracticeScreen() }
        composable("settings") { SettingsScreen() }
    }
}

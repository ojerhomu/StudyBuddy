package com.example.studybuddy.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
//screw it just import everything!!
import com.example.studybuddy.ui.screens.LoginScreen
import com.example.studybuddy.ui.screens.MenuScreen
import com.example.studybuddy.ui.screens.PracticeScreen
import com.example.studybuddy.ui.screens.ProfileScreen
import com.example.studybuddy.ui.screens.SchedulerScreen
import com.example.studybuddy.ui.screens.SettingsScreen
import com.example.studybuddy.ui.screens.TaskScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
//TODO: ADD A MESSAGE WHY THEY GOT REDIRECTED TO LOGIN SCREEN (This prevents sending null or expired tokens.)
    // check if token exists to determine the start destination
    val sharedPref = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
    val token = sharedPref.getString("JWT_TOKEN", null)
    val startDestination = if (token != null) "menu" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(onLogin = {
                // navigate to menu and remove login from the back stack
                navController.navigate("menu") {
                    popUpTo("login") { inclusive = true }
                }
            })
        }
        composable("menu") { MenuScreen(onNavigate = { route -> navController.navigate(route) }) }
        composable("scheduler") { SchedulerScreen() }
        composable("tasks") { TaskScreen() }
        composable("practice") { PracticeScreen() }
        composable("settings") { SettingsScreen() }
        composable("profile") {
            ProfileScreen(onLogout = {
                navController.navigate("login") {
                    // pop everything up to the new start of the graph (menu)
                    popUpTo("menu") {
                        inclusive = true
                    }
                    // to make sure we don't have multiple copies of the login screen
                    launchSingleTop = true
                }
            })
        }

    }
}

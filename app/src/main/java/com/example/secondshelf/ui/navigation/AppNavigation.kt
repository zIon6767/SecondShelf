package com.example.secondshelf.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.secondshelf.ui.screens.HomeScreen
import com.example.secondshelf.ui.screens.LoginScreen
import com.example.secondshelf.ui.screens.NotificationsScreen
import com.example.secondshelf.ui.screens.OnboardingScreen
import com.example.secondshelf.ui.screens.RegisterScreen
import com.example.secondshelf.ui.screens.SetPasswordScreen
import com.example.secondshelf.ui.screens.SettingsScreen
import com.example.secondshelf.ui.screens.SplashScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {

        // ── Splash ──────────────────────────────────────────────────────
        composable("splash") {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate("onboarding") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        // ── Onboarding ──────────────────────────────────────────────────
        composable("onboarding") {
            OnboardingScreen(
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        // ── Login ───────────────────────────────────────────────────────
        composable("login") {
            LoginScreen(
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }

        // ── Register ────────────────────────────────────────────────────
        composable("register") {
            RegisterScreen(
                onNavigateToSetPassword = { name, email ->
                    val encodedName = Uri.encode(name)
                    val encodedEmail = Uri.encode(email)
                    navController.navigate("set_password?name=$encodedName&email=$encodedEmail")
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // ── Set Password ─────────────────────────────────────────────────
        composable(
            route = "set_password?name={name}&email={email}",
            arguments = listOf(
                navArgument("name") { type = NavType.StringType; defaultValue = "" },
                navArgument("email") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name") ?: ""
            SetPasswordScreen(
                userName = name,
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ── Home (with bottom nav) ───────────────────────────────────────
        composable("home") {
            HomeScreen(
                onNavigateToSettings = {
                    navController.navigate("settings")
                },
                onNavigateToNotifications = {
                    navController.navigate("notifications")
                }
            )
        }

        // ── Settings ─────────────────────────────────────────────────────
        composable("settings") {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ── Notifications ────────────────────────────────────────────────
        composable("notifications") {
            NotificationsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

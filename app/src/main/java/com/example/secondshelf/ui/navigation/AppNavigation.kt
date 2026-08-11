package com.example.secondshelf.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.secondshelf.ui.AuthViewModel
import com.example.secondshelf.ui.screens.HomeScreen
import com.example.secondshelf.ui.screens.LoginScreen
import com.example.secondshelf.ui.screens.NotificationsScreen
import com.example.secondshelf.ui.screens.OnboardingScreen
import com.example.secondshelf.ui.screens.RegisterScreen
import com.example.secondshelf.ui.screens.SetPasswordScreen
import com.example.secondshelf.ui.screens.SettingsScreen
import com.example.secondshelf.ui.screens.SplashScreen

@Composable
fun AppNavigation(authCallbackUri: Uri? = null) {
    val navController = rememberNavController()

    // One shared AuthViewModel for all auth screens (Activity-scoped)
    val authViewModel: AuthViewModel = viewModel()

    // Handle Google OAuth deep-link redirect forwarded from MainActivity
    LaunchedEffect(authCallbackUri) {
        authCallbackUri?.let { authViewModel.handleAuthCallback(it) }
    }

    // Auto-login: skip to Home if a valid session already exists
    LaunchedEffect(Unit) {
        if (authViewModel.isLoggedIn()) {
            navController.navigate("home") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

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

        // ── Login ────────────────────────────────────────────────────────
        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }

        // ── Register ─────────────────────────────────────────────────────
        composable("register") {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToSetPassword = { name, email, phone ->
                    val encodedName  = Uri.encode(name)
                    val encodedEmail = Uri.encode(email)
                    val encodedPhone = Uri.encode(phone)
                    navController.navigate(
                        "set_password?name=$encodedName&email=$encodedEmail&phone=$encodedPhone"
                    )
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ── Set Password ─────────────────────────────────────────────────
        composable(
            route = "set_password?name={name}&email={email}&phone={phone}",
            arguments = listOf(
                navArgument("name")  { type = NavType.StringType; defaultValue = "" },
                navArgument("email") { type = NavType.StringType; defaultValue = "" },
                navArgument("phone") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val name  = backStackEntry.arguments?.getString("name")  ?: ""
            val email = backStackEntry.arguments?.getString("email") ?: ""
            val phone = backStackEntry.arguments?.getString("phone") ?: ""
            SetPasswordScreen(
                viewModel = authViewModel,
                userName  = name,
                userEmail = email,
                userPhone = phone,
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ── Home (with bottom nav) ────────────────────────────────────────
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

        // ── Settings ──────────────────────────────────────────────────────
        composable("settings") {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ── Notifications ─────────────────────────────────────────────────
        composable("notifications") {
            NotificationsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

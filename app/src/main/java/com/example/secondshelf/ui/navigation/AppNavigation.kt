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
import com.example.secondshelf.ui.screens.*
import com.example.secondshelf.ui.viewmodel.MarketplaceViewModel

@Composable
fun AppNavigation(authCallbackUri: Uri? = null) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val marketplaceViewModel: MarketplaceViewModel = viewModel()

    LaunchedEffect(authCallbackUri) {
        authCallbackUri?.let { authViewModel.handleAuthCallback(it) }
    }

    LaunchedEffect(Unit) {
        if (authViewModel.isLoggedIn()) {
            navController.navigate("home") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(navController = navController, startDestination = "splash") {

        composable("splash") {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate("onboarding") { popUpTo("splash") { inclusive = true } }
                }
            )
        }

        composable("onboarding") {
            OnboardingScreen(
                onNavigateToLogin = {
                    navController.navigate("login") { popUpTo("onboarding") { inclusive = true } }
                }
            )
        }

        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToHome = {
                    navController.navigate("home") { popUpTo(0) { inclusive = true } }
                },
                onNavigateToRegister = { navController.navigate("register") }
            )
        }

        composable("register") {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToSetPassword = { name, email, phone ->
                    navController.navigate(
                        "set_password?name=${Uri.encode(name)}&email=${Uri.encode(email)}&phone=${Uri.encode(phone)}"
                    )
                },
                onNavigateToLogin = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate("home") { popUpTo(0) { inclusive = true } }
                }
            )
        }

        composable(
            route = "set_password?name={name}&email={email}&phone={phone}",
            arguments = listOf(
                navArgument("name") { type = NavType.StringType; defaultValue = "" },
                navArgument("email") { type = NavType.StringType; defaultValue = "" },
                navArgument("phone") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            SetPasswordScreen(
                viewModel = authViewModel,
                userName = backStackEntry.arguments?.getString("name") ?: "",
                userEmail = backStackEntry.arguments?.getString("email") ?: "",
                userPhone = backStackEntry.arguments?.getString("phone") ?: "",
                onNavigateToHome = {
                    navController.navigate("home") { popUpTo(0) { inclusive = true } }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("home") {
            HomeScreen(
                viewModel = marketplaceViewModel,
                onNavigateToNotifications = { navController.navigate("notifications") },
                onNavigateToMarketplace = { navController.navigate("marketplace") },
                onNavigateToListing = { id -> navController.navigate("listing/$id") },
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToCreateListing = { navController.navigate("create_listing") },
                onNavigateToSaved = { navController.navigate("saved") },
                onNavigateToMessages = { navController.navigate("messages") },
                onNavigateToEditProfile = { navController.navigate("edit_profile") },
                onLogout = {
                    authViewModel.signOut()
                    navController.navigate("login") { popUpTo(0) { inclusive = true } }
                }
            )
        }

        composable("marketplace") {
            MarketplaceScreen(
                viewModel = marketplaceViewModel,
                onNavigateBack = { navController.popBackStack() },
                onListingClick = { id -> navController.navigate("listing/$id") }
            )
        }

        composable(
            route = "listing/{listingId}",
            arguments = listOf(navArgument("listingId") { type = NavType.StringType })
        ) { backStackEntry ->
            val listingId = backStackEntry.arguments?.getString("listingId") ?: return@composable
            ListingDetailScreen(
                listingId = listingId,
                viewModel = marketplaceViewModel,
                onNavigateBack = { navController.popBackStack() },
                onContactSeller = { participantId, lid ->
                    navController.navigate("conversation/$participantId/$lid")
                },
                onViewSeller = { /* seller info shown in detail */ },
                onEditListing = { id -> navController.navigate("edit_listing/$id") }
            )
        }

        composable("create_listing") {
            CreateListingScreen(
                viewModel = marketplaceViewModel,
                onNavigateBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() }
            )
        }

        composable(
            route = "edit_listing/{listingId}",
            arguments = listOf(navArgument("listingId") { type = NavType.StringType })
        ) { backStackEntry ->
            val listingId = backStackEntry.arguments?.getString("listingId") ?: return@composable
            CreateListingScreen(
                viewModel = marketplaceViewModel,
                listingId = listingId,
                onNavigateBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() }
            )
        }

        composable("notifications") {
            NotificationsScreen(
                viewModel = marketplaceViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("settings") {
            SettingsScreen(
                viewModel = marketplaceViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditProfile = { navController.navigate("edit_profile") },
                onLogout = {
                    authViewModel.signOut()
                    navController.navigate("login") { popUpTo(0) { inclusive = true } }
                }
            )
        }

        composable("saved") {
            SavedItemsScreen(
                viewModel = marketplaceViewModel,
                onNavigateBack = { navController.popBackStack() },
                onListingClick = { id -> navController.navigate("listing/$id") }
            )
        }

        composable("messages") {
            MessagesScreen(
                viewModel = marketplaceViewModel,
                onNavigateBack = { navController.popBackStack() },
                onConversationClick = { participantId, listingId ->
                    navController.navigate("conversation/$participantId/$listingId")
                }
            )
        }

        composable(
            route = "conversation/{participantId}/{listingId}",
            arguments = listOf(
                navArgument("participantId") { type = NavType.StringType },
                navArgument("listingId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            ConversationScreen(
                participantId = backStackEntry.arguments?.getString("participantId") ?: return@composable,
                listingId = backStackEntry.arguments?.getString("listingId") ?: return@composable,
                viewModel = marketplaceViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("edit_profile") {
            EditProfileScreen(
                viewModel = marketplaceViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

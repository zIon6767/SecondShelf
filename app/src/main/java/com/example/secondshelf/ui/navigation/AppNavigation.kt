package com.example.secondshelf.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.secondshelf.ui.AuthUiState
import com.example.secondshelf.ui.AuthViewModel
import com.example.secondshelf.ui.screens.*
import com.example.secondshelf.ui.theme.SecondShelfTheme
import com.example.secondshelf.ui.viewmodel.MarketplaceViewModel

@Composable
fun AppNavigation(authCallbackUri: Uri? = null) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val marketplaceViewModel: MarketplaceViewModel = viewModel()
    val authUiState by authViewModel.uiState.collectAsState()

    // Trigger OAuth token exchange when the deep-link arrives
    LaunchedEffect(authCallbackUri) {
        authCallbackUri?.let { authViewModel.handleAuthCallback(it) }
    }

    // Navigate to Home or Complete Profile after a successful Google (or any) OAuth callback
    LaunchedEffect(authUiState) {
        if (authUiState is AuthUiState.Success) {
            authViewModel.resetState()
            if (authViewModel.isProfileComplete()) {
                navController.navigate("home") {
                    popUpTo(0) { inclusive = true }
                }
            } else {
                navController.navigate("complete_profile") {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    NavHost(navController = navController, startDestination = "splash") {

        composable("splash") {
            SplashScreen(
                onSplashFinished = {
                    if (authViewModel.isLoggedIn()) {
                        if (authViewModel.isProfileComplete()) {
                            navController.navigate("home") {
                                popUpTo("splash") { inclusive = true }
                            }
                        } else {
                            navController.navigate("complete_profile") {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                    } else if (authViewModel.hasSeenOnboarding()) {
                        navController.navigate("login") {
                            popUpTo("splash") { inclusive = true }
                        }
                    } else {
                        navController.navigate("onboarding") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                }
            )
        }

        composable("onboarding") {
            OnboardingScreen(
                onNavigateToLogin = {
                    authViewModel.setSeenOnboarding()
                    navController.navigate("login") { popUpTo("onboarding") { inclusive = true } }
                }
            )
        }

        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToHome = {
                    if (authViewModel.isProfileComplete()) {
                        navController.navigate("home") { popUpTo(0) { inclusive = true } }
                    } else {
                        navController.navigate("complete_profile") { popUpTo(0) { inclusive = true } }
                    }
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
                onNavigateToTerms = { navController.navigate("terms_and_conditions") },
                onNavigateToHome = {
                    if (authViewModel.isProfileComplete()) {
                        navController.navigate("home") { popUpTo(0) { inclusive = true } }
                    } else {
                        navController.navigate("complete_profile") { popUpTo(0) { inclusive = true } }
                    }
                }
            )
        }

        composable("terms_and_conditions") {
            TermsAndConditionsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("complete_profile") {
            CompleteProfileScreen(
                authViewModel = authViewModel,
                marketplaceViewModel = marketplaceViewModel,
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
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTerms = { navController.navigate("terms_and_conditions") }
            )
        }

        composable("home") {
            HomeScreen(
                viewModel = marketplaceViewModel,
                userName = authViewModel.getUserFullName(),
                userEmail = authViewModel.getUserEmail(),
                userCampus = authViewModel.getUserCampus(),
                userUniversity = authViewModel.getUserUniversity(),
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
                onNavigateToMyListings = { navController.navigate("create_listing") },
                onNavigateToSaved = { navController.navigate("saved") },
                onNavigateToChatSettings = { navController.navigate("chat_settings") },
                onNavigateToTerms = { navController.navigate("terms_and_conditions") },
                onNavigateToCampusSafety = { navController.navigate("campus_safety") },
                onNavigateToAbout = { navController.navigate("about") },
                onLogout = {
                    authViewModel.signOut()
                    navController.navigate("login") { popUpTo(0) { inclusive = true } }
                },
                onDeleteAccount = {
                    marketplaceViewModel.clearAllUserData()
                    authViewModel.deleteAccount {
                        navController.navigate("login") { popUpTo(0) { inclusive = true } }
                    }
                }
            )
        }

        composable("chat_settings") {
            ChatSettingsScreen(
                viewModel = marketplaceViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("campus_safety") {
            CampusSafetyScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("about") {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() }
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

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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.secondshelf.ui.components.InAppNotificationBanner
import com.example.secondshelf.ui.AuthUiState
import com.example.secondshelf.ui.AuthViewModel
import com.example.secondshelf.ui.screens.*
import com.example.secondshelf.ui.viewmodel.ChatViewModel
import com.example.secondshelf.ui.viewmodel.MarketplaceViewModel

@Composable
fun AppNavigation(
    authCallbackUri: Uri? = null,
    marketplaceViewModel: MarketplaceViewModel = viewModel()
) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
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
                navController.navigate("complete_profile")
            }
        }
    }

    val currentPopup = marketplaceViewModel.currentPopupNotification

    // Ensure incoming message listener is active whenever user is authenticated
    LaunchedEffect(authViewModel.getUserId()) {
        val uid = authViewModel.getUserId()
        if (uid.isNotBlank()) {
            marketplaceViewModel.startMessageNotificationListener(uid)
            marketplaceViewModel.loadConversationsFromDb(uid)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                        navController.navigate("complete_profile")
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
                        navController.navigate("complete_profile")
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
                },
                onNavigateBack = {
                    if (!navController.popBackStack()) {
                        authViewModel.signOut()
                        navController.navigate("login") { popUpTo(0) { inclusive = true } }
                    }
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

        composable("home") { backStackEntry ->
            val targetTab = backStackEntry.savedStateHandle.get<Int>("selected_tab")
            HomeScreen(
                viewModel = marketplaceViewModel,
                userId = authViewModel.getUserId(),
                userName = authViewModel.getUserFullName(),
                userEmail = authViewModel.getUserEmail(),
                userCampus = authViewModel.getUserCampus(),
                userUniversity = authViewModel.getUserUniversity(),
                userAvatarUrl = authViewModel.cachedAvatarUrl,
                targetTab = targetTab,
                onTabConsumed = { backStackEntry.savedStateHandle.remove<Int>("selected_tab") },
                onNavigateToNotifications = { navController.navigate("notifications") },
                onNavigateToMarketplace = { navController.navigate("marketplace") },
                onNavigateToListing = { id -> navController.navigate("listing/$id") },
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToCreateListing = { navController.navigate("create_listing") },
                onNavigateToSaved = { navController.navigate("saved") },
                onNavigateToMessages = { navController.navigate("messages") },
                onNavigateToConversation = { participantId, listingId ->
                    navController.navigate("conversation/$participantId/$listingId")
                },
                onNavigateToEditProfile = { navController.navigate("edit_profile") },

                //qr code testing remove later
                onNavigateToScanner = {
                    navController.navigate("scanner_route") // <-- navController lives HERE!
                },
                //---------------------------------------------------------------
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
                onEditListing = { id -> navController.navigate("edit_listing/$id") },
                onNavigateToScanner = { navController.navigate("scanner_route") }
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
                onNavigateBack = { navController.popBackStack() },
                onNavigateToConversation = { participantId, listingId ->
                    navController.navigate("conversation/$participantId/$listingId")
                },
                onNavigateToListing = { listingId ->
                    navController.navigate("listing/$listingId")
                }
            )
        }

        composable("settings") {
            SettingsScreen(
                viewModel = marketplaceViewModel,
                onNavigateBack = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("selected_tab", 3)
                    if (!navController.popBackStack()) {
                        navController.navigate("home")
                    }
                },
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
            val participantId = backStackEntry.arguments?.getString("participantId") ?: return@composable
            val listingId = backStackEntry.arguments?.getString("listingId") ?: return@composable
            val chatViewModel: ChatViewModel = viewModel()

            DisposableEffect(participantId, listingId) {
                marketplaceViewModel.setActiveConversation(participantId, listingId)
                onDispose {
                    marketplaceViewModel.clearActiveConversation()
                }
            }

            ConversationScreen(
                participantId = participantId,
                listingId = listingId,
                viewModel = marketplaceViewModel,
                chatViewModel = chatViewModel,
                currentUserId = authViewModel.getUserId(),
                onNavigateBack = { navController.popBackStack() },
                onNavigateToScanner = { navController.navigate("scanner_route") }
            )
        }

        composable("edit_profile") {
            EditProfileScreen(
                viewModel = marketplaceViewModel,
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("scanner_route") {
            val repository = androidx.compose.runtime.remember {
                com.example.secondshelf.data.TransactionRepository(com.example.secondshelf.supabase)
            }
            val transactionViewModel: com.example.secondshelf.ui.viewmodel.TransactionViewModel = viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        return com.example.secondshelf.ui.viewmodel.TransactionViewModel(repository) as T
                    }
                }
            )

            val scanState by transactionViewModel.scanState.collectAsState()
            val context = androidx.compose.ui.platform.LocalContext.current

            LaunchedEffect(Unit) {
                transactionViewModel.resetScanState()
            }

            LaunchedEffect(scanState) {
                when (val state = scanState) {
                    is com.example.secondshelf.ui.viewmodel.ScanState.Success -> {
                        marketplaceViewModel.refreshMarketplace()
                        navController.navigate("handoff_success_route") {
                            popUpTo("scanner_route") { inclusive = true }
                        }
                        transactionViewModel.resetScanState()
                    }
                    is com.example.secondshelf.ui.viewmodel.ScanState.Error -> {
                        android.widget.Toast.makeText(context, state.message, android.widget.Toast.LENGTH_SHORT).show()
                        transactionViewModel.resetScanState()
                    }
                    else -> {}
                }
            }

            QrScannerScreen(
                onNavigateBack = { navController.popBackStack() },
                onQrScanned = { scannedString ->
                    val currentUserId = authViewModel.getUserId()
                    transactionViewModel.verifyQrCode(scannedString, currentUserId)
                }
            )
        }

        composable("handoff_success_route") {
            SuccessScreen(
                onBackToHome = {
                    navController.navigate("home") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }

        // ── Global In-App Notification Banner Popup ──
        InAppNotificationBanner(
            notification = currentPopup,
            onDismiss = { marketplaceViewModel.dismissPopupNotification() },
            onClick = { participantId, listingId ->
                marketplaceViewModel.dismissPopupNotification()
                navController.navigate("conversation/$participantId/$listingId")
            },
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

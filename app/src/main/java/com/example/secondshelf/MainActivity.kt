package com.example.secondshelf

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.secondshelf.ui.navigation.AppNavigation
import com.example.secondshelf.ui.theme.SecondShelfTheme
import com.example.secondshelf.ui.viewmodel.MarketplaceViewModel

class MainActivity : ComponentActivity() {

    // Holds the OAuth callback URI so Compose can react to it
    private var authCallbackUri by mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            splashScreen.setOnExitAnimationListener { splashScreenView ->
                splashScreenView.remove()
            }
        }
        super.onCreate(savedInstanceState)
        // Handle deep-link on cold start (e.g. app was not in memory)
        handleDeepLinkIntent(intent)

        setContent {
            val marketplaceViewModel: MarketplaceViewModel = viewModel()
            SecondShelfTheme(darkTheme = marketplaceViewModel.isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        marketplaceViewModel = marketplaceViewModel,
                        authCallbackUri = authCallbackUri
                    )
                }
            }
        }
    }

    /** Called when the app is already running and a new Intent arrives (singleTop). */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLinkIntent(intent)
    }

    /**
     * Extracts the OAuth redirect URI (secondshelf://auth-callback?...) from the
     * incoming intent and stores it so AppNavigation can pass it to the ViewModel.
     */
    private fun handleDeepLinkIntent(intent: Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme == "secondshelf" && uri.host == "auth-callback") {
            authCallbackUri = uri
        }
    }
}

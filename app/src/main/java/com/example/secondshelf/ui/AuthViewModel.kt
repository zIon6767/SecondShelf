package com.example.secondshelf.ui

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.secondshelf.BuildConfig
import com.example.secondshelf.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.parseSessionFromUrl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

// ── UI State ──────────────────────────────────────────────────────────────────

sealed class AuthUiState {
    object Idle    : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
    data class EmailConfirmationRequired(val email: String) : AuthUiState()
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

class AuthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private fun setError(message: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Error(message)
            delay(5000) // Clear error after 5 seconds
            if (_uiState.value is AuthUiState.Error) {
                _uiState.value = AuthUiState.Idle
            }
        }
    }

    // ── Session check ─────────────────────────────────────────────────────────

    /** Returns true if a valid Supabase session already exists (persisted login). */
    fun isLoggedIn(): Boolean = supabase.auth.currentSessionOrNull() != null

    /** Returns true if the user profile has their custom username and campus set. */
    fun isProfileComplete(): Boolean {
        val user = supabase.auth.currentUserOrNull() ?: return false
        val fullName = user.userMetadata?.get("full_name")?.toString()?.trim('"')
        val campus = user.userMetadata?.get("campus")?.toString()?.trim('"')
        return !fullName.isNullOrBlank() && fullName != "null" && fullName != "User" && !campus.isNullOrBlank()
    }

    /** Returns the full name of the currently logged-in user from metadata or email. */
    fun getUserFullName(): String {
        val user = supabase.auth.currentUserOrNull() ?: return ""
        val fullName = user.userMetadata?.get("full_name")?.toString()?.trim('"')
            ?: user.userMetadata?.get("name")?.toString()?.trim('"')
            ?: user.userMetadata?.get("user_name")?.toString()?.trim('"')
            ?: user.userMetadata?.get("preferred_username")?.toString()?.trim('"')
        if (!fullName.isNullOrBlank() && fullName != "null" && fullName != "User" && fullName != "Student") {
            return fullName
        }
        val email = getUserEmail()
        if (email.isNotBlank()) {
            val prefix = email.substringBefore("@").replace(".", " ").replace("_", " ")
            return prefix.split(" ").filter { it.isNotBlank() }.joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
        }
        return ""
    }

    /** Returns the user ID of the currently logged-in user. */
    fun getUserId(): String {
        return supabase.auth.currentUserOrNull()?.id ?: "user-current"
    }

    /** Returns the campus of the currently logged-in user. */
    fun getUserCampus(): String {
        val user = supabase.auth.currentUserOrNull() ?: return "KL Main Campus"
        val campus = user.userMetadata?.get("campus")?.toString()?.trim('"')
        return if (campus.isNullOrBlank() || campus == "null") "KL Main Campus" else campus
    }

    /** Returns the university of the currently logged-in user. */
    fun getUserUniversity(): String {
        val user = supabase.auth.currentUserOrNull() ?: return "TAR UMT"
        val uni = user.userMetadata?.get("university")?.toString()?.trim('"')
        return if (uni.isNullOrBlank() || uni == "null") "TAR UMT" else uni
    }

    /** Returns the email of the currently logged-in user. */
    fun getUserEmail(): String {
        val user = supabase.auth.currentUserOrNull() ?: return ""
        val directEmail = user.email.orEmpty().trim().trim('"')
        if (directEmail.isNotBlank() && directEmail != "null") return directEmail
        val metaEmail = user.userMetadata?.get("email")?.toString()?.trim('"').orEmpty().trim()
        if (metaEmail.isNotBlank() && metaEmail != "null") return metaEmail
        return ""
    }

    // ── Email / Password Auth ─────────────────────────────────────────────────

    /**
     * Sign in with email + password.
     * Surfaces a clear "not registered" message when credentials are unrecognised.
     */
    fun signIn(email: String, password: String) {
        if (BuildConfig.SUPABASE_ANON_KEY.contains("PLACEHOLDER") || BuildConfig.SUPABASE_ANON_KEY.isBlank()) {
            setError("Configuration Error: Please update your SUPABASE_ANON_KEY in local.properties with your 'ey...' key")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            runCatching {
                supabase.auth.signInWith(Email) {
                    this.email    = email
                    this.password = password
                }
            }.onSuccess {
                _uiState.value = AuthUiState.Success
            }.onFailure { e ->
                setError(mapSignInError(e))
            }
        }
    }

    /**
     * Register a new account with email + password + profile metadata.
     * Called from SetPasswordScreen after the user completes the two-step register flow.
     */
    fun signUp(email: String, password: String, fullName: String, phone: String) {
        if (BuildConfig.SUPABASE_ANON_KEY.contains("PLACEHOLDER") || BuildConfig.SUPABASE_ANON_KEY.isBlank()) {
            setError("Configuration Error: Please update your SUPABASE_ANON_KEY in local.properties with your 'ey...' key")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            runCatching {
                supabase.auth.signUpWith(Email) {
                    this.email    = email
                    this.password = password
                    data = buildJsonObject {
                        put("full_name", fullName)
                        put("phone",     phone)
                    }
                }
            }.onSuccess {
                // If email confirmation is required, the session will be null
                if (supabase.auth.currentSessionOrNull() == null) {
                    _uiState.value = AuthUiState.EmailConfirmationRequired(email)
                } else {
                    _uiState.value = AuthUiState.Success
                }
            }.onFailure { e ->
                setError(mapSignUpError(e))
            }
        }
    }

    // ── Sign Out ──────────────────────────────────────────────────────────────

    /** Signs out the current user and resets state. */
    fun signOut() {
        viewModelScope.launch {
            runCatching { supabase.auth.signOut() }
            _uiState.value = AuthUiState.Idle
        }
    }

    /** Deletes the user account / terminates session and redirects. */
    fun deleteAccount(onComplete: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            runCatching {
                supabase.auth.signOut()
            }
            _uiState.value = AuthUiState.Idle
            onComplete()
        }
    }

    /** Updates custom user profile fields (username, campus) in Supabase user metadata. */
    fun updateUserProfile(fullName: String, campus: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            runCatching {
                supabase.auth.updateUser {
                    data = buildJsonObject {
                        put("full_name", fullName)
                        put("campus", campus)
                    }
                }
            }
            _uiState.value = AuthUiState.Idle
            onComplete()
        }
    }

    // ── Google OAuth (Custom Tabs) ────────────────────────────────────────────

    /**
     * Opens Supabase Google OAuth in a Chrome Custom Tab.
     * The OS redirects back to `secondshelf://auth-callback` when done,
     * which is handled in MainActivity → handleAuthCallback().
     *
     * Pre-requisite: Google provider must be enabled in the Supabase dashboard
     * (Authentication → Providers → Google) with a valid Client ID + Secret.
     */
    fun signInWithGoogle(context: Context) {
        val oauthUrl = "${BuildConfig.SUPABASE_URL}/auth/v1/authorize" +
            "?provider=google" +
            "&redirect_to=secondshelf%3A%2F%2Fauth-callback"
        CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
            .launchUrl(context, Uri.parse(oauthUrl))
    }

    /**
     * Called when the app receives the OAuth redirect deep-link
     * (secondshelf://auth-callback). Exchanges the tokens for a session.
     */
    fun handleAuthCallback(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            runCatching {
                val session = supabase.auth.parseSessionFromUrl(uri.toString())
                supabase.auth.importSession(session)
            }.onSuccess {
                _uiState.value = AuthUiState.Success
            }.onFailure { e ->
                setError("Google sign-in failed: ${e.message}")
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Resets state back to Idle (call after navigating away on success). */
    fun resetState() { _uiState.value = AuthUiState.Idle }

    private fun mapSignInError(e: Throwable): String {
        val msg = e.message.orEmpty().lowercase()
        return when {
            ("invalid_credentials" in msg) || ("invalid login credentials" in msg) ->
                "No account found with this email, or the password is incorrect.\n" +
                "Don't have an account yet? Please tap Sign Up below."
            "email not confirmed" in msg ->
                "Please verify your email address before logging in. Check your inbox."
            "too many requests" in msg ->
                "Too many login attempts. Please wait a moment and try again."
            "unable to resolve host" in msg || "timeout" in msg || "connect" in msg ->
                "Network Error: Unable to reach Supabase. Please Cold Boot your emulator or check your Wi-Fi."
            else ->
                "Login failed: ${e.localizedMessage ?: "Check connection"}. Please ensure your Supabase Key is correct."
        }
    }

    private fun mapSignUpError(e: Throwable): String {
        val msg = e.message.orEmpty().lowercase()
        return when {
            "already registered" in msg || "user already" in msg ->
                "An account with this email already exists. Please log in instead."
            "password should be" in msg ->
                "Password is too weak. Please use a stronger password."
            "invalid email" in msg || "unable to validate" in msg ->
                "Invalid email address. Please check and try again."
            else ->
                "Registration failed: ${e.localizedMessage ?: "Connection error"}. Please check your connection."
        }
    }
}

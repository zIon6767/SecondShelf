package com.example.secondshelf.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.secondshelf.BuildConfig
import com.example.secondshelf.data.SessionManager
import com.example.secondshelf.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.parseSessionFromUrl
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

// ─────────────────────────────────────────────────────────────────────────────
// AuthUiState — represents the result of each auth action
// ─────────────────────────────────────────────────────────────────────────────
sealed class AuthUiState {
    object Idle    : AuthUiState()    // no action happening
    object Loading : AuthUiState()    // waiting for Supabase response
    object Success : AuthUiState()    // login / register succeeded
    data class Error(val message: String) : AuthUiState()   // something went wrong
    data class EmailConfirmationRequired(val email: String) : AuthUiState() // needs email verify
}

// ─────────────────────────────────────────────────────────────────────────────
// AuthViewModel
// Handles all authentication logic: login, register, logout, profile updates.
// Uses Supabase as the backend — no dummy data is stored here.
// ─────────────────────────────────────────────────────────────────────────────
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    // ── Local session cache (SharedPreferences) ──────────────────────────────
    private val sessionManager = SessionManager(application.applicationContext)

    // ── UI state stream — screens observe this to react to auth events ────────
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // ── Cached user info (loaded from session on startup) ────────────────────
    var cachedFullName by mutableStateOf(""); private set
    var cachedCampus   by mutableStateOf(""); private set
    var cachedEmail    by mutableStateOf(""); private set
    var cachedAvatarUrl by mutableStateOf(""); private set

    // ── Avatar upload state ──────────────────────────────────────────────────
    var isUploadingAvatar by mutableStateOf(false); private set
    var avatarUploadError by mutableStateOf<String?>(null); private set

    // ── On startup: restore session from SharedPreferences ───────────────────
    init {
        if (sessionManager.isLoggedIn) {
            cachedFullName  = sessionManager.userName
            cachedEmail     = sessionManager.userEmail
            cachedCampus    = sessionManager.userCampus
            cachedAvatarUrl = sessionManager.avatarUrl
        }
    }

    // ── Helper: show an error message, then auto-clear it after 5 seconds ────
    private fun setError(message: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Error(message)
            delay(5000)
            if (_uiState.value is AuthUiState.Error) {
                _uiState.value = AuthUiState.Idle
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Session helpers — used by navigation to decide which screen to show
    // ─────────────────────────────────────────────────────────────────────────

    fun hasSeenOnboarding(): Boolean = sessionManager.hasSeenOnboarding
    fun setSeenOnboarding()          { sessionManager.hasSeenOnboarding = true }

    /** Returns true if a valid login session exists */
    fun isLoggedIn(): Boolean = sessionManager.isLoggedIn

    /** Returns true if the user has completed their profile (name + campus set) */
    fun isProfileComplete(): Boolean {
        // Check cached values first (fastest)
        if (cachedFullName.isNotBlank() && cachedFullName !in listOf("Student", "User")
            && cachedCampus.isNotBlank()) return true

        // Then check SharedPreferences
        if (sessionManager.userName.isNotBlank() && sessionManager.userName !in listOf("Student", "User")
            && sessionManager.userCampus.isNotBlank()) {
            cachedFullName = sessionManager.userName
            cachedCampus   = sessionManager.userCampus
            return true
        }

        // Finally check Supabase auth metadata
        val user      = supabase.auth.currentUserOrNull() ?: return false
        val fullName  = user.userMetadata?.get("full_name")?.toString()?.trim('"')
        val campus    = user.userMetadata?.get("campus")?.toString()?.trim('"')
        return !fullName.isNullOrBlank() && fullName !in listOf("null", "User", "Student")
               && !campus.isNullOrBlank()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Getters — pull user info from cache, SharedPreferences, or Supabase
    // ─────────────────────────────────────────────────────────────────────────

    /** Get the user's full name */
    fun getUserFullName(): String {
        // 1. Check cache
        if (cachedFullName.isNotBlank() && cachedFullName !in listOf("Student", "User"))
            return cachedFullName

        // 2. Check SharedPreferences
        if (sessionManager.userName.isNotBlank() && sessionManager.userName !in listOf("Student", "User")) {
            cachedFullName = sessionManager.userName
            return cachedFullName
        }

        // 3. Check Supabase metadata (multiple possible field names)
        val user = supabase.auth.currentUserOrNull()
        val fullName = user?.userMetadata?.get("full_name")?.toString()?.trim('"')
            ?: user?.userMetadata?.get("name")?.toString()?.trim('"')
            ?: user?.userMetadata?.get("user_name")?.toString()?.trim('"')
            ?: user?.userMetadata?.get("preferred_username")?.toString()?.trim('"')

        if (!fullName.isNullOrBlank() && fullName !in listOf("null", "User", "Student")) {
            cachedFullName = fullName
            sessionManager.userName = fullName
            return fullName
        }

        // 4. Fallback: derive a readable name from email prefix
        val email = getUserEmail()
        if (email.isNotBlank()) {
            val formatted = email.substringBefore("@")
                .replace(".", " ").replace("_", " ")
                .split(" ").filter { it.isNotBlank() }
                .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
            if (formatted.isNotBlank()) {
                cachedFullName = formatted
                sessionManager.userName = formatted
                return formatted
            }
        }

        return ""
    }

    /** Get the user's Supabase UID */
    fun getUserId(): String {
        val uid = supabase.auth.currentUserOrNull()?.id
        if (!uid.isNullOrBlank()) {
            sessionManager.userId = uid
            return uid
        }
        if (sessionManager.userId.isNotBlank()) return sessionManager.userId
        return "user-current"
    }

    /** Get the user's selected campus */
    fun getUserCampus(): String {
        if (cachedCampus.isNotBlank()) return cachedCampus
        if (sessionManager.userCampus.isNotBlank()) {
            cachedCampus = sessionManager.userCampus
            return cachedCampus
        }
        val user   = supabase.auth.currentUserOrNull() ?: return "KL Main Campus"
        val campus = user.userMetadata?.get("campus")?.toString()?.trim('"')
        if (!campus.isNullOrBlank() && campus != "null") {
            cachedCampus = campus
            sessionManager.userCampus = campus
            return campus
        }
        return "KL Main Campus"
    }

    /** Get the user's university (default: TAR UMT) */
    fun getUserUniversity(): String {
        if (sessionManager.userUniversity.isNotBlank()) return sessionManager.userUniversity
        val user = supabase.auth.currentUserOrNull() ?: return "TAR UMT"
        val uni  = user.userMetadata?.get("university")?.toString()?.trim('"')
        return if (uni.isNullOrBlank() || uni == "null") "TAR UMT" else uni
    }

    /** Get the user's email address */
    fun getUserEmail(): String {
        if (cachedEmail.isNotBlank()) return cachedEmail
        if (sessionManager.userEmail.isNotBlank()) {
            cachedEmail = sessionManager.userEmail
            return cachedEmail
        }
        val user        = supabase.auth.currentUserOrNull() ?: return ""
        val directEmail = user.email.orEmpty().trim().trim('"')
        if (directEmail.isNotBlank() && directEmail != "null") {
            cachedEmail = directEmail
            sessionManager.userEmail = directEmail
            return directEmail
        }
        val metaEmail = user.userMetadata?.get("email")?.toString()?.trim('"').orEmpty().trim()
        if (metaEmail.isNotBlank() && metaEmail != "null") {
            cachedEmail = metaEmail
            sessionManager.userEmail = metaEmail
            return metaEmail
        }
        return ""
    }

    /** Update cached profile info without calling Supabase (used internally) */
    fun setCachedUserProfile(name: String, email: String, campus: String) {
        if (name.isNotBlank())   { cachedFullName = name;   sessionManager.userName  = name  }
        if (email.isNotBlank())  { cachedEmail    = email;  sessionManager.userEmail = email }
        if (campus.isNotBlank()) { cachedCampus   = campus; sessionManager.userCampus = campus }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Email / Password Authentication
    // ─────────────────────────────────────────────────────────────────────────

    /** Sign in with email and password via Supabase */
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            runCatching {
                supabase.auth.signInWith(Email) {
                    this.email    = email
                    this.password = password
                }
            }.onSuccess {
                cachedEmail = email
                val fullName = getUserFullName().ifBlank { email.substringBefore("@").replace(".", " ") }
                val campus   = getUserCampus()
                sessionManager.saveSession(getUserId(), email, fullName, campus)
                _uiState.value = AuthUiState.Success
            }.onFailure { e ->
                setError(mapSignInError(e))
            }
        }
    }

    /** Register a new account with email and password via Supabase */
    fun signUp(email: String, password: String, fullName: String, phone: String) {
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
                cachedEmail    = email
                cachedFullName = fullName
                sessionManager.hasSeenOnboarding = true
                // If email confirmation is required, Supabase returns no session
                if (supabase.auth.currentSessionOrNull() == null) {
                    _uiState.value = AuthUiState.EmailConfirmationRequired(email)
                } else {
                    sessionManager.saveSession(getUserId(), email, fullName, getUserCampus())
                    _uiState.value = AuthUiState.Success
                }
            }.onFailure { e ->
                setError(mapSignUpError(e))
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Sign Out & Delete Account
    // ─────────────────────────────────────────────────────────────────────────

    /** Log out the user and clear all local session data */
    fun signOut() {
        cachedFullName = ""
        cachedEmail    = ""
        cachedCampus   = ""
        sessionManager.clearSession()
        viewModelScope.launch {
            runCatching { supabase.auth.signOut() }
            _uiState.value = AuthUiState.Idle
        }
    }

    /** Delete the user's account and clear session */
    fun deleteAccount(onComplete: () -> Unit) {
        cachedFullName = ""
        cachedEmail    = ""
        cachedCampus   = ""
        sessionManager.clearSession()
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            runCatching { supabase.auth.signOut() }
            _uiState.value = AuthUiState.Idle
            onComplete()
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Profile Update — saves name and campus to both Supabase and local cache
    // ─────────────────────────────────────────────────────────────────────────

    fun updateUserProfile(fullName: String, campus: String, onComplete: () -> Unit) {
        // Update local cache immediately so UI responds fast
        if (fullName.isNotBlank()) { cachedFullName = fullName; sessionManager.userName  = fullName }
        if (campus.isNotBlank())   { cachedCampus   = campus;   sessionManager.userCampus = campus  }
        sessionManager.saveSession(getUserId(), getUserEmail(), fullName, campus)

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val uid   = getUserId()
            val email = getUserEmail()
            runCatching {
                // 1. Update Supabase Auth user metadata
                supabase.auth.updateUser {
                    data = buildJsonObject {
                        put("full_name", fullName)
                        put("campus",    campus)
                    }
                }
                // 2. Sync the profiles table row in Supabase
                supabase.postgrest.from("profiles").upsert(
                    listOf(
                        buildJsonObject {
                            put("id",         uid)
                            put("email",      email)
                            put("name",       fullName)
                            put("campus",     campus)
                            put("university", "TAR UMT")
                        }
                    )
                )
            }
            _uiState.value = AuthUiState.Idle
            onComplete()
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Google OAuth — opens Supabase Google sign-in in a Chrome Custom Tab
    // ─────────────────────────────────────────────────────────────────────────

    fun signInWithGoogle(context: Context) {
        val oauthUrl = "${BuildConfig.SUPABASE_URL}/auth/v1/authorize" +
            "?provider=google" +
            "&redirect_to=secondshelf%3A%2F%2Fauth-callback"
        CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
            .launchUrl(context, Uri.parse(oauthUrl))
    }

    /** Called when the app receives the Google sign-in callback deep link */
    fun handleAuthCallback(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            runCatching {
                supabase.auth.parseSessionFromUrl(uri.toString())
            }.onSuccess {
                val user = supabase.auth.currentUserOrNull()

                // Get email from Supabase user object
                val email = user?.email?.trim()?.lowercase()
                    ?: user?.userMetadata?.get("email")?.toString()?.trim('"')?.lowercase()
                    ?: getUserEmail().lowercase()

                // Get display name from Supabase metadata, or derive from email
                val metaName = user?.userMetadata?.get("full_name")?.toString()?.trim('"')
                    ?: user?.userMetadata?.get("name")?.toString()?.trim('"')

                val name = if (!metaName.isNullOrBlank() && metaName != "null") {
                    metaName
                } else if (email.isNotBlank()) {
                    email.substringBefore("@").replace(".", " ").replace("_", " ")
                        .split(" ").filter { it.isNotBlank() }
                        .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                } else {
                    "Student"
                }

                val uid    = user?.id ?: getUserId()
                val campus = getUserCampus()

                cachedEmail    = email
                cachedFullName = name
                sessionManager.saveSession(uid, email, name, campus)
                _uiState.value = AuthUiState.Success
            }.onFailure { e ->
                setError("Google sign-in failed: ${e.message}")
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Avatar Upload — uploads the selected image to Supabase Storage
    // ─────────────────────────────────────────────────────────────────────────

    fun uploadAvatar(uri: Uri) {
        viewModelScope.launch {
            isUploadingAvatar = true
            avatarUploadError = null
            runCatching {
                val context = getApplication<Application>().applicationContext

                // Read the image bytes from the URI
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: error("Could not read image file")

                val userId = sessionManager.userId.ifBlank {
                    supabase.auth.currentUserOrNull()?.id ?: "unknown"
                }
                val ext  = context.contentResolver.getType(uri)?.substringAfter("/")?.lowercase() ?: "jpg"
                val path = "public/$userId/avatar.$ext"

                // Upload to Supabase Storage "avatars" bucket
                supabase.storage.from("avatars").upload(path, bytes) {
                    upsert = true
                }

                // Get the public URL and add a cache-buster so the image reloads
                val publicUrl   = supabase.storage.from("avatars").publicUrl(path)
                val urlWithBust = "$publicUrl?t=${System.currentTimeMillis()}"

                cachedAvatarUrl          = urlWithBust
                sessionManager.avatarUrl = urlWithBust

                // Sync avatar_url to the profiles table in Supabase
                supabase.postgrest.from("profiles").upsert(
                    listOf(
                        buildJsonObject {
                            put("id",         userId)
                            put("email",      sessionManager.userEmail)
                            put("name",       sessionManager.userName)
                            put("avatar_url", urlWithBust)
                        }
                    )
                )
            }.onFailure { e ->
                avatarUploadError = "Upload failed: ${e.message}"
            }
            isUploadingAvatar = false
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Utility
    // ─────────────────────────────────────────────────────────────────────────

    /** Reset the UI state back to Idle (clears any error or success state) */
    fun resetState() { _uiState.value = AuthUiState.Idle }

    /** Convert Supabase sign-in errors to user-friendly messages */
    private fun mapSignInError(e: Throwable): String {
        val msg = e.message.orEmpty().lowercase()
        return when {
            "provider" in msg && ("email" in msg || "not found" in msg) ->
                "This email is linked to a Google account. Please tap \"Continue with Google\" to sign in."
            "should login with" in msg || "oauth" in msg ->
                "This email is registered via Google. Please tap \"Continue with Google\" to sign in."
            "invalid_credentials" in msg || "invalid login credentials" in msg ->
                "Email or password is incorrect.\nIf you signed up with Google, tap \"Continue with Google\" instead."
            "email not confirmed" in msg ->
                "Please verify your email address before logging in. Check your inbox."
            "too many requests" in msg ->
                "Too many login attempts. Please wait a moment and try again."
            "unable to resolve host" in msg || "timeout" in msg || "connect" in msg ->
                "Network Error: Unable to reach the server. Please check your Wi-Fi connection."
            else ->
                "Login failed: ${e.localizedMessage ?: "Check connection"}."
        }
    }

    /** Convert Supabase sign-up errors to user-friendly messages */
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

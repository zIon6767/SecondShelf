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
import kotlinx.serialization.Serializable
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

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application.applicationContext)

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    var cachedFullName by mutableStateOf("")
        private set
    var cachedCampus by mutableStateOf("")
        private set
    var cachedEmail by mutableStateOf("")
        private set
    var cachedAvatarUrl by mutableStateOf("")
        private set

    // Upload state: null = idle, true = uploading, false = done/error
    var isUploadingAvatar by mutableStateOf(false)
        private set
    var avatarUploadError by mutableStateOf<String?>(null)
        private set

    init {
        if (sessionManager.isLoggedIn) {
            cachedFullName = sessionManager.userName
            cachedEmail = sessionManager.userEmail
            cachedCampus = sessionManager.userCampus
            cachedAvatarUrl = sessionManager.avatarUrl
        }
    }

    private fun setError(message: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Error(message)
            delay(5000) // Clear error after 5 seconds
            if (_uiState.value is AuthUiState.Error) {
                _uiState.value = AuthUiState.Idle
            }
        }
    }

    fun setCachedUserProfile(name: String, email: String, campus: String) {
        if (name.isNotBlank()) {
            cachedFullName = name
            sessionManager.userName = name
        }
        if (email.isNotBlank()) {
            cachedEmail = email
            sessionManager.userEmail = email
        }
        if (campus.isNotBlank()) {
            cachedCampus = campus
            sessionManager.userCampus = campus
        }
    }

    // ── Session check ─────────────────────────────────────────────────────────

    fun hasSeenOnboarding(): Boolean = sessionManager.hasSeenOnboarding
    fun setSeenOnboarding() { sessionManager.hasSeenOnboarding = true }

    /** Returns true if a valid session exists (persisted via SharedPreferences). */
    fun isLoggedIn(): Boolean = sessionManager.isLoggedIn

    /** Returns true if the user profile has their custom username and campus set. */
    fun isProfileComplete(): Boolean {
        if (cachedFullName.isNotBlank() && cachedFullName != "Student" && cachedFullName != "User" && cachedCampus.isNotBlank()) {
            return true
        }
        if (sessionManager.userName.isNotBlank() && sessionManager.userName != "Student" && sessionManager.userName != "User" && sessionManager.userCampus.isNotBlank()) {
            cachedFullName = sessionManager.userName
            cachedCampus = sessionManager.userCampus
            return true
        }
        val user = supabase.auth.currentUserOrNull() ?: return false
        val fullName = user.userMetadata?.get("full_name")?.toString()?.trim('"')
        val campus = user.userMetadata?.get("campus")?.toString()?.trim('"')
        return !fullName.isNullOrBlank() && fullName != "null" && fullName != "User" && fullName != "Student" && !campus.isNullOrBlank()
    }

    /** Returns the full name of the currently logged-in user from metadata, cache, or preferences. */
    fun getUserFullName(): String {
        if (cachedFullName.isNotBlank() && cachedFullName != "Student" && cachedFullName != "User") {
            return cachedFullName
        }
        if (sessionManager.userName.isNotBlank() && sessionManager.userName != "Student" && sessionManager.userName != "User") {
            cachedFullName = sessionManager.userName
            return cachedFullName
        }
        val user = supabase.auth.currentUserOrNull()
        val fullName = user?.userMetadata?.get("full_name")?.toString()?.trim('"')
            ?: user?.userMetadata?.get("name")?.toString()?.trim('"')
            ?: user?.userMetadata?.get("user_name")?.toString()?.trim('"')
            ?: user?.userMetadata?.get("preferred_username")?.toString()?.trim('"')
        if (!fullName.isNullOrBlank() && fullName != "null" && fullName != "User" && fullName != "Student") {
            cachedFullName = fullName
            sessionManager.userName = fullName
            return fullName
        }
        val email = getUserEmail()
        if (email.isNotBlank()) {
            val prefix = email.substringBefore("@").replace(".", " ").replace("_", " ")
            val formatted = prefix.split(" ").filter { it.isNotBlank() }.joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
            if (formatted.isNotBlank()) {
                cachedFullName = formatted
                sessionManager.userName = formatted
                return formatted
            }
        }
        return ""
    }

    /** Returns the user ID of the currently logged-in user. */
    fun getUserId(): String {
        val uid = supabase.auth.currentUserOrNull()?.id
        if (!uid.isNullOrBlank()) {
            sessionManager.userId = uid
            return uid
        }
        if (sessionManager.userId.isNotBlank()) return sessionManager.userId
        return "user-current"
    }

    /** Returns the campus of the currently logged-in user. */
    fun getUserCampus(): String {
        if (cachedCampus.isNotBlank()) return cachedCampus
        if (sessionManager.userCampus.isNotBlank()) {
            cachedCampus = sessionManager.userCampus
            return cachedCampus
        }
        val user = supabase.auth.currentUserOrNull() ?: return "KL Main Campus"
        val campus = user.userMetadata?.get("campus")?.toString()?.trim('"')
        if (!campus.isNullOrBlank() && campus != "null") {
            cachedCampus = campus
            sessionManager.userCampus = campus
            return campus
        }
        return "KL Main Campus"
    }

    /** Returns the university of the currently logged-in user. */
    fun getUserUniversity(): String {
        if (sessionManager.userUniversity.isNotBlank()) return sessionManager.userUniversity
        val user = supabase.auth.currentUserOrNull() ?: return "TAR UMT"
        val uni = user.userMetadata?.get("university")?.toString()?.trim('"')
        return if (uni.isNullOrBlank() || uni == "null") "TAR UMT" else uni
    }

    /** Returns the email of the currently logged-in user. */
    fun getUserEmail(): String {
        if (cachedEmail.isNotBlank()) return cachedEmail
        if (sessionManager.userEmail.isNotBlank()) {
            cachedEmail = sessionManager.userEmail
            return cachedEmail
        }
        val user = supabase.auth.currentUserOrNull() ?: return ""
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

    // ── Email / Password Auth ─────────────────────────────────────────────────

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
                cachedEmail = email
                val fullName = getUserFullName().ifBlank { email.substringBefore("@").replace(".", " ") }
                val campus = getUserCampus()
                sessionManager.saveSession(getUserId(), email, fullName, campus)
                _uiState.value = AuthUiState.Success
            }.onFailure { e ->
                setError(mapSignInError(e))
            }
        }
    }

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
                cachedEmail = email
                cachedFullName = fullName
                sessionManager.hasSeenOnboarding = true
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

    // ── Sign Out ──────────────────────────────────────────────────────────────

    fun signOut() {
        cachedFullName = ""
        cachedEmail = ""
        cachedCampus = ""
        sessionManager.clearSession()
        viewModelScope.launch {
            runCatching { supabase.auth.signOut() }
            _uiState.value = AuthUiState.Idle
        }
    }

    fun deleteAccount(onComplete: () -> Unit) {
        cachedFullName = ""
        cachedEmail = ""
        cachedCampus = ""
        sessionManager.clearSession()
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            runCatching {
                supabase.auth.signOut()
            }
            _uiState.value = AuthUiState.Idle
            onComplete()
        }
    }

    fun updateUserProfile(fullName: String, campus: String, onComplete: () -> Unit) {
        if (fullName.isNotBlank()) {
            cachedFullName = fullName
            sessionManager.userName = fullName
        }
        if (campus.isNotBlank()) {
            cachedCampus = campus
            sessionManager.userCampus = campus
        }
        sessionManager.saveSession(getUserId(), getUserEmail(), fullName, campus)

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val uid = getUserId()
            val email = getUserEmail()
            runCatching {
                // 1. Update Supabase Auth user metadata
                supabase.auth.updateUser {
                    data = buildJsonObject {
                        put("full_name", fullName)
                        put("campus", campus)
                    }
                }
                // 2. Sync the profiles table row
                supabase.postgrest.from("profiles").upsert(
                    listOf(
                        buildJsonObject {
                            put("id", uid)
                            put("email", email)
                            put("name", fullName)
                            put("campus", campus)
                            put("university", "TAR UMT")
                        }
                    )
                )
            }
            _uiState.value = AuthUiState.Idle
            onComplete()
        }
    }

    // ── Google OAuth (Custom Tabs) ────────────────────────────────────────────

    fun signInWithGoogle(context: Context) {
        val oauthUrl = "${BuildConfig.SUPABASE_URL}/auth/v1/authorize" +
            "?provider=google" +
            "&redirect_to=secondshelf%3A%2F%2Fauth-callback"
        CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
            .launchUrl(context, Uri.parse(oauthUrl))
    }

    fun handleAuthCallback(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            runCatching {
                val session = supabase.auth.parseSessionFromUrl(uri.toString())
                supabase.auth.importSession(session)
            }.onSuccess {
                // After parseSessionFromUrl, the session is imported into the Supabase client.
                // Read from currentUserOrNull() which is now reliably populated.
                val user = supabase.auth.currentUserOrNull()
                val email = user?.email?.trim()?.lowercase()
                    ?: user?.userMetadata?.get("email")?.toString()?.trim('"')?.trim()?.lowercase()
                    ?: getUserEmail().trim().lowercase()

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

                val uid = user?.id ?: getUserId()
                val campus = getUserCampus()

                cachedEmail = email
                cachedFullName = name
                sessionManager.saveSession(uid, email, name, campus)
                _uiState.value = AuthUiState.Success
            }.onFailure { e ->
                setError("Google sign-in failed: ${e.message}")
            }
        }
    }

    // ── Avatar Upload ─────────────────────────────────────────────────────────

    fun uploadAvatar(uri: Uri) {
        viewModelScope.launch {
            isUploadingAvatar = true
            avatarUploadError = null
            runCatching {
                val context = getApplication<Application>().applicationContext
                // Read bytes from the picked image URI
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: error("Could not read image file")

                val userId = sessionManager.userId.ifBlank { supabase.auth.currentUserOrNull()?.id ?: "unknown" }
                val ext = context.contentResolver.getType(uri)
                    ?.substringAfter("/")?.lowercase() ?: "jpg"
                val path = "public/$userId/avatar.$ext"

                // Upload to the "avatars" bucket (create this in Supabase Storage dashboard)
                supabase.storage.from("avatars").upload(path, bytes) {
                    upsert = true
                }

                // Get the public URL
                val publicUrl = supabase.storage.from("avatars").publicUrl(path)
                // Add cache-busting so Coil reloads the new image
                val urlWithBust = "$publicUrl?t=${System.currentTimeMillis()}"

                cachedAvatarUrl = urlWithBust
                sessionManager.avatarUrl = urlWithBust

                // Sync avatar_url to the profiles table
                supabase.postgrest.from("profiles").upsert(
                    listOf(
                        buildJsonObject {
                            put("id", userId)
                            put("email", sessionManager.userEmail)
                            put("name", sessionManager.userName)
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

    // ── Helpers ───────────────────────────────────────────────────────────────

    fun resetState() { _uiState.value = AuthUiState.Idle }

    private fun mapSignInError(e: Throwable): String {
        val msg = e.message.orEmpty().lowercase()
        return when {
            // Account exists but was created via Google OAuth (no password set)
            "provider" in msg && ("email" in msg || "not found" in msg) ->
                "This email is linked to a Google account. Please tap \"Continue with Google\" to sign in."
            "should login with" in msg || "oauth" in msg ->
                "This email is registered via Google. Please tap \"Continue with Google\" to sign in."
            (("invalid_credentials" in msg) || ("invalid login credentials" in msg)) ->
                "Email or password is incorrect.\n" +
                "If you signed up with Google, tap \"Continue with Google\" instead."
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

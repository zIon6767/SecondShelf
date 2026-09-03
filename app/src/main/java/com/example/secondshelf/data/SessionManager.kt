package com.example.secondshelf.data

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("second_shelf_auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_CAMPUS = "user_campus"
        private const val KEY_USER_UNIVERSITY = "user_university"
        private const val KEY_HAS_SEEN_ONBOARDING = "has_seen_onboarding"
    }

    var isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply()

    var hasSeenOnboarding: Boolean
        get() = prefs.getBoolean(KEY_HAS_SEEN_ONBOARDING, false)
        set(value) = prefs.edit().putBoolean(KEY_HAS_SEEN_ONBOARDING, value).apply()

    var userId: String
        get() = prefs.getString(KEY_USER_ID, "") ?: ""
        set(value) = prefs.edit().putString(KEY_USER_ID, value).apply()

    var userEmail: String
        get() = prefs.getString(KEY_USER_EMAIL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_USER_EMAIL, value).apply()

    var userName: String
        get() = prefs.getString(KEY_USER_NAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

    var userCampus: String
        get() = prefs.getString(KEY_USER_CAMPUS, "KL Main Campus") ?: "KL Main Campus"
        set(value) = prefs.edit().putString(KEY_USER_CAMPUS, value).apply()

    var userUniversity: String
        get() = prefs.getString(KEY_USER_UNIVERSITY, "TAR UMT") ?: "TAR UMT"
        set(value) = prefs.edit().putString(KEY_USER_UNIVERSITY, value).apply()

    fun saveSession(id: String, email: String, name: String, campus: String, university: String = "TAR UMT") {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putBoolean(KEY_HAS_SEEN_ONBOARDING, true)
            .putString(KEY_USER_ID, id)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_NAME, name)
            .putString(KEY_USER_CAMPUS, campus)
            .putString(KEY_USER_UNIVERSITY, university)
            .apply()
    }

    fun clearSession() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_USER_NAME)
            .remove(KEY_USER_CAMPUS)
            .remove(KEY_USER_UNIVERSITY)
            .apply()
    }
}

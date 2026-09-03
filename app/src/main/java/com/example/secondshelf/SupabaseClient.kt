package com.example.secondshelf

import android.content.Context
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.SupabaseClient as SbClient

/**
 * Singleton Supabase client shared across the app.
 *
 * Must be initialised once via [initSupabase] (called from [SecondShelfApplication.onCreate])
 * before accessing [supabase]. Passing an Android [Context] allows the Auth plugin to use
 * Android-platform storage (DataStore) so that the JWT session token is persisted across
 * app restarts and debug relaunch cycles.
 */
private var _supabase: SbClient? = null

val supabase: SbClient
    get() = checkNotNull(_supabase) {
        "Supabase client not initialised. Call initSupabase(context) first."
    }

fun initSupabase(context: Context) {
    if (_supabase != null) return          // already initialised
    _supabase = createSupabaseClient(
        supabaseUrl  = BuildConfig.SUPABASE_URL,
        supabaseKey  = BuildConfig.SUPABASE_ANON_KEY
    ) {
        install(Auth) {
            scheme = "secondshelf"
            host   = "auth-callback"
            // autoLoadFromStorage = true (default) — loads saved JWT on every cold start
            // alwaysAutoRefresh  = true (default) — silently refreshes expiring tokens
        }
        install(Postgrest)
    }
}
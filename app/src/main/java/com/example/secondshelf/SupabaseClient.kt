package com.example.secondshelf

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

/**
 * Singleton Supabase client shared across the app.
 * URL and key are injected from local.properties via BuildConfig.
 */
val supabase = createSupabaseClient(
    supabaseUrl = BuildConfig.SUPABASE_URL,
    supabaseKey = BuildConfig.SUPABASE_ANON_KEY
) {
    install(Auth) {
        scheme = "secondshelf"
        host = "auth-callback"
    }
    install(Postgrest)
}
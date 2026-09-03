package com.example.secondshelf

import android.app.Application

class SecondShelfApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize the Supabase client with Android Context so it can
        // persist the auth session (JWT token) to DataStore across restarts.
        initSupabase(this)
    }
}

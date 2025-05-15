package com.example.refac_userbus

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val prefs = getSharedPreferences("MyApp", MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode", false)

        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
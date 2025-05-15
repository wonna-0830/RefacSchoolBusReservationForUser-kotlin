package com.example.refac_userbus

import android.app.AlarmManager
import android.content.Context
import android.os.Bundle
import android.content.Intent;
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("MyApp", MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode", false)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //자동 로그인을 위한 로직
        val isAutoLogin = prefs.getBoolean("autoLogin", false)

        if (isAutoLogin) {
            val email = prefs.getString("userEmail", null)
            val password = prefs.getString("userPassword", null)

            if (email != null && password != null) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        startActivity(Intent(this, RouteChoose::class.java))
                        finish()
                    }
                    .addOnFailureListener {
                        // 실패했을 때는 그냥 Login으로 넘어가야 함!
                        moveToLogin()
                    }
            } else {
                moveToLogin()
            }
        } else {
            moveToLogin()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        }




    }
    // 로그인으로 넘어가는 함수
    private fun moveToLogin() {
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, Login::class.java))
            finish()
        }, 5000)
    }

}
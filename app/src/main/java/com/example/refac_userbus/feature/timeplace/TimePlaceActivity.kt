package com.example.refac_userbus.feature.timeplace

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.refac_userbus.R
import com.example.refac_userbus.feature.login.LoginActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.refac_userbus.feature.alarm.AlarmReceive
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import com.example.refac_userbus.feature.selectbuslist.SelectBusListActivity

class TimePlaceActivity : AppCompatActivity() {

    private val viewModel: TimePlaceViewModel by viewModels()
    private var doubleBackToExitPressedOnce = false

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timeplace)

        val prefs = getSharedPreferences("MyApp", MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode", false)
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_NO else AppCompatDelegate.MODE_NIGHT_YES
        )

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener { showPopupMenu(it) }

        val routeName = intent.getStringExtra("EXTRA_ROUTE_NAME") ?: return
        val textRouteName = findViewById<TextView>(R.id.displayRoute)
        val imageView = findViewById<ImageView>(R.id.mapImage)
        val spinnerTime = findViewById<Spinner>(R.id.time_spinner)
        val spinnerPlace = findViewById<Spinner>(R.id.station_spinner)
        val btnReserve = findViewById<Button>(R.id.reservation)
        val backBtn = findViewById<Button>(R.id.btn_back)

        textRouteName.text = routeName

        // 노선 데이터 로딩
        viewModel.loadRouteInfo(routeName, this, imageView, spinnerTime, spinnerPlace)

        btnReserve.setOnClickListener {
            viewModel.makeReservation(
                context = this,
                routeName = routeName,
                selectedTime = spinnerTime.selectedItem?.toString() ?: "",
                selectedStation = spinnerPlace.selectedItem?.toString() ?: "",
                onSuccess = {
                    val timeMillis = viewModel.getReservationTimeInMillis(it)
                    setAlarm(timeMillis)
                    startActivity(Intent(this, SelectBusListActivity::class.java))
                },
                onFailure = { message ->
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
            )
        }

        backBtn.setOnClickListener { finish() }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (doubleBackToExitPressedOnce) {
                    finishAffinity()
                    return
                }
                doubleBackToExitPressedOnce = true
                Toast.makeText(this@TimePlaceActivity, "한 번 더 누르면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show()
                Handler(Looper.getMainLooper()).postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
            }
        })
    }

    private fun showPopupMenu(v: View) {
        val popupMenu = PopupMenu(this, v)
        popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)

        val prefs = getSharedPreferences("MyApp", MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode", false)
        val darkModeItem = popupMenu.menu.findItem(R.id.menu_item_4)
        darkModeItem.title = if (isDarkMode) "다크모드로 변경" else "라이트모드로 변경"

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_item_1 -> {
                    val url = "https://www.cu.ac.kr/life/welfare/schoolbus"
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    true
                }
                R.id.menu_item_2 -> {
                    startActivity(Intent(this, SelectBusListActivity::class.java))
                    finish()
                    true
                }
                R.id.menu_item_3 -> {
                    prefs.edit().clear().apply()
                    viewModel.logout()
                    startActivity(Intent(this, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    finish()
                    true
                }
                R.id.menu_item_4 -> {
                    prefs.edit().putBoolean("dark_mode", !isDarkMode).apply()
                    AppCompatDelegate.setDefaultNightMode(
                        if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                    )
                    recreate()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun setAlarm(reservationTimeInMillis: Long) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceive::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val triggerTime = reservationTimeInMillis - (10 * 60 * 1000)

        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else {
            Toast.makeText(this, "정확한 알람 권한이 필요합니다. 설정에서 허용해주세요.", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }
    }
}

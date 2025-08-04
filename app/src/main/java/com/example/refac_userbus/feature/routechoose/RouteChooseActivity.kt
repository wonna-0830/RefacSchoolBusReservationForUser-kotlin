package com.example.refac_userbus.feature.routechoose

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.refac_userbus.R
import com.example.refac_userbus.feature.selectbuslist.SelectBusListActivity
import com.example.refac_userbus.feature.timeplace.TimePlaceActivity
import com.example.refac_userbus.feature.login.LoginActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class RouteChooseActivity : AppCompatActivity() {
    private val viewModel: RouteChooseViewModel by viewModels()
    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("MyApp", MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode", false)
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_NO
            else AppCompatDelegate.MODE_NIGHT_YES
        )

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_routechoose)

        val nameTextView = findViewById<TextView>(R.id.userName)
        val routeContainer = findViewById<LinearLayout>(R.id.routeContainer)
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        val logoutBtn = findViewById<Button>(R.id.btn_logout)

        // 사용자 이름
        viewModel.userName.observe(this) { name ->
            nameTextView.text = name
        }

        // 노선 목록 버튼으로 동적 생성
        viewModel.routes.observe(this) { routes ->
            routeContainer.removeAllViews()
            for (route in routes) {
                val button = Button(this).apply {
                    text = route.name
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
                    ).apply { setMargins(0, 8, 0, 8) }

                    setOnClickListener {
                        val intent = Intent(this@RouteChooseActivity, TimePlaceActivity::class.java).apply {
                            putExtra("EXTRA_ROUTE_NAME", route.name)
                            putExtra("EXTRA_IMAGE_NAME", route.image)
                            putStringArrayListExtra("EXTRA_STATIONS", ArrayList(route.stops))
                            putStringArrayListExtra("EXTRA_TIMES", ArrayList(route.times))
                        }
                        startActivity(intent)
                    }
                }
                routeContainer.addView(button)
            }
        }

        // 로딩 시작
        viewModel.loadUserName()
        viewModel.loadPinnedRoutes()


        // 팝업 메뉴
        fab.setOnClickListener { showPopupMenu(it) }

        // 로그아웃
        logoutBtn.setOnClickListener {
            prefs.edit().clear().apply()
            viewModel.logout()
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }

        // 뒤로 두 번 누르면 종료
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (doubleBackToExitPressedOnce) {
                    finishAffinity()
                } else {
                    doubleBackToExitPressedOnce = true
                    Toast.makeText(this@RouteChooseActivity, "한 번 더 누르면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show()
                    Handler(Looper.getMainLooper()).postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
                }
            }
        })
    }

    private fun showPopupMenu(v: View) {
        val popupMenu = PopupMenu(this, v)
        popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)

        popupMenu.menu.findItem(R.id.menu_item_3).isVisible = false // 로그아웃 메뉴 숨기기

        val isDarkMode = getSharedPreferences("MyApp", MODE_PRIVATE).getBoolean("dark_mode", false)
        val darkModeItem = popupMenu.menu.findItem(R.id.menu_item_4)
        darkModeItem.title = if (isDarkMode) "다크모드로 변경" else "라이트모드로 변경"

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_item_1 -> {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.cu.ac.kr/life/welfare/schoolbus")))
                    true
                }
                R.id.menu_item_2 -> {
                    startActivity(Intent(this, SelectBusListActivity::class.java))
                    finish()
                    true
                }
                R.id.menu_item_4 -> {
                    val prefs = getSharedPreferences("MyApp", MODE_PRIVATE)
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
}
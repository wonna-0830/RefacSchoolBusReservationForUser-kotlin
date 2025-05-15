package com.example.refac_userbus

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth

class RouteChoose : AppCompatActivity() {
    private var doubleBackToExitPressedOnce = false
    override fun onCreate(saveInstanceState: Bundle?) {
        val prefs = getSharedPreferences("MyApp", MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode", false)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        super.onCreate(saveInstanceState)
        setContentView(R.layout.activity_routechoose)

        val btnCampus = findViewById<Button>(R.id.gyonea)
        val btnSawel = findViewById<Button>(R.id.sawel)
        val btnSawelAnsim = findViewById<Button>(R.id.sawel_ansim)
        val btnAnsim = findViewById<Button>(R.id.ansim)
        val btnHayang = findViewById<Button>(R.id.hayang)

        btnCampus.setOnClickListener{openTimePlace("교내순환")}
        btnSawel.setOnClickListener{openTimePlace("사월역->교내순환")}
        btnSawelAnsim.setOnClickListener{openTimePlace("A2->안심역->사월역")}
        btnAnsim.setOnClickListener{openTimePlace("안심역->교내순환")}
        btnHayang.setOnClickListener{openTimePlace("하양역->교내순환")}

        //팝업메뉴 버튼 정의 후 showPopupMenu 실행
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener{v ->
            showPopupMenu(v)
        }

        val logoutBtn = findViewById<Button>(R.id.btn_logout)
        logoutBtn.setOnClickListener{
            val prefs = getSharedPreferences("MyApp", MODE_PRIVATE)
            prefs.edit().clear().apply() //SharedPreferences 초기화-> 자동로그인 정보 삭제

            FirebaseAuth.getInstance().signOut() //Firebase에서 로그아웃

            val intent = Intent(this, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
        // 뒤로 두 번 누르면 앱 종료
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (doubleBackToExitPressedOnce) {
                    finishAffinity()
                    return
                }

                doubleBackToExitPressedOnce = true
                Toast.makeText(this@RouteChoose, "한 번 더 누르면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show()

                Handler(Looper.getMainLooper()).postDelayed({
                    doubleBackToExitPressedOnce = false
                }, 2000)
            }
        })
    }
    //선택된 노선 이름을 담아서 TimePlace 클래스로 전달
    private fun openTimePlace(routeName: String){
        val intent = Intent(this, TimePlace::class.java).apply {
            putExtra("EXTRA_ROUTE_NAME", routeName)
        }
        startActivity(intent)
    }

    private fun showPopupMenu(v: View) {
        val popupMenu = PopupMenu(this, v)
        popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)

        // RouteChoose에서는 로그아웃 메뉴 숨기기
        popupMenu.menu.findItem(R.id.menu_item_3).isVisible = false

        // 다크모드 상태에 따라 메뉴 이름 미리 설정!
        val prefs = getSharedPreferences("MyApp", MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode", false)
        val darkModeItem = popupMenu.menu.findItem(R.id.menu_item_4)
        darkModeItem.title = if (isDarkMode) "다크모드로 변경" else "라이트모드로 변경"

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_item_1 -> {
                    val url = "https://www.cu.ac.kr/life/welfare/schoolbus"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                    true
                }
                R.id.menu_item_2 -> {
                    val intent = Intent(this, SelectBusList::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.menu_item_4 -> {
                    val prefs = getSharedPreferences("MyApp", MODE_PRIVATE)
                    val isDarkMode = prefs.getBoolean("dark_mode", false)

                    if (isDarkMode) {
                        // 현재 다크모드면 → 라이트모드로 변경
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        prefs.edit().putBoolean("dark_mode", false).apply()
                    } else {
                        // 현재 라이트모드면 → 다크모드로 변경
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                        prefs.edit().putBoolean("dark_mode", true).apply()
                    }

                    // ✅ 테마 바뀌었으니까 화면 새로고침!(recreate 호출했는데도 다크모드 적용x recreate 대신 앱 재실행)
                    val intent = intent
                    finish()
                    startActivity(intent)

                    true
                }

                else -> false
            }
        }

        popupMenu.show()
    }

}
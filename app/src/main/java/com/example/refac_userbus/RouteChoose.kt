package com.example.refac_userbus

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import org.w3c.dom.Text

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

        val currentUser = FirebaseAuth.getInstance().currentUser
        val uid = currentUser?.uid
        val nameTextView = findViewById<TextView>(R.id.userName)

        FirebaseDatabase.getInstance().getReference("users").child(uid!!)
            .child("name")
            .get()
            .addOnSuccessListener { snapshot ->
                val name = snapshot.getValue(String::class.java)
                nameTextView.text = "$name"
            }.addOnFailureListener{nameTextView.text = "이름 불러오기 실패"}

        //팝업메뉴 버튼 정의 후 showPopupMenu 실행
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener{v ->
            showPopupMenu(v)
        }

        val routeContainer = findViewById<LinearLayout>(R.id.routeContainer)

        val routeRef = FirebaseDatabase.getInstance().getReference("routes")

        routeRef.orderByChild("isPinned").equalTo(true)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    Toast.makeText(this, "고정된 노선이 없습니다", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                for (routeSnapshot in snapshot.children) {
                    val routeName = routeSnapshot.child("name").getValue(String::class.java) ?: continue
                    val imageName = routeSnapshot.child("imageName").getValue(String::class.java) ?: ""
                    val stopsSnapshot = routeSnapshot.child("stops")
                    Log.d("ROUTE_DATA", "stops snapshot: ${stopsSnapshot.value}")  // 전체 stops 확인

                    val stations = mutableListOf<String>()
                    for (child in stopsSnapshot.children) {
                        Log.d("ROUTE_DATA", "stop key: ${child.key}, value: ${child.value}")
                        child.getValue(String::class.java)?.let { stations.add(it) }
                    }
                    val times = routeSnapshot.child("times").children.mapNotNull { it.getValue(String::class.java) }

                    Log.d("ROUTE_DATA", "노선명: $routeName, 정류장 수: ${stations.size}, 시간 수: ${times.size}")

                    val button = Button(this).apply {
                        text = routeName

                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            0, // 높이를 0으로 설정하고
                            1f // weight를 1로 줘서 버튼 개수에 따라 자동 분배
                        ).apply {
                            setMargins(0, 8, 0, 8)
                        }

                        setOnClickListener {
                            val intent = Intent(this@RouteChoose, TimePlace::class.java).apply {
                                putExtra("EXTRA_ROUTE_NAME", routeName)
                                putExtra("EXTRA_IMAGE_NAME", imageName)
                                putStringArrayListExtra("EXTRA_STATIONS", ArrayList(stations))
                                putStringArrayListExtra("EXTRA_TIMES", ArrayList(times))



                            }
                            startActivity(intent)
                        }



                    }
                    routeContainer.addView(button)
                    Log.d("ROUTE_DATA", "버튼 추가됨: $routeName")
                }
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "노선 목록을 불러오지 못했습니다: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("ROUTE_DATA", "Firebase 로딩 실패", error)
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
package com.example.refac_userbus

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import org.checkerframework.common.subtyping.qual.Bottom

class SelectBusList : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var reservationList: ArrayList<ReservationData>
    private lateinit var adapter: ReservationAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var textNoReservation: TextView
    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selectbuslist)
        val prefs = getSharedPreferences("MyApp", MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode", false)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        textNoReservation = findViewById(R.id.textNoReservation)

        reservationList = arrayListOf()

        //어댑터 세팅 후 리스트가 비어있으면 예약 없음 문구 표시
        adapter = ReservationAdapter(reservationList) {
            textNoReservation.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        fetchReservation()

        //팝업메뉴 버튼 정의 후 showPopupMenu 실행
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener{v ->
            showPopupMenu(v)
        }

        val backBtn = findViewById<Button>(R.id.btn_home)
        backBtn.setOnClickListener{
            val intent = Intent(this, RouteChoose::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
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
                Toast.makeText(this@SelectBusList, "한 번 더 누르면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show()

                Handler(Looper.getMainLooper()).postDelayed({
                    doubleBackToExitPressedOnce = false
                }, 2000)
            }
        })
    }

    //예약 내역을 불러오기
    private fun fetchReservation(){
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser

        //로그인한 사용자의 uid를 이용해 Firebase에서 예약 데이터를 가져옴
        currentUser?.let { user ->
            val ref = com.google.firebase.database.FirebaseDatabase.getInstance().reference
                .child("users")
                .child(user.uid)
                .child("reservations")

            progressBar.visibility = View.VISIBLE

            ref.get().addOnSuccessListener { snapshot ->
                reservationList.clear()

                //예약 데이터가 존재하면 리스트에 추가해서 recyclerView로 보여주기
                if (snapshot.exists()){
                    for(reservationSnapshot in snapshot.children){
                        val reservation = reservationSnapshot.getValue(ReservationData::class.java)
                        reservation?.let {
                            it.pushKey = reservationSnapshot.key ?: ""
                            reservationList.add(it)
                        }
                    }
                    reservationList.sortByDescending { it.date }
                    recyclerView.visibility = View.VISIBLE
                    textNoReservation.visibility = View.GONE

                } else { //예약 데이터 존재하지 않으면 예약 없음 텍스트 표기
                    textNoReservation.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                }


                //item_list간 간격 조정(RecyclerViewDecoration에서 설정 후 가져와서 간격(20) 지정)
                recyclerView.layoutManager = LinearLayoutManager(this)
                recyclerView.adapter = adapter
                recyclerView.addItemDecoration(RecyclerViewDecoration(20))

                adapter.notifyDataSetChanged()
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun showPopupMenu(v: View){
        val popupMenu = PopupMenu(this, v)
        popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)

        // 다크모드 상태에 따라 메뉴 이름 미리 설정!
        val prefs = getSharedPreferences("MyApp", MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode", false)
        val darkModeItem = popupMenu.menu.findItem(R.id.menu_item_4)
        darkModeItem.title = if (isDarkMode) "다크모드로 변경" else "라이트모드로 변경"

        popupMenu.setOnMenuItemClickListener { item ->
            when(item.itemId){
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
                R.id.menu_item_3 -> {
                    val prefs = getSharedPreferences("MyApp", MODE_PRIVATE)
                    prefs.edit().clear().apply()  // SharedPreferences 초기화

                    FirebaseAuth.getInstance().signOut()  // Firebase 인증 세션 로그아웃

                    val intent = Intent(this, Login::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.menu_item_4-> {
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
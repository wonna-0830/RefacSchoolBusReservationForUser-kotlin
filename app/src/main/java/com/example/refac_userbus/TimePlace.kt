package com.example.refac_userbus

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/*예약 내역에 띄우기 위해 파이어베이스 참조 선언*/
private lateinit var mDatabaseRef: DatabaseReference
private lateinit var mAuth: FirebaseAuth
private var doubleBackToExitPressedOnce = false

class TimePlace : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timeplace)
        val prefs = getSharedPreferences("MyApp", MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode", false)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        //팝업메뉴 버튼 정의 후 showPopupMenu 실행
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener{v ->
            showPopupMenu(v)
        }

        /*데이터베이스 초기화*/
        mDatabaseRef = FirebaseDatabase.getInstance().reference
        mAuth = FirebaseAuth.getInstance()


        val routeName = intent.getStringExtra("EXTRA_ROUTE_NAME") ?: return
        val textRouteName = findViewById<TextView>(R.id.displayRoute)
        val imageView = findViewById<ImageView>(R.id.mapImage)
        val spinnerTime = findViewById<Spinner>(R.id.time_spinner)
        val spinnerPlace = findViewById<Spinner>(R.id.station_spinner)

        textRouteName.text = routeName

        val routeRef = FirebaseDatabase.getInstance().getReference("routes")

        routeRef.orderByChild("name").equalTo(routeName).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (routeSnapshot in snapshot.children) {
                    val imageName = routeSnapshot.child("imageName").getValue(String::class.java) ?: ""
                    val stops = routeSnapshot.child("stops").children.mapNotNull { it.getValue(String::class.java) }
                    val times = routeSnapshot.child("times").children.mapNotNull { it.getValue(String::class.java) }

                    fun getImageResIdFromRouteName(routeName: String): Int {
                        return when (routeName) {
                            "교내순환" -> R.drawable.map_gyonea
                            "하양역->교내순환" -> R.drawable.hayang_station
                            "안심역->교내순환" -> R.drawable.map_gyonea
                            "사월역->교내순환" -> R.drawable.map_gyonea
                            "A2->안심역->사월역" -> R.drawable.ansim_sawel
                            else -> R.drawable.dcu_profile // 기본 이미지
                        }
                    }
                    // 이미지
                    val imageResId = getImageResIdFromRouteName(routeName)
                    imageView.setImageResource(imageResId)

                    // 시간 스피너
                    val currentTime = Calendar.getInstance()
                    val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)
                    val currentMinute = currentTime.get(Calendar.MINUTE)

                    val timeList = mutableListOf("시간을 선택하세요")
                    for (time in times) {
                        val hour = time.substring(0, 2).toIntOrNull()
                        val minute = time.substring(3).toIntOrNull()
                        if (hour != null && minute != null &&
                            (hour > currentHour || (hour == currentHour && minute >= currentMinute))) {
                            timeList.add(time)
                        }
                    }

                    val timeAdapter = ArrayAdapter(this@TimePlace, R.layout.spinner_item_black, timeList)
                    timeAdapter.setDropDownViewResource(R.layout.spinner_dropdown)
                    spinnerTime.adapter = timeAdapter

                    // 정류장 스피너
                    val stationList = mutableListOf("장소를 선택하세요") + stops
                    val stationAdapter = ArrayAdapter(this@TimePlace, R.layout.spinner_item_black, stationList)
                    stationAdapter.setDropDownViewResource(R.layout.spinner_dropdown)
                    spinnerPlace.adapter = stationAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TimePlace, "노선 데이터를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
            }
        })





        //예약 버튼 클릭 시 파이어베이스 저장 및 예약 페이지로 넘어가기
        val btnReserve = findViewById<Button>(R.id.reservation)
        btnReserve.setOnClickListener{
            Log.d("예약", "버튼 눌림")
            //사용자 선택 값 가져오기
            val selectedTime = spinnerTime.selectedItem?.toString() ?: return@setOnClickListener
            val selectedStation = spinnerPlace.selectedItem?.toString() ?: return@setOnClickListener
            val currentDate = SimpleDateFormat("YY-MM-dd", Locale.getDefault()).format(Date())


            // 🔒 예약 금지 조건 검사
            if (selectedTime == "시간을 선택하세요") {
                Toast.makeText(this, "시간을 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedStation == "장소를 선택하세요") {
                Toast.makeText(this, "정류장을 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            /*현재 로그인 되어있는 사용자의 uid 기준으로 경로 설정(연속된 .child가 경로 설정하는 것)
             후에 push(), setValue()로 데이터베이스를 저장
             실시간 DB에 users > {uid} > reservations > pushKey > route, time, station, date 구조로 저장 완료
             저장 후 Log로 저장 성공이 됐는지 확인
             예약이 완료 됨을 알리는 토스트메세지 출력 후 예약 확인 페이지로 넘어감*/
            val currentUser = FirebaseAuth.getInstance().currentUser
            currentUser?.let { user ->
                val ref = FirebaseDatabase.getInstance().reference
                    .child("users")
                    .child(user.uid)
                    .child("reservations")
                // ✅ 예약 중복 체크
                    ref.get().addOnSuccessListener { snapshot ->
                        var alreadyBooked = false
                        var alreadySchoolBusBooked = false
                        var alreadyReturnBusBooked = false

                        for (resSnapshot in snapshot.children) {
                            val res = resSnapshot.getValue(ReservationData::class.java)
                            if (res != null && res.date == currentDate) {
                            // 등교 노선
                                if (res.route == "교내순환" || res.route == "사월역->교내순환" || res.route == "안심역->교내순환" || res.route == "하양역->교내순환") {
                                    alreadySchoolBusBooked = true
                                }
                                // 하교 노선
                                if (res.route == "A2->안심역->사월역") {
                                    alreadyReturnBusBooked = true
                                }
                            }
                        }

                        val isSchoolRoute = routeName in listOf("교내순환", "사월역->교내순환", "안심역->교내순환", "하양역->교내순환")
                        val isReturnRoute = routeName == "A2->안심역->사월역"

                    // 조건에 따라 차단
                         if (isSchoolRoute && alreadySchoolBusBooked) {
                            Toast.makeText(this, "등교버스는 하루에 하나만 예약할 수 있어요.", Toast.LENGTH_SHORT).show()
                            return@addOnSuccessListener
                        }
                        if (isReturnRoute && alreadyReturnBusBooked) {
                            Toast.makeText(this, "하교버스는 하루에 하나만 예약할 수 있어요.", Toast.LENGTH_SHORT).show()
                            return@addOnSuccessListener
                        }

                    // ✅ 저장 로직
                        val reservationData = ReservationData(
                            route = routeName,
                            time = selectedTime,
                            station = selectedStation,
                            date = currentDate
                        )

                        ref.push().setValue(reservationData)
                            .addOnCompleteListener { task ->
                                 if (task.isSuccessful) {
                                    Log.d("예약", "저장 성공")
                                     Toast.makeText(this, "예약이 완료되었습니다", Toast.LENGTH_SHORT).show()

                                     val reservationTimeInMillis = getReservationTimeInMillis(selectedTime)
                                     setAlarm(reservationTimeInMillis) // 알람도 같이 설정
                                     startActivity(Intent(this, SelectBusList::class.java))
                                } else {
                                    Toast.makeText(this, "예약 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                }
            }
        }
        val backBtn = findViewById<Button>(R.id.btn_back)
        backBtn.setOnClickListener{
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
                Toast.makeText(this@TimePlace, "한 번 더 누르면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show()

                Handler(Looper.getMainLooper()).postDelayed({
                    doubleBackToExitPressedOnce = false
                }, 2000)
            }
        })
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

    //10분 전으로 triggerTime을 설정하고 AlarmManager가 알람을 울리도록 함
    @RequiresApi(Build.VERSION_CODES.S)
    private fun setAlarm(reservationTimeInMillis: Long){
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

            // 권한 설정화면으로 이동할 수도 있음
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }
    }

    //시간(selectedTime)을 밀리초로 변환하는 함수
    private fun getReservationTimeInMillis(selectedTime: String): Long {
        val today = Calendar.getInstance()
        val parts = selectedTime.split(":") // 예를 들면 "17:30" 이런 거

        if (parts.size == 2) {
            today.set(Calendar.HOUR_OF_DAY, parts[0].toInt())
            today.set(Calendar.MINUTE, parts[1].toInt())
            today.set(Calendar.SECOND, 0)
        }
        return today.timeInMillis
    }


}
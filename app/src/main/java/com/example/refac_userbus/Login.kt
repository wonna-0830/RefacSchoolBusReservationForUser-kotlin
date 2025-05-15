package com.example.refac_userbus

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import androidx.activity.OnBackPressedCallback


class Login : AppCompatActivity() {
    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var mEtEmail: EditText
    private lateinit var mEtPwd: EditText
    private lateinit var mBtnRegister: Button
    private lateinit var mBtnLogin: Button
    private var doubleBackToExitPressedOnce = false

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Firebase Auth만 초기화
        mFirebaseAuth = FirebaseAuth.getInstance()

        // 레이아웃 요소 초기화
        mEtEmail = findViewById(R.id.school_num_input)
        mEtPwd = findViewById(R.id.number_input)
        mBtnLogin = findViewById(R.id.btn_click)
        mBtnRegister = findViewById(R.id.btn_JOIN)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        // 로그인 버튼 클릭 시
        mBtnLogin.setOnClickListener {
            val strEmail = mEtEmail.text.toString()
            val strPwd = mEtPwd.text.toString()
            val checkBoxAutoLogin = findViewById<CheckBox>(R.id.autoLogin)

            when {
                strEmail.isEmpty() -> {
                    Toast.makeText(this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                strPwd.isEmpty() -> {
                    Toast.makeText(this, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                !strEmail.contains("@") -> {
                    Toast.makeText(this, "알맞지 않은 이메일 형식입니다.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }


            //로그인 버튼 클릭 시 로딩 아이콘 보이게 하기
            progressBar.visibility = View.VISIBLE

            mFirebaseAuth.signInWithEmailAndPassword(strEmail, strPwd)
                .addOnCompleteListener(this) { task ->
                    progressBar.visibility = View.GONE //로그인 성공 바로 전에 로딩 아이콘 숨김 처리
                    if (task.isSuccessful) {
                        // 🔐 로그인 성공 → 사용자 이메일 저장 + 자동 로그인
                        val sharedPref = getSharedPreferences("MyApp", MODE_PRIVATE)
                        //sharedPreference는 내가 만든 저장소가 아니라 안드로이드의 기본 제공 저장 시스템! 저
                        // 장소 내의 MyApp이라는 곳에 이 어플리케이션의 로그인 정보 저장, 로그인 정보에 접근 못하도록 MODE_PRIVATE 적용
                        // 여기서 내가 자동 로그인 체크 표시를 했을 때 로그인 정보를 단말 내 저장소에 저장,
                        // 저장된 값이 있으면 바로 그 정보로 로그인 됨
                        with(sharedPref.edit()) {
                            putBoolean("autoLogin", checkBoxAutoLogin.isChecked)
                            putString("userEmail", strEmail)
                            putString("userPassword", strPwd)
                            apply()
                        }

                        Toast.makeText(this, "로그인에 성공하셨습니다.", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, RouteChoose::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "로그인에 실패하였습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // 회원가입 버튼 → RegisterActivity 이동
        mBtnRegister.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
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
                Toast.makeText(this@Login, "한 번 더 누르면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show()

                Handler(Looper.getMainLooper()).postDelayed({
                    doubleBackToExitPressedOnce = false
                }, 2000)
            }
        })
    }
}
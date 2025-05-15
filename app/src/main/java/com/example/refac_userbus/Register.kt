package com.example.refac_userbus

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Register : AppCompatActivity() {
    private lateinit var mFirebaseAuth : FirebaseAuth
    private lateinit var mEtEmail : EditText
    private lateinit var mEtPwd : EditText
    private lateinit var mBtnRegister : Button
    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mFirebaseAuth = FirebaseAuth.getInstance()
        mEtEmail = findViewById(R.id.et_email)
        mEtPwd = findViewById(R.id.et_pwd)
        mBtnRegister = findViewById(R.id.btn_register)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        mBtnRegister.setOnClickListener{
            val email = mEtEmail.text.toString().trim()
            val password = mEtPwd.text.toString().trim()

            if(email.isEmpty() || password.length < 8){
                Toast.makeText(this, "유효한 이메일과 8자 이상의 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            progressBar.visibility = View.VISIBLE

            mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this){ task ->
                    progressBar.visibility = View.GONE
                if(task.isSuccessful) {
                    Toast.makeText(this, "회원가입 성공", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, Login::class.java))
                    finish()
                } else{
                    Toast.makeText(this, "회원가입 실패:${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        // 뒤로 두 번 누르면 앱 종료
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (doubleBackToExitPressedOnce) {
                    finishAffinity()
                    return
                }

                doubleBackToExitPressedOnce = true
                Toast.makeText(this@Register, "한 번 더 누르면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show()

                Handler(Looper.getMainLooper()).postDelayed({
                    doubleBackToExitPressedOnce = false
                }, 2000)
            }
        })
    }

}
package com.example.refac_userbus.feature.register

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.refac_userbus.R
import com.example.refac_userbus.feature.login.LoginActivity

class RegisterActivity : AppCompatActivity() {
    private lateinit var viewModel: RegisterViewModel
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var nameInput: EditText
    private lateinit var registerBtn: Button
    private lateinit var progressBar: ProgressBar
    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        viewModel = ViewModelProvider(this)[RegisterViewModel::class.java]

        emailInput = findViewById(R.id.et_email)
        passwordInput = findViewById(R.id.et_pwd)
        nameInput = findViewById(R.id.et_name)
        registerBtn = findViewById(R.id.btn_register)
        progressBar = findViewById(R.id.progressBar)

        // 🔁 ViewModel 상태 관찰
        viewModel.registerResult.observe(this) { result ->
            progressBar.visibility = View.GONE
            if (result.isSuccess) {
                Toast.makeText(this, "회원가입 성공", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, result.exceptionOrNull()?.message ?: "회원가입 실패", Toast.LENGTH_SHORT).show()
            }
        }

        // 가입 버튼
        registerBtn.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val name = nameInput.text.toString().trim()

            if (email.isEmpty() || password.length < 8 || name.isEmpty()) {
                Toast.makeText(this, "유효한 이메일과 8자 이상의 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            viewModel.register(email, password, name)
        }

        // 뒤로 두 번 누르면 앱 종료
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (doubleBackToExitPressedOnce) {
                    finishAffinity()
                    return
                }
                doubleBackToExitPressedOnce = true
                Toast.makeText(this@RegisterActivity, "한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
                Handler(Looper.getMainLooper()).postDelayed({
                    doubleBackToExitPressedOnce = false
                }, 2000)
            }
        })
    }
}

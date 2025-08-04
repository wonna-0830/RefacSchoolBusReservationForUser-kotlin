package com.example.refac_userbus.feature.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.refac_userbus.R
import com.example.refac_userbus.feature.routechoose.RouteChooseActivity
import com.example.refac_userbus.feature.register.RegisterActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var viewModel: LoginViewModel
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var autoLoginCheckBox: CheckBox

    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        // UI 초기화
        emailInput = findViewById(R.id.school_num_input)
        passwordInput = findViewById(R.id.number_input)
        loginButton = findViewById(R.id.btn_click)
        registerButton = findViewById(R.id.btn_JOIN)
        progressBar = findViewById(R.id.progressBar)
        autoLoginCheckBox = findViewById(R.id.autoLogin)

        // 로그인 상태 관찰
        viewModel.loginResult.observe(this) { result ->
            progressBar.visibility = View.GONE
            if (result.isSuccess) {
                val uid = result.getOrNull() ?: return@observe
                saveAutoLogin(emailInput.text.toString(), passwordInput.text.toString())
                Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, RouteChooseActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, result.exceptionOrNull()?.message ?: "로그인 실패", Toast.LENGTH_SHORT).show()
            }
        }

        // 버튼 이벤트
        loginButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            progressBar.visibility = View.VISIBLE
            viewModel.login(email, password)
        }

        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (doubleBackToExitPressedOnce) {
                    finishAffinity()
                    return
                }

                doubleBackToExitPressedOnce = true
                Toast.makeText(this@LoginActivity, "한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()

                android.os.Handler(mainLooper).postDelayed({
                    doubleBackToExitPressedOnce = false
                }, 2000)
            }
        })
    }

    private fun saveAutoLogin(email: String, password: String) {
        val sharedPref = getSharedPreferences("MyApp", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("autoLogin", autoLoginCheckBox.isChecked)
            putString("userEmail", email)
            putString("userPassword", password)
            apply()
        }
    }
}

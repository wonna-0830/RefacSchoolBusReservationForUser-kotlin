package com.example.refac_userbus.feature.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.refac_userbus.data.UserRepository

class LoginViewModel : ViewModel() {
    private val repository = UserRepository()

    private val _loginResult = MutableLiveData<Result<String>>()
    val loginResult: LiveData<Result<String>> = _loginResult

    fun login(email: String, password: String) {
        _loginResult.value = Result.failure(Exception("로딩 중..."))  // 로딩 상태
        repository.login(email, password) { result ->
            _loginResult.postValue(result)
        }
    }
}

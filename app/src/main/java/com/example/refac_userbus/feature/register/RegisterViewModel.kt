package com.example.refac_userbus.feature.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.refac_userbus.data.UserRepository

class RegisterViewModel : ViewModel() {
    private val repository = UserRepository()

    private val _registerResult = MutableLiveData<Result<Unit>>()
    val registerResult: LiveData<Result<Unit>> = _registerResult

    fun register(email: String, password: String, name: String) {
        _registerResult.value = Result.failure(Exception("로딩 중..."))
        repository.register(email, password, name) { result ->
            _registerResult.postValue(result)
        }
    }
}

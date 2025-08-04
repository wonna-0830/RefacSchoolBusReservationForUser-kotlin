package com.example.refac_userbus.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().getReference("users")

    fun login(
        email: String,
        password: String,
        callback: (Result<String>) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        db.child(uid).get().addOnSuccessListener { snapshot ->
                            val isBanned = snapshot.child("isBanned").getValue(Boolean::class.java) ?: false
                            if (isBanned) {
                                auth.signOut()
                                callback(Result.failure(Exception("정지된 계정입니다.")))
                            } else {
                                callback(Result.success(uid))
                            }
                        }.addOnFailureListener {
                            callback(Result.failure(Exception("유저 정보 조회 실패")))
                        }
                    } else {
                        callback(Result.failure(Exception("UID 없음")))
                    }
                } else {
                    callback(Result.failure(task.exception ?: Exception("로그인 실패")))
                }
            }
    }

    fun register(
        email: String,
        password: String,
        name: String,
        callback: (Result<Unit>) -> Unit
    ) {
        val joinDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        val userData = mapOf(
                            "name" to name,
                            "email" to email,
                            "joinDate" to joinDate
                        )
                        db.child(uid).setValue(userData)
                            .addOnSuccessListener {
                                callback(Result.success(Unit))
                            }
                            .addOnFailureListener {
                                callback(Result.failure(it))
                            }
                    } else {
                        callback(Result.failure(Exception("UID가 존재하지 않음")))
                    }
                } else {
                    callback(Result.failure(task.exception ?: Exception("회원가입 실패")))
                }
            }
    }

}

package com.example.refac_userbus.feature.routechoose

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RouteChooseViewModel : ViewModel() {

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    private val _routes = MutableLiveData<List<RouteData>>()
    val routes: LiveData<List<RouteData>> = _routes

    fun loadUserName() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseDatabase.getInstance().getReference("users").child(uid).child("name")
            .get()
            .addOnSuccessListener {
                _userName.value = it.getValue(String::class.java) ?: "알 수 없음"
            }
            .addOnFailureListener {
                _userName.value = "이름 불러오기 실패"
            }
    }

    fun loadPinnedRoutes() {
        val ref = FirebaseDatabase.getInstance().getReference("routes")
        ref.orderByChild("isPinned").equalTo(true)
            .get()
            .addOnSuccessListener { snapshot ->
                val result = mutableListOf<RouteData>()
                for (routeSnapshot in snapshot.children) {
                    val name = routeSnapshot.child("name").getValue(String::class.java) ?: continue
                    val image = routeSnapshot.child("imageName").getValue(String::class.java) ?: ""
                    val stops = routeSnapshot.child("stops").children.mapNotNull { it.getValue(String::class.java) }
                    val times = routeSnapshot.child("times").children.mapNotNull { it.getValue(String::class.java) }

                    result.add(RouteData(name, image, stops, times))
                }
                _routes.value = result
            }
            .addOnFailureListener {
                _routes.value = emptyList()
            }
    }
    fun logout() {
        FirebaseAuth.getInstance().signOut()
    }
}

data class RouteData(
    val name: String,
    val image: String,
    val stops: List<String>,
    val times: List<String>
)

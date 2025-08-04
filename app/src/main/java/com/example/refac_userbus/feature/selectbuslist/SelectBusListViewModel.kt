package com.example.refac_userbus.feature.selectbuslist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.refac_userbus.data.model.ReservationData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class SelectBusListViewModel : ViewModel() {

    private val _reservations = MutableLiveData<List<ReservationData>>()
    val reservations: LiveData<List<ReservationData>> = _reservations

    fun loadReservations() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance().reference
            .child("users")
            .child(uid)
            .child("reservations")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newList = mutableListOf<ReservationData>()
                for (reservationSnapshot in snapshot.children) {
                    val reservation = reservationSnapshot.getValue(ReservationData::class.java)
                    reservation?.let {
                        it.pushKey = reservationSnapshot.key ?: ""
                        newList.add(it)
                    }
                }
                newList.sortByDescending { it.date }
                _reservations.value = newList
            }

            override fun onCancelled(error: DatabaseError) {
                _reservations.value = emptyList()
            }
        })
    }

    fun logout() {
        FirebaseAuth.getInstance().signOut()
    }
}

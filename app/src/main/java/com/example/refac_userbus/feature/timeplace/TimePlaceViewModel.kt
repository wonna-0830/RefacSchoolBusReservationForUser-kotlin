package com.example.refac_userbus.feature.timeplace

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import androidx.lifecycle.ViewModel
import com.example.refac_userbus.R
import com.example.refac_userbus.data.model.ReservationData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class TimePlaceViewModel : ViewModel() {

    fun logout() {
        FirebaseAuth.getInstance().signOut()
    }

    fun loadRouteInfo(
        routeName: String,
        context: Context,
        imageView: ImageView,
        timeSpinner: Spinner,
        stationSpinner: Spinner
    ) {
        val routeRef = FirebaseDatabase.getInstance().getReference("routes")

        routeRef.orderByChild("name").equalTo(routeName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (routeSnapshot in snapshot.children) {
                        val imageName = routeSnapshot.child("imageName").getValue(String::class.java) ?: ""
                        val stops = routeSnapshot.child("stops").children.mapNotNull { it.getValue(String::class.java) }
                        val times = routeSnapshot.child("times").children.mapNotNull { it.getValue(String::class.java) }

                        imageView.setImageResource(getImageResIdFromRouteName(routeName))

                        val timeList = mutableListOf("시간을 선택하세요")
                        val now = Calendar.getInstance()
                        val currentHour = now.get(Calendar.HOUR_OF_DAY)
                        val currentMinute = now.get(Calendar.MINUTE)

                        for (time in times) {
                            val hour = time.substring(0, 2).toIntOrNull()
                            val minute = time.substring(3).toIntOrNull()
                            if (hour != null && minute != null &&
                                (hour > currentHour || (hour == currentHour && minute >= currentMinute))) {
                                timeList.add(time)
                            }
                        }

                        val timeAdapter = ArrayAdapter(context, R.layout.spinner_item_black, timeList)
                        timeAdapter.setDropDownViewResource(R.layout.spinner_dropdown)
                        timeSpinner.adapter = timeAdapter

                        val stationList = listOf("장소를 선택하세요") + stops
                        val stationAdapter = ArrayAdapter(context, R.layout.spinner_item_black, stationList)
                        stationAdapter.setDropDownViewResource(R.layout.spinner_dropdown)
                        stationSpinner.adapter = stationAdapter
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun makeReservation(
        context: Context,
        routeName: String,
        selectedTime: String,
        selectedStation: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (selectedTime == "시간을 선택하세요") {
            onFailure("시간을 선택해주세요.")
            return
        }

        if (selectedStation == "장소를 선택하세요") {
            onFailure("정류장을 선택해주세요.")
            return
        }

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance().reference
            .child("users")
            .child(uid)
            .child("reservations")

        val currentDate = SimpleDateFormat("YY-MM-dd", Locale.getDefault()).format(Date())

        ref.get().addOnSuccessListener { snapshot ->
            var alreadySchool = false
            var alreadyReturn = false

            for (child in snapshot.children) {
                val res = child.getValue(ReservationData::class.java)
                if (res != null && res.date == currentDate) {
                    if (res.route in listOf("교내순환", "사월역->교내순환", "안심역->교내순환", "하양역->교내순환")) {
                        alreadySchool = true
                    }
                    if (res.route == "A2->안심역->사월역") {
                        alreadyReturn = true
                    }
                }
            }

            val isSchool = routeName in listOf("교내순환", "사월역->교내순환", "안심역->교내순환", "하양역->교내순환")
            val isReturn = routeName == "A2->안심역->사월역"

            if (isSchool && alreadySchool) {
                onFailure("등교버스는 하루에 하나만 예약할 수 있어요.")
                return@addOnSuccessListener
            }
            if (isReturn && alreadyReturn) {
                onFailure("하교버스는 하루에 하나만 예약할 수 있어요.")
                return@addOnSuccessListener
            }

            val data = ReservationData(
                route = routeName,
                time = selectedTime,
                station = selectedStation,
                date = currentDate
            )

            ref.push().setValue(data).addOnSuccessListener {
                onSuccess(selectedTime)
            }.addOnFailureListener {
                onFailure("예약 실패: ${it.message}")
            }
        }
    }

    fun getReservationTimeInMillis(time: String): Long {
        val now = Calendar.getInstance()
        val parts = time.split(":")
        if (parts.size == 2) {
            now.set(Calendar.HOUR_OF_DAY, parts[0].toInt())
            now.set(Calendar.MINUTE, parts[1].toInt())
            now.set(Calendar.SECOND, 0)
        }
        return now.timeInMillis
    }

    private fun getImageResIdFromRouteName(routeName: String): Int {
        return when (routeName) {
            "교내순환" -> R.drawable.map_gyonea
            "하양역->교내순환" -> R.drawable.hayang_station
            "안심역->교내순환" -> R.drawable.map_gyonea
            "사월역->교내순환" -> R.drawable.map_gyonea
            "A2->안심역->사월역" -> R.drawable.ansim_sawel
            else -> R.drawable.dcu_profile
        }
    }
}

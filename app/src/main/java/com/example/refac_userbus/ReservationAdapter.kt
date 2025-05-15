package com.example.refac_userbus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase

class ReservationAdapter(private val reservationList: ArrayList<ReservationData>,
    private val onListEmpty: () -> Unit)
    : RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder>() {

    //item_list.xml의 보여줄 데이터를 하나씩 인플레이트
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ReservationAdapter.ReservationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list, parent, false)
        return ReservationViewHolder(view)
    }

    //데이터 바인딩 + 삭제 버튼 클릭 이벤트
    override fun onBindViewHolder(holder: ReservationViewHolder, position: Int) {
        val reservation = reservationList[position]

        holder.textRoute.text = reservation.route
        holder.textTime.text = reservation.time
        holder.textStation.text = reservation.station
        holder.textDate.text = reservation.date

        //삭제 버튼 클릭시 현재 사용자의 예약 내역을 데이터베이스에서 삭제
        holder.btnDelete.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("예약 삭제")
                .setMessage("정말 이 예약을 삭제하시겠습니까?")
                .setPositiveButton("확인") { dialog, _ ->
                    val currentUser =
                        com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                    currentUser?.let { user ->
                        val ref = FirebaseDatabase.getInstance().reference
                            .child("users")
                            .child(user.uid)
                            .child("reservations")
                            .child(reservation.pushKey)

                        ref.removeValue()
                    }
                    reservationList.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, reservationList.size)

                    if (reservationList.isEmpty()) {
                        onListEmpty()
                    }
                }
                .setNegativeButton("취소", null) // 취소 누르면 아무 일도 안 함
                .show()
        }


    }

    override fun getItemCount(): Int = reservationList.size

    //item_list.xml에 보여줄 데이터를 묶기
    class ReservationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textRoute: TextView = itemView.findViewById(R.id.textRoute)
        val textTime: TextView = itemView.findViewById(R.id.textTime)
        val textStation: TextView = itemView.findViewById(R.id.textPlace)
        val textDate: TextView = itemView.findViewById(R.id.textDate)
        val btnDelete: Button = itemView.findViewById(R.id.btnCancel)
    }


}
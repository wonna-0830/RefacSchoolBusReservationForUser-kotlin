package com.example.refac_userbus

import android.app.AlertDialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class ReservationAdapter(
    private val onListEmpty: () -> Unit
) : RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder>() {

    private var reservationList: List<ReservationData> = listOf()

    fun updateData(newList: List<ReservationData>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = reservationList.size
            override fun getNewListSize() = newList.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return reservationList[oldItemPosition].pushKey == newList[newItemPosition].pushKey
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return reservationList[oldItemPosition] == newList[newItemPosition]
            }
        })

        reservationList = newList
        diffResult.dispatchUpdatesTo(this)

        if (reservationList.isEmpty()) onListEmpty()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list, parent, false)
        return ReservationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReservationViewHolder, position: Int) {
        val reservation = reservationList[position]

        holder.textRoute.text = reservation.route
        holder.textTime.text = reservation.time
        holder.textStation.text = reservation.station
        holder.textDate.text = reservation.date

        val canDelete = checkCanDelete(reservation.time, reservation.date)

        if (reservation.deleted == true) {
            holder.btnDelete.isEnabled = true
            holder.btnDelete.text = "취소완료"
            holder.btnDelete.setBackgroundColor(Color.BLUE)
            holder.btnDelete.setOnClickListener {
                Toast.makeText(holder.itemView.context, "이미 취소된 예약입니다.", Toast.LENGTH_SHORT).show()
            }
            return
        }

        if (canDelete) {
            holder.btnDelete.isEnabled = true
            holder.btnDelete.text = "삭제"
            holder.btnDelete.setBackgroundColor(Color.RED)

            holder.btnDelete.setOnClickListener {
                AlertDialog.Builder(holder.itemView.context)
                    .setTitle("예약 취소")
                    .setMessage("예약을 취소하시겠습니까?")
                    .setPositiveButton("확인") { _, _ ->
                        val reasons = arrayOf("수업 변경", "버스 예약 실수", "개인 사정", "기타")
                        val selectedItems = mutableListOf<String>()
                        val checkedItems = BooleanArray(reasons.size) { false }

                        val reasonDialog = AlertDialog.Builder(holder.itemView.context)
                            .setTitle("취소 사유를 선택해주세요")
                            .setMultiChoiceItems(reasons, checkedItems) { _, which, isChecked ->
                                if (isChecked) {
                                    selectedItems.add(reasons[which])
                                } else {
                                    selectedItems.remove(reasons[which])
                                }
                            }
                            .setPositiveButton("확인", null) // 나중에 setOnShowListener로 커스텀 처리
                            .setNegativeButton("취소", null)
                            .create()

                        reasonDialog.setOnShowListener {
                            val button = reasonDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            button.setOnClickListener {
                                if (selectedItems.isEmpty()) {
                                    Toast.makeText(holder.itemView.context, "하나 이상의 사유를 선택해주세요.", Toast.LENGTH_SHORT).show()
                                } else {
                                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                                    val reservationId = reservation.pushKey

                                    if (userId != null && reservationId.isNotEmpty()) {
                                        val ref = FirebaseDatabase.getInstance().reference
                                            .child("users")
                                            .child(userId)
                                            .child("reservations")
                                            .child(reservationId)

                                        val updates = mapOf(
                                            "deleted" to true,
                                            "reason" to selectedItems.joinToString(", ")
                                        )

                                        ref.updateChildren(updates).addOnSuccessListener {
                                            Toast.makeText(holder.itemView.context, "예약이 취소되었습니다.", Toast.LENGTH_SHORT).show()

                                            holder.btnDelete.isEnabled = true
                                            holder.btnDelete.text = "취소완료"
                                            holder.btnDelete.setBackgroundColor(Color.BLUE)

                                            holder.btnDelete.setOnClickListener {
                                                Toast.makeText(holder.itemView.context, "이미 취소된 예약입니다ㅓ.", Toast.LENGTH_SHORT).show()
                                            }

                                            reasonDialog.dismiss()
                                        }.addOnFailureListener { e ->
                                            Toast.makeText(holder.itemView.context, "업데이트 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        Toast.makeText(holder.itemView.context, "업데이트할 수 없습니다. (키 누락)", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }

                        reasonDialog.show()
                    }
                    .setNegativeButton("닫기", null)
                    .show()
            }

        } else {
            holder.btnDelete.isEnabled = true
            holder.btnDelete.text = "운행완료"
            holder.btnDelete.setBackgroundColor(Color.LTGRAY)

            holder.btnDelete.setOnClickListener {
                Toast.makeText(holder.itemView.context, "이미 운행된 버스는 삭제할 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int = reservationList.size

    private fun checkCanDelete(busTime: String, dateStr: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("yy-MM-dd HH:mm", Locale.getDefault())
            val busDateTime = sdf.parse("$dateStr $busTime") ?: return false
            val currentTime = Calendar.getInstance().time
            currentTime.before(Date(busDateTime.time + 1 * 60 * 1000)) // 30분 이내면 true
        } catch (e: Exception) {
            false
        }
    }

    class ReservationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textRoute: TextView = itemView.findViewById(R.id.textRoute)
        val textTime: TextView = itemView.findViewById(R.id.textTime)
        val textStation: TextView = itemView.findViewById(R.id.textPlace)
        val textDate: TextView = itemView.findViewById(R.id.textDate)
        val btnDelete: Button = itemView.findViewById(R.id.btnCancel)
    }
}

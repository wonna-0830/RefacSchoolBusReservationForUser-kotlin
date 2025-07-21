package com.example.refac_userbus

data class ReservationData(
    //데이터 저장을 위한 데이터클래스 (사용자가 예약한 정보 하나의 객체로 관리)
    val route: String = "",
    val time: String = "",
    val station: String = "",
    val date: String = "",
    var deleted: Boolean? = null,  // 👈 이 줄 추가!
    var reason: String = "",
    var pushKey: String = ""  //-> FireBase에 저장된 고유키 (삭제할 때 필요)
)

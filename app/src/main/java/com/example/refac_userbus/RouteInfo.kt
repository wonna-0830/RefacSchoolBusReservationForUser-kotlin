package com.example.refac_userbus

data class RouteInfo(
    val imageResId: Int,
    val times: List<String>,
    val stations: List<String>,
    var displayName: String,
)
//사용자가 선택할 노선, 이미지, 시간, 정류장 리스트 선언
val routeMap = mapOf(
    "교내순환" to RouteInfo(
        R.drawable.map_gyonea,
        listOf("시간을 선택하세요", "08:30", "08:35", "08:40", "08:55", "09:00"
            , "09:30", "09:40", "09:50", "10:00", "10:10", "10:20", "10:30"
            , "10:40", "10:50", "11:00"),
        listOf("장소를 선택하세요", "정문", "B1", "C7", "C13", "D6", "A2(건너편)"),
        displayName = "교내순환"
    ),
    "하양역->교내순환" to RouteInfo(
        R.drawable.hayang_station,
        listOf("시간을 선택하세요", "08:30", "08:50", "08:57", "09:10"),
        listOf("장소를 선택하세요", "하양역","정문", "B1", "C7", "C13", "D6", "A2(건너편)"),
        displayName = "하양역->교내순환"
    ),
    "안심역->교내순환" to RouteInfo(
        R.drawable.map_gyonea,
        listOf("시간을 선택하세요", "08:10", "08:20", "08:25", "08:30", "08:40"
            , "08:50", "09:00", "09:30", "09:50", "10:10", "10:20", "10:30"
            , "12:20"),
        listOf("장소를 선택하세요", "안심역(3번출구)", "정문", "B1", "C7", "C13", "D6", "A2(건너편)"),
        displayName = "안심역->교내순환"
    ),
    "사월역->교내순환" to RouteInfo(
        R.drawable.map_gyonea,
        listOf("시간을 선택하세요", "08:00", "08:05", "08:20", "08:40", "09:00"
            , "09:30", "09:50", "10:00"),
        listOf("장소를 선택하세요", "사월역(3번출구)", "정문", "B1", "C7", "C13", "D6", "A2(건너편)"),
        displayName = "사월역->교내순환"
    ),
    "A2->안심역->사월역" to RouteInfo(
        R.drawable.ansim_sawel,
        listOf("시간을 선택하세요", "16:00", "16:15", "16:30", "16:40", "16:50", "17:00", "17:16", "17:30", ),
        listOf("장소를 선택하세요", "A2(건너편)", "안심역", "사월역"),
        displayName = "A2->안심역->사월역"
    )
)

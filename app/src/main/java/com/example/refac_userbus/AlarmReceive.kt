package com.example.refac_userbus

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class AlarmReceive : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val builder = NotificationCompat.Builder(context, "bus_reminder")
            .setSmallIcon(R.drawable.smile)
            .setContentTitle("셔틀버스 알림")
            .setContentText("곧 셔틀버스가 도착합니다! 늦지않게 탑승해주세요.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                "bus_reminder",
                "버스 알림",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(1, builder.build())
    }
}
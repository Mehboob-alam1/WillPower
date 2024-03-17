package com.example.willpower

import android.Manifest
import android.app.NotificationChannel
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.RingtoneManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val challengeTitle = intent.getStringExtra("challengeTitle") ?: "Challenge"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "challenge_channel",
                "Challenge Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Channel for Challenge Notifications"
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, "challenge_channel")
            .setContentTitle("Daily Challenge")
            .setContentText(challengeTitle)
            .setSmallIcon(R.drawable.baseline_star_border_24) // ensure this icon exists
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000)) // Vibration pattern
            .setLights(Color.BLUE, 3000, 3000)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
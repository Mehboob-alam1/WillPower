package com.example.willpower

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.os.PowerManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.v("dailyAlarm", "AlarmReceiver onReceive...")

        /*val stopAlarmIntent = Intent(context, StopAlarmActivity::class.java)
        stopAlarmIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(stopAlarmIntent)*/


        // Get the time when the alarm was triggered
        val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

        // Put the time information as extra in the intent
        val stopAlarmIntent = Intent(context, StopAlarmActivity::class.java).apply {
            putExtra("ALARM_TIME", currentTime)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        // Acquire a partial WakeLock
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag")
        wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/)

//        val stopAlarmIntent = Intent(context, StopAlarmActivity::class.java)
//        stopAlarmIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//        context.startActivity(stopAlarmIntent)
        context.startActivity(stopAlarmIntent)

        // Release the WakeLock
        wakeLock.release()

    }
}
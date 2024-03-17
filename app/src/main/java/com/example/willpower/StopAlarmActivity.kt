package com.example.willpower

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView

class StopAlarmActivity : AppCompatActivity() {
    private var ringtone: Ringtone? = null

    private var snoozeHandler: Handler? = null
    private var snoozeRunnable: Runnable? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)

        setContentView(R.layout.activity_stop_alarm)

        val text= findViewById<TextView>(R.id.alarmLabel)


        playRingtone()

        val stopButton = findViewById<Button>(R.id.stopButton)
        stopButton.setOnClickListener {
            stopRingtone()
            finish()
        }

        val snoozeButton=findViewById<Button>(R.id.snoozeButton);

        snoozeButton.setOnClickListener {
            snoozeAlarm()
        }
    }



    private fun playRingtone() {
        try {
            val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ringtone = RingtoneManager.getRingtone(applicationContext, ringtoneUri)
            ringtone?.play()
            Log.v("playRingtone", "Ringtone started")
        } catch (e: Exception) {
            Log.e("playRingtone", "Error playing ringtone: ${e.message}")
        }
    }

    private fun stopRingtone() {
        try {
            ringtone?.stop()
            Log.v("stopRingtone", "Ringtone stopped")
        } catch (e: Exception) {
            Log.e("stopRingtone", "Error stopping ringtone: ${e.message}")
        }
    }
    @SuppressLint("ScheduleExactAlarm")
    private fun snoozeAlarm() {
        try {
            // Stop the current ringtone
            stopRingtone()

            // Calculate the snooze time (10 minutes from now)
            val snoozeTime = System.currentTimeMillis() + 10 * 60 * 1000 // 10 minutes in milliseconds

            // Create an intent to trigger the alarm receiver
            val intent = Intent(applicationContext, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(applicationContext, 0, intent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

            // Get the AlarmManager service and schedule the alarm
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, snoozeTime, pendingIntent)

            Log.v("snoozeAlarm", "Alarm snoozed for 10 minutes")
        } catch (e: Exception) {
            Log.e("snoozeAlarm", "Error snoozing alarm: ${e.message}")
        }

        finish()
    }



}
package com.example.willpower

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar


class AlarmHelper(private val context: Context) {

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun setAlarm(alarmId: Int, alarmHour: Int, alarmMinute: Int) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarmHour)
            set(Calendar.MINUTE, alarmMinute)
            set(Calendar.SECOND, 0)
        }

        val alarmIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(context, alarmId, intent, PendingIntent.FLAG_IMMUTABLE)
        }

        /*try {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, alarmIntent)
        } catch (e: SecurityException) {
            TODO("Not yet implemented")
        }*/

        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, alarmIntent)
        } catch (e: SecurityException) {
            // Handle the security exception
        }
    }

    fun cancelAlarm(alarmId: Int) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, alarmId, intent, 0)
        alarmManager.cancel(pendingIntent)
    }
}


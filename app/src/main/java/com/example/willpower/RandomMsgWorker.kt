package com.example.willpower

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.willpower.dadabase.DatabaseHelper
import kotlin.random.Random

class RandomMsgWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        Log.v("RandomMsgWorker", "Worker started")
        val dbHelper = DatabaseHelper(applicationContext)
        val newMessage = getRandomStartMessage()
        dbHelper.updateAllMessages(newMessage)

        Log.v("RandomMsgWorker", "Messages updated with: $newMessage")
        return Result.success()
    }

    fun getRandomStartMessage(): String {
        val messages = listOf(
            "Today is a new beginning. Embrace your journey!",
            "Every small step is progress. Keep moving forward!",
            "You have the power to create change. Start now!",
            "Believe in yourself and your ability to succeed.",
            "Turn your 'I can't' into 'I will'. You've got this!",
            "Your goals are within reach. Stay focused and determined.",
            "Consistency is key. Keep pushing through!",
            "Embrace the challenge. Your potential is limitless.",
            "Make today count. You're capable of amazing things!",
            "Remember why you started. Stay motivated and inspired."
        )
        return messages[Random.nextInt(messages.size)]
    }
}

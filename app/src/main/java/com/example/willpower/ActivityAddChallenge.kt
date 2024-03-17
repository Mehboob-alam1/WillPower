package com.example.willpower

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.willpower.dadabase.DatabaseHelper
import com.example.willpower.model.Challenge
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

const val CHANNEL_ID = "challenge_notification_channel"

class ActivityAddChallenge : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private var currentChallengeId: Long = -1
    private var alarmHour: Int = 0
    private var alarmMinute: Int = 0
    private lateinit var alarmHelper: AlarmHelper
    private var isAlarmEnabled = false // Flag to track if the alarm is enabled

    private lateinit var alarmManager: AlarmManager
    private lateinit var alarmIntent: PendingIntent
    private lateinit var toggleAlarmSwitch: Switch
    private var intAlarm: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_challenge)
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val intent = Intent(this, AlarmReceiver::class.java)

        alarmHelper = AlarmHelper(this)
        dbHelper = DatabaseHelper(this)
        toggleAlarmSwitch = findViewById(R.id.toggleAlarmSwitch)

        val buttonAddChallenge = findViewById<Button>(R.id.buttonAddChallenge)
        val buttonCancel = findViewById<Button>(R.id.buttonCancel)
        val buttonUpdateChallenge = findViewById<Button>(R.id.buttonUpdateChallenge)
        val buttonDeleteChallenge = findViewById<Button>(R.id.buttonDeleteChallenge)
        val btnTimePicker = findViewById<ImageView>(R.id.btnTimePicker)

        val challengeJson = intent.getStringExtra("challengeJson")
        val challengeType = object : TypeToken<Challenge>() {}.type
        val gson = Gson()
        val challengeObj = gson.fromJson<Challenge>(challengeJson, challengeType)
        // Check if there's an incoming challenge ID

        Log.d("JSON",""+challengeJson)

        //val challengeId = intent.getLongExtra("challengeId", -1)
        if (challengeObj != null) {
            // Code Added
            val editChalenge = findViewById<EditText>(R.id.editTextChallenge)
            editChalenge.setText(challengeObj.challenge)
            val editBenifits = findViewById<EditText>(R.id.editTextBenefits)
            editBenifits.setText(challengeObj.benefits)
            val switch = findViewById<Switch>(R.id.toggleAlarmSwitch)
            if (challengeObj.alarmEnabled == 1) {
                switch.isChecked = true
            }
        }
        val challengeId = challengeObj.id
        Log.d("ChallengeID", "" + challengeId)
        Toast.makeText(this, "" + challengeId, Toast.LENGTH_SHORT).show()
        if (challengeId != -1L) {
            currentChallengeId = challengeId
            val challenge = dbHelper.getChallenge(challengeId)
            challenge?.let { populateFormWithChallenge(it) }

            buttonAddChallenge.visibility = View.GONE
            buttonUpdateChallenge.visibility = View.VISIBLE
            buttonDeleteChallenge.visibility = View.VISIBLE
        } else {
            buttonAddChallenge.visibility = View.VISIBLE
            buttonUpdateChallenge.visibility = View.GONE
            buttonDeleteChallenge.visibility = View.GONE
        }

        btnTimePicker.setOnClickListener {
            // Get Current Time
            val c = Calendar.getInstance()
            val hour = c.get(Calendar.HOUR_OF_DAY)
            val minute = c.get(Calendar.MINUTE)

            // Launch Time Picker Dialog
            val timePickerDialog = TimePickerDialog(
                this,
                { view, hourOfDay, minute ->

                    alarmHour = hourOfDay
                    alarmMinute = minute

                }, hour, minute, false
            )
            timePickerDialog.show()
        }

        buttonAddChallenge.setOnClickListener {

            val dialogFragment = CustomDialogFragment.newInstance(
                "Challenge Instructions",
                "Keep you daily tasks simple." +
                        "The goal is to create a new habit, the difficulty comes later.\n" +
                        "REWARD: Destiny Control + 1", true,
                "PENALTY: day # - 2 on report missed.\n", true,
                "Do you accept the challenge?", true
            ).apply {
                onPositiveClick = {

                    addChallengeToDatabase()
                }
                onNegativeClick = {
                    // Handle negative button click for showDialog1
                }
            }
            dialogFragment.show(supportFragmentManager, "CustomDialog1")
        }

        buttonCancel.setOnClickListener {
            finish()
        }

        buttonUpdateChallenge.setOnClickListener {
            updateChallengeInDatabase()
        }

        buttonDeleteChallenge.setOnClickListener {
            val dialogFragment = CustomDialogFragment.newInstance(
                "Attention!",
                "...", false,
                "...", false,
                "Do you want to delete this challenge?", true
            ).apply {
                onPositiveClick = {
                    deleteChallengeFromDatabase()
                }
                onNegativeClick = {
                    // Handle negative button click for showDialog1
                }
            }
            dialogFragment.show(supportFragmentManager, "CustomDialog1")
        }


        loadAlarmState()

        // Set the initial state of the switch
        toggleAlarmSwitch.isChecked = isAlarmEnabled

        // Set up switch change listener
        toggleAlarmSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Update the alarm state
            isAlarmEnabled = isChecked
            // Save the updated state in the database
            saveAlarmState()
        }
    }

    private fun loadAlarmState() {
        // Load the alarm state from the database based on the current challenge ID
        // You need to implement this method in your DatabaseHelper class
        // For demonstration purposes, I'll assume you have a method like getAlarmState() in DatabaseHelper
        isAlarmEnabled = dbHelper.getAlarmState(currentChallengeId)
    }

    private fun saveAlarmState() {
        // Save the alarm state in the database based on the current challenge ID
        // You need to implement this method in your DatabaseHelper class
        // For demonstration purposes, I'll assume you have a method like updateAlarmState() in DatabaseHelper
        dbHelper.updateAlarmState(currentChallengeId, isAlarmEnabled)
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)

        outState.putBoolean("isAlarmEnabled", isAlarmEnabled)

    }

    private fun setDailyAlarm(hour: Int, minute: Int, descriptionStr: String): Int {
        Log.v("dailyAlarm", "setDailyAlarm() >>minute: $minute, hour: $hour, text: $descriptionStr")
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("description", descriptionStr)
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val alarmIntent = PendingIntent.getBroadcast(this, 0, intent, flags)

        val calendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                alarmIntent
            )
        } else {
            // Fallback for older versions - use setExact() or setRepeating() as needed
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                alarmIntent
            )
        }

        // Generate a unique ID based on the current time
        val uniqueId = (System.currentTimeMillis() % Integer.MAX_VALUE).toInt()

        // Save alarm details for later use (e.g., update, delete)
        // This could be saved in SharedPreferences, a database, etc.

        return uniqueId
    }


    /*fun scheduleDailyNotification(context: Context, hour: Int, minute: Int, challengeTitle: String): Int {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        intent.putExtra("challengeTitle", challengeTitle)

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        // Ensure the alarm fires the next day if the time has already passed for today
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val uniqueId = System.currentTimeMillis().toInt() // Generating a unique ID for the PendingIntent
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(context, uniqueId, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            PendingIntent.getBroadcast(context, uniqueId, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )

        return uniqueId
    }*/

    private fun addChallengeToDatabase() {
        val challengeText = findViewById<EditText>(R.id.editTextChallenge).text.toString()
        val benefitsText = findViewById<EditText>(R.id.editTextBenefits).text.toString()
        val startDate = getCurrentDate()
        val day = 0
        val level = "easy"
        val message = "I can do it!"
        val lastUpdateDate = "0000-00-00"
//        var alarmId = scheduleDailyNotification(this, alarmHour, alarmMinute, challengeText)
//        var alarmId = setDailyAlarm(alarmHour, alarmMinute, challengeText)
        var alarmId = (System.currentTimeMillis() % Integer.MAX_VALUE).toInt()
//        var alarmId = 111
        if (isAlarmEnabled) {
            intAlarm = 1
        }
        Log.v("dailyAlarm", "addChallengeToDatabase() >>alarmId: " + alarmId)

        // Create a new Challenge object using named arguments will not need the ID
        val newChallenge = Challenge(
            challenge = challengeText,
            benefits = benefitsText,
            startDate = startDate,
            day = day,
            level = level,
            message = message,
            lastUpdateDate = lastUpdateDate,
            alarmId = alarmId,
            alarmEnabled = intAlarm
        )
        val result = dbHelper.addChallenge(newChallenge)

        if (result) {
            Toast.makeText(this, "Challenge added successfully!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Failed to add challenge.", Toast.LENGTH_SHORT).show()
        }
        alarmHelper.setAlarm(alarmId, alarmHour, alarmMinute)
    }

    fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun populateFormWithChallenge(challenge: Challenge) {
        findViewById<EditText>(R.id.editTextChallenge).setText(challenge.challenge)
        findViewById<EditText>(R.id.editTextBenefits).setText(challenge.benefits)
    }

    private fun updateChallengeInDatabase() {
        if (currentChallengeId == -1L) {
            Toast.makeText(this, "No challenge to update", Toast.LENGTH_SHORT).show()
            return
        }

        val challengeText = findViewById<EditText>(R.id.editTextChallenge).text.toString()
        val benefitsText = findViewById<EditText>(R.id.editTextBenefits).text.toString()

        // Get the current challenge data from the database
        val currentChallenge = dbHelper.getChallenge(currentChallengeId)
        if (currentChallenge == null) {
            Toast.makeText(this, "Challenge not found", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedChallenge = currentChallenge.copy(
            challenge = challengeText,
            benefits = benefitsText
        )

        val result = dbHelper.updateChallengeAndBenefits(updatedChallenge)

        if (result) {
            Toast.makeText(this, "Challenge updated successfully!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Failed to update challenge.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteChallengeFromDatabase() {
        if (currentChallengeId == -1L) {
            Toast.makeText(this, "No challenge to delete", Toast.LENGTH_SHORT).show()
            return
        }

        val result = dbHelper.deleteChallenge(currentChallengeId)

        if (result) {
            Toast.makeText(this, "Challenge deleted successfully!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Failed to delete challenge.", Toast.LENGTH_SHORT).show()
        }
    }
}
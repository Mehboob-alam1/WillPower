package com.example.willpower

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import com.example.willpower.dadabase.DatabaseHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*
import java.util.concurrent.TimeUnit

import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var adapter: ChallengeAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        databaseHelper = DatabaseHelper(this)
        adapter = ChallengeAdapter(emptyList(), databaseHelper) // Initialize with empty list
        recyclerView = findViewById<RecyclerView>(R.id.recyclerViewChallenges)
        recyclerView.layoutManager = LinearLayoutManager(this) // Set LayoutManager
        recyclerView.adapter = adapter
        loadChallenges()

        checkAndRequestPermissions()
        scheduleRandomMsgWorker(this)

        val addChallengeBtn = findViewById<FloatingActionButton>(R.id.fab_add_challenge)
        addChallengeBtn.setOnClickListener {
            val intent = Intent(this, ActivityAddChallenge::class.java)
            startActivity(intent)
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        // Check for POST_NOTIFICATIONS permission on Android 13 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Check for VIBRATE permission on Android 6.0 (Marshmallow) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.VIBRATE)
                != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(android.Manifest.permission.VIBRATE)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestMultiplePermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private val requestMultiplePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach { entry ->
            if (!entry.value) {
                // Permission is denied. You can show a message to the user.
                // Handle each permission accordingly.
            }
        }
        // Continue with functionality if permissions are granted.
    }


    /*private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {

                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted. Continue with the functionality.
        } else {
            // Permission is denied. You can show a message to the user and close the app or limit the functionality.
        }
    }*/

    fun scheduleRandomMsgWorker(context: Context) {
        Log.v("MainActivity", "Scheduling RandomMsgWorker")

        val targetTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 1)
            set(Calendar.MINUTE, 1)
            set(Calendar.SECOND, 0)
        }

        // Calculate the delay until the target time
        val delay = targetTime.timeInMillis - System.currentTimeMillis()
        if (delay < 0) {
            targetTime.add(Calendar.DAY_OF_YEAR, 1)  // Schedule for next day
        }

        val randomMsgWorkerRequest = OneTimeWorkRequestBuilder<RandomMsgWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueue(randomMsgWorkerRequest)

        Log.v("MainActivity", "RandomMsgWorker scheduled for: ${targetTime.time}")
    }


    override fun onResume() {
        super.onResume()
        loadChallenges()
    }

    private fun loadChallenges() {
        val challenges = databaseHelper.getAllChallengesDesc()
        adapter.updateData(challenges) // Update adapter data
    }
}
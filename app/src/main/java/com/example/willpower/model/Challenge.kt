package com.example.willpower.model

import java.io.Serializable

data class Challenge(
    val id: Long = -1L,
    val challenge: String,
    val benefits: String,
    val startDate: String, // Using String to simplify, can be changed to Date with proper formatting
    var day: Int,
    val level: String,
    var message: String,
    var lastUpdateDate: String,
    var alarmId: Int,
    var alarmEnabled:Int
): Serializable

/*
Remember that while Serializable is a quick way to make objects passable through Intents, it's not
the most performance-efficient method in Android. For more complex or larger data objects,
Parcelable is recommended, but it requires a bit more implementation work. For your current use
case, Serializable should suffice.
E.G. on your "intent.putExtra("editChallenge", challenge)" on the ChallengeAdapter.kt.
 */

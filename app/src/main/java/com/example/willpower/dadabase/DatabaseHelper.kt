package com.example.willpower.dadabase

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.willpower.model.Challenge
import android.util.Log


class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "challengeDatabase"
        private const val DATABASE_VERSION = 2

        private const val TABLE_NAME = "challenges"
        private const val COLUMN_ID = "id"
        private const val COLUMN_CHALLENGE = "challenge"
        private const val COLUMN_BENEFITS = "benefits"
        private const val COLUMN_START_DATE = "startDate"
        private const val COLUMN_DAY = "day"
        private const val COLUMN_LEVEL = "level"
        private const val COLUMN_MESSAGE = "message"
        private const val COLUMN_LAST_UPDATE_DATE = "lastUpdateDate"
        private const val COLUMN_ALARM_ID = "alarmId"
        private const val COLUMN_ALARM_ENABLED = "alarm_enabled"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableStatement = "CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_CHALLENGE TEXT," +
                "$COLUMN_BENEFITS TEXT," +
                "$COLUMN_START_DATE TEXT," +
                "$COLUMN_DAY INTEGER," +
                "$COLUMN_LEVEL TEXT," +
                "$COLUMN_MESSAGE TEXT," +
                "$COLUMN_LAST_UPDATE_DATE TEXT," +
                "$COLUMN_ALARM_ID INTEGER," +
                "$COLUMN_ALARM_ENABLED INTEGER DEFAULT 0)"

        db.execSQL(createTableStatement)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun updateAllMessages(newMessage: String) {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_MESSAGE, newMessage)
        }

        // Update all rows with the new message
        val success = db.update(TABLE_NAME, contentValues, null, null)
        db.close()

        Log.v("updateAllMessages", "Updated rows: $success")
    }

    //testing
    fun updateMessage(id: Long, message: String): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_MESSAGE, message)
        }

        // Updating row
        val success = db.update(TABLE_NAME, contentValues, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
        return success > 0
    }

    fun updateChallengeDayCount(id: Long, newDay: Int, newLastUpdateDate: String): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_DAY, newDay)
            put(COLUMN_LAST_UPDATE_DATE, newLastUpdateDate)
        }

        // Updating row
        val success = db.update(TABLE_NAME, contentValues, "$COLUMN_ID=?", arrayOf(id.toString()))
        db.close()
        return success > 0
    }


    fun updateChallengeAndBenefits(challenge: Challenge): Boolean {
        val db = this.writableDatabase
        val cv = ContentValues()

        cv.put(COLUMN_CHALLENGE, challenge.challenge)
        cv.put(COLUMN_BENEFITS, challenge.benefits)

        // Update only the specified fields for the challenge with the given ID
        val result = db.update(TABLE_NAME, cv, "$COLUMN_ID=?", arrayOf(challenge.id.toString()))
        db.close()
        return result > 0
    }

    // Add methods to insert, query, update, and delete data
    fun addChallenge(challenge: Challenge): Boolean {
        val db = this.writableDatabase
        val cv = ContentValues()

        cv.put(COLUMN_CHALLENGE, challenge.challenge)
        cv.put(COLUMN_BENEFITS, challenge.benefits)
        cv.put(COLUMN_START_DATE, challenge.startDate)
        cv.put(COLUMN_DAY, challenge.day)
        cv.put(COLUMN_LEVEL, challenge.level)
        cv.put(COLUMN_MESSAGE, challenge.message)
        cv.put(COLUMN_LAST_UPDATE_DATE, challenge.lastUpdateDate)
        cv.put(COLUMN_ALARM_ID, challenge.alarmId)
        cv.put(COLUMN_ALARM_ENABLED,challenge.alarmEnabled)

        val result = db.insert(TABLE_NAME, null, cv)
        db.close()
        return result != (-1).toLong()
    }

    @SuppressLint("Range")
    fun getAllChallengesDesc(): List<Challenge> {
        val challengeList = ArrayList<Challenge>()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_NAME ORDER BY $COLUMN_ID DESC"
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                val challenge = Challenge(
                    cursor.getLong(cursor.getColumnIndex(COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_CHALLENGE)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_BENEFITS)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_START_DATE)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_DAY)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_LEVEL)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_MESSAGE)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_LAST_UPDATE_DATE)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_ALARM_ID)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_ALARM_ENABLED))
                )
                challengeList.add(challenge)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return challengeList
    }

    @SuppressLint("Range")
    fun getChallenge(id: Long): Challenge? {
        val db = this.readableDatabase
        var challenge: Challenge? = null

        val cursor = db.query(
            TABLE_NAME,
            arrayOf(
                COLUMN_ID,
                COLUMN_CHALLENGE,
                COLUMN_BENEFITS,
                COLUMN_START_DATE,
                COLUMN_DAY,
                COLUMN_LEVEL,
                COLUMN_MESSAGE,
                COLUMN_LAST_UPDATE_DATE,
                COLUMN_ALARM_ID,
                COLUMN_ALARM_ENABLED
            ),
            "$COLUMN_ID=?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )

        if (cursor.moveToFirst()) {
            challenge = Challenge(
                id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID)),
                challenge = cursor.getString(cursor.getColumnIndex(COLUMN_CHALLENGE)),
                benefits = cursor.getString(cursor.getColumnIndex(COLUMN_BENEFITS)),
                startDate = cursor.getString(cursor.getColumnIndex(COLUMN_START_DATE)),
                day = cursor.getInt(cursor.getColumnIndex(COLUMN_DAY)),
                level = cursor.getString(cursor.getColumnIndex(COLUMN_LEVEL)),
                message = cursor.getString(cursor.getColumnIndex(COLUMN_MESSAGE)),
                lastUpdateDate = cursor.getString(cursor.getColumnIndex(COLUMN_LAST_UPDATE_DATE)),
                alarmId = cursor.getInt(cursor.getColumnIndex(COLUMN_ALARM_ID)),
                alarmEnabled= cursor.getInt(cursor.getColumnIndex(COLUMN_ALARM_ENABLED))

            )
        }
        Log.d("DatabaseHelper", "Last Update Date: ${challenge?.lastUpdateDate}")

        cursor.close()
        db.close()
        return challenge
    }

    fun deleteChallenge(id: Long): Boolean {
        val db = this.writableDatabase
        val result = db.delete(TABLE_NAME, "$COLUMN_ID=?", arrayOf(id.toString()))
        db.close()
        return result > 0
    }


    @SuppressLint("Range")
    fun getAlarmState(challengeId: Long): Boolean {
        val db = this.readableDatabase
        val query = "SELECT $COLUMN_ALARM_ENABLED FROM $TABLE_NAME WHERE $COLUMN_ID = ?"
        val cursor: Cursor? = db.rawQuery(query, arrayOf(challengeId.toString()))
        var alarmEnabled = false

        cursor?.use {
            if (it.moveToFirst()) {
                alarmEnabled = it.getInt(it.getColumnIndex(COLUMN_ALARM_ENABLED)) == 1
            }
        }

        cursor?.close()
        return alarmEnabled
    }

    fun updateAlarmState(challengeId: Long, isEnabled: Boolean) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ALARM_ENABLED, if (isEnabled) 1 else 0)
        }
        db.update(
            TABLE_NAME,
            values,
            "$COLUMN_ID = ?",
            arrayOf(challengeId.toString())
        )
    }
}

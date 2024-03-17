package com.example.willpower

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.willpower.dadabase.DatabaseHelper
import com.example.willpower.model.Challenge
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random


class ChallengeAdapter(private var challenges: List<Challenge>, private val dbHelper: DatabaseHelper) : RecyclerView.Adapter<ChallengeAdapter.ChallengeViewHolder>() {

    class ChallengeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewChallenge: TextView = view.findViewById(R.id.textViewChallenge)
        val textViewBenefits: TextView = view.findViewById(R.id.textViewBenefits)
        val textViewStartDate: TextView = view.findViewById(R.id.textViewStartDate)
        val textViewDay: TextView = view.findViewById(R.id.textViewDay)
        val textViewLevel: TextView = view.findViewById(R.id.textViewLevel)
        val textViewMessage: TextView = view.findViewById(R.id.textViewMessage)
        val editIcon: ImageView = view.findViewById(R.id.editIcon)
        val completitionStar: ImageView = view.findViewById(R.id.completitionStar)
        val progressBar: ProgressBar = view.findViewById(R.id.challengeProgressBar)
        val randomStartMessage: TextView = view.findViewById(R.id.textViewRandomMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_challenge, parent, false)
        return ChallengeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChallengeViewHolder, position: Int) {
        val challenge = challenges[position]
        holder.textViewChallenge.text = challenge.challenge
        holder.textViewBenefits.text = challenge.benefits
        holder.textViewStartDate.text = challenge.startDate
        holder.textViewDay.text = challenge.day.toString() // Assuming day is an Int
        holder.textViewLevel.text = challenge.level
        holder.textViewMessage.text = challenge.message
        holder.progressBar.progress = challenge.day
        holder.randomStartMessage.text = challenge.alarmId.toString()

        //Print random start message on all item_challenge
        /*val sharedPrefs = holder.itemView.context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val message = sharedPrefs.getString("motivationalMessage", "")
        Log.d("ChallengeAdapter", "Motivational Message: $message")
        holder.randomStartMessage.text = message*/

        holder.itemView.setOnClickListener {
            Toast.makeText(holder.itemView.context, "" +challenge.id, Toast.LENGTH_SHORT).show()

        }
        holder.editIcon.setOnClickListener {
            val context = holder.itemView.context
            val updateIntent = Intent(context, ActivityAddChallenge::class.java).apply {
                putExtra("challengeId", challenge.id)
            }
            context.startActivity(updateIntent)
        }
//
//        holder.editIcon.setOnClickListener {
//            val context = holder.itemView.context
//            val gson = Gson()
//            val challengeJson = gson.toJson(challenge)
//
//            val updateIntent = Intent(context, ActivityAddChallenge::class.java).apply {
//                putExtra("challengeJson", challengeJson)
//            }
//            context.startActivity(updateIntent)
//        }

        holder.completitionStar.setOnClickListener {
            val currentDate = getCurrentDate()
            val challenge = challenges[position]

            playSound(holder.itemView.context)

            //To save message to the database
            /*val messageUpdated = dbHelper.updateMessage(challenge.id, "TESTING 2  ")
            if (messageUpdated) {
                Log.d("ChallengeAdapter", "Message updated successfully for ID: ${challenge.id}")
            } else {
                Log.e("ChallengeAdapter", "Failed to update message for ID: ${challenge.id}")
            }*/

            //Display random message without saving to database
            challenge.message = getRandomEncouragingMessage();
            notifyItemChanged(position)

            if(challenge.lastUpdateDate != currentDate) {
                val newDay = challenge.day + 1
                val isUpdated = dbHelper.updateChallengeDayCount(challenge.id, newDay, currentDate)

                if (isUpdated) {
                    challenge.day = newDay
                    challenge.lastUpdateDate = currentDate
                    holder.progressBar.progress = newDay

                    if (newDay == 22) {
                        Toast.makeText(holder.itemView.context, "Congratulations! level EASY complete!", Toast.LENGTH_SHORT).show()
                    }
                    if (newDay == 44) {
                        Toast.makeText(holder.itemView.context, "Congratulations! level MEDIUM complete!", Toast.LENGTH_SHORT).show()
                    }
                    if (newDay == 66) {
                        Toast.makeText(holder.itemView.context, "Congratulations! level HARD complete!", Toast.LENGTH_SHORT).show()
                    }

                    // Notify the adapter about the update
                    notifyItemChanged(position)
                    Log.d("ChallengeAdapter", "Challenge updated successfully for ID: ${challenge.id}")
                } else {
                    // Handle the error, e.g., show a Toast message
                    Log.e("ChallengeAdapter", "Failed to update challenge for ID: ${challenge.id}")
                }
            } else {
                Log.d("ChallengeAdapter", "No update needed for challenge ID: ${challenge.id} (Date is current)")
            }
        }
    }

    fun getRandomEncouragingMessage(): String {
        val messages = listOf(
            "Keep pushing, you're doing great!",
            "Each moment of effort counts.",
            "Stay focused, stay determined.",
            "Believe in yourself, you have what it takes.",
            "Your hard work is paying off.",
            "Stay positive, work hard, make it happen.",
            "Consistency is key. Keep it up!",
            "Every effort is a step closer to success.",
            "Breathe, focus, and conquer.",
            "You're building something great."
        )
        return messages[Random.nextInt(messages.size)]
    }

    private fun playSound(context: Context) {
        val mediaPlayer = MediaPlayer.create(context, R.raw.check_sound)
        mediaPlayer.setOnCompletionListener {
            it.release()
        }
        mediaPlayer.start()
    }

    fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    override fun getItemCount() = challenges.size

    fun updateData(newChallenges: List<Challenge>) {
        challenges = newChallenges
        notifyDataSetChanged()
    }
}

package com.example.moodup.database

import android.util.Log
import com.example.moodup.ui.chat.business.Chat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue

class ChatDatabase {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: ""
    }

    fun addChatsToDB(chatId: Int, message: String, type: String) {

        val userId = getCurrentUserId()
        val timestamp = Date()
        val chat = Chat(
            chatId = chatId,
            message = message,
            messageType = type,
            date = timestamp,
            sentAnScore = ""
        )

        db.collection("users").document(userId).collection("chats")
            .document(chat.chatId.toString())
            .set(chat)
            .addOnSuccessListener { documentReference ->
                //Log.d("TAG", "Chat message added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("TAG", "Error adding chat message", e)
            }
    }

    private fun addDailyScores(date: Date, dailyScore: Map<String, Double>) {
        val userId = getCurrentUserId()
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate = dateFormatter.format(date)

        db.collection("users").document(userId).collection("dailyScores")
            .document(formattedDate)
            .set(dailyScore)
            .addOnSuccessListener {
                Log.d("TAG", "Daily scores added successfully for: $formattedDate")
            }
            .addOnFailureListener { e ->
                Log.w("TAG", "Error adding daily scores", e)
            }
    }

    fun fetchChatMessages(onSuccess: (List<Chat>) -> Unit, onFailure: (Exception) -> Unit) {

        val userId = getCurrentUserId()

        db.collection("users").document(userId).collection("chats")
            .orderBy("chatId")
            .addSnapshotListener { snapshots, exception ->
                if (exception != null) {
                    onFailure(exception)
                    return@addSnapshotListener
                }

                val chatList = mutableListOf<Chat>()
                for (document in snapshots!!) {
                    val chat = document.toObject(Chat::class.java)
                    chatList.add(chat)
                }
                onSuccess(chatList)
            }
    }

    fun fetchLastWeeksDailyScores(
        onSuccess: (Map<String, Map<String, Double>>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = getCurrentUserId()
        val calendar = Calendar.getInstance()
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val displayDateFormatter = SimpleDateFormat("EEE", Locale.getDefault())

        val lastWeekDates = mutableListOf<String>()
        for (i in 0..6) {
            lastWeekDates.add(dateFormatter.format(calendar.time))
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        lastWeekDates.reverse()

        val lastWeekDateString = lastWeekDates[0]

        Log.d("ChatDatabase", "Fetching scores from: $lastWeekDateString")

        db.collection("users").document(userId).collection("dailyScores")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val scoresMap = mutableMapOf<String, Map<String, Double>>()
                for (document in querySnapshot) {
                    val date = document.id
                    if (date >= lastWeekDateString) {
                        val dailyScore =
                            document.data.mapValues { it.value as Double } // Ensure correct type casting
                        Log.d("ChatDatabase", "Fetched document: $date with data: $dailyScore")
                        scoresMap[date] = dailyScore
                    }
                }

                // Fill missing dates with empty data
                val completeScoresMap = mutableMapOf<String, Map<String, Double>>()
                for (date in lastWeekDates) {
                    if (scoresMap.containsKey(date)) {
                        completeScoresMap[date] = scoresMap[date]!!
                    } else {
                        completeScoresMap[date] =
                            mapOf("positivePercentage" to 0.0, "negativePercentage" to 0.0)
                    }
                }

                val sortedScoresMap = completeScoresMap.toSortedMap()
                Log.d("ChatDatabase", "Final scores map: $sortedScoresMap")

                // Transform keys to display format
                val displayScoresMap = sortedScoresMap.mapKeys { (key, _) ->
                    try {
                        displayDateFormatter.format(dateFormatter.parse(key)!!)
                    } catch (e: Exception) {
                        Log.e("ChatDatabase", "Error parsing date: $key", e)
                        key // Fallback to original key if parsing fails
                    }
                }
                Log.d("ChatDatabase", "Display scores map: $displayScoresMap")

                onSuccess(displayScoresMap)
            }
            .addOnFailureListener { e ->
                Log.e("ChatDatabase", "Error fetching scores", e)
                onFailure(e)
            }
    }


    fun calculateAllDailyScores(onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val userId = getCurrentUserId()

        db.collection("users").document(userId).collection("chats")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val dailyScoresMap = mutableMapOf<String, MutableList<Double>>()

                // fetching the scores for the messages in a specific day
                for (document in querySnapshot) {
                    val chat = document.toObject(Chat::class.java)
                    if (chat.messageType != "sender") continue

                    val date = chat.date
                    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val formattedDate = dateFormatter.format(date)

                    val score = chat.sentAnScore.toDoubleOrNull() ?: continue

                    if (dailyScoresMap.containsKey(formattedDate)) {
                        dailyScoresMap[formattedDate]?.add(score)
                    } else {
                        dailyScoresMap[formattedDate] = mutableListOf(score)
                    }
                }

                // calculation of the score percentage
                val finalDailyScoresMap = mutableMapOf<String, Map<String, Double>>()

                for ((date, scoresList) in dailyScoresMap) {
                    val positiveScores = scoresList.filter { it > 0 }
                    val negativeScores = scoresList.filter { it < 0 }

                    val positiveSum = positiveScores.sum()
                    val negativeSum = negativeScores.sumOf { it.absoluteValue }
                    val totalScores = positiveSum + negativeSum

                    val positivePercentage =
                        if (totalScores > 0) (positiveSum * 100 / totalScores) else 0.0
                    val negativePercentage =
                        if (totalScores > 0) (negativeSum * 100 / totalScores) else 0.0

                    val dailyScore = mapOf(
                        "positivePercentage" to positivePercentage,
                        "negativePercentage" to negativePercentage
                    )

                    finalDailyScoresMap[date] = dailyScore
                }

                // pushing scores to firestore
                for ((formattedDate, dailyScore) in finalDailyScoresMap) {
                    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val date = dateFormatter.parse(formattedDate)
                    addDailyScores(date, dailyScore)
                }

                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }
}
package com.example.moodup.ui.profile.analysis

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.moodup.database.ChatDatabase

class AnalysisViewModel : ViewModel() {

    private val chatDatabase = ChatDatabase()

    val onArrowButtonClicked = MutableLiveData(false)
    val onShowButtonClicked = MutableLiveData(true)
    val weeklyScores = MutableLiveData<Map<String, Map<String, Double>>>()

    fun onClickArrowButton() {
        onArrowButtonClicked.value = true
    }

    fun onShowButton() {
        onShowButtonClicked.value = true
    }

    fun fetchWeeklyScores() {
        chatDatabase.fetchLastWeeksDailyScores(
            onSuccess = { scores ->
                Log.d("AnalysisViewModel", "Fetched scores: $scores")
                weeklyScores.value = scores
            },
            onFailure = { e ->
                Log.e("AnalysisViewModel", "Error fetching scores", e)
            }
        )
    }
}
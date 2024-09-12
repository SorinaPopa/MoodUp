package com.example.moodup.ui.profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.moodup.database.ChatDatabase

class ProfileViewModel : ViewModel() {

    private val chatDatabase = ChatDatabase()

    val onInfoButtonClicked = MutableLiveData(false)
    val onAnalysisButtonClicked = MutableLiveData(false)
    val onTrainAIButtonClicked = MutableLiveData(false)
    val onLogoutButtonClicked = MutableLiveData(false)

    fun onClickInfoButton() {
        onInfoButtonClicked.value = true
    }

    fun onClickAnalysisButton() {
        onAnalysisButtonClicked.value = true
    }

    fun onClickTrainAIButton() {
        onTrainAIButtonClicked.value = true
    }

    fun onClickLogoutButton() {
        onLogoutButtonClicked.value = true
    }

    fun calculateAllDailyScores() {
        chatDatabase.calculateAllDailyScores(
            onSuccess = {
            },
            onFailure = { exception ->
            }
        )
    }
}
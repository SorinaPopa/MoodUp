package com.example.moodup.ui.register

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    val registerEmail = MutableLiveData("")
    val registerPassword = MutableLiveData("")
    val onLoginHereButtonClicked = MutableLiveData(false)
    val onRegisterButtonClicked = MutableLiveData(false)
    fun onClickLoginHereButton() {
        onLoginHereButtonClicked.value = true
    }

    fun onClickRegisterButton() {
        onRegisterButtonClicked.value = true
    }
}
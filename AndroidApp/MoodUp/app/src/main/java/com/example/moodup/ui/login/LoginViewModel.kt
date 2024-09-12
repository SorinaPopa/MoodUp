package com.example.moodup.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    val loginEmail = MutableLiveData("")
    val loginPassword = MutableLiveData("")
    val onRegisterHereButtonClicked = MutableLiveData(false)
    val onLoginButtonClicked = MutableLiveData(false)
    fun onClickRegisterHereButton() {
        onRegisterHereButtonClicked.value = true
    }

    fun onClickLoginButton() {
        onLoginButtonClicked.value = true
    }


}
package com.example.moodup.ui.profile.editprofile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EditProfileViewModel : ViewModel() {
    val onArrowButtonClicked = MutableLiveData(false)

    fun onClickArrowButton() {
        onArrowButtonClicked.value = true
    }
}
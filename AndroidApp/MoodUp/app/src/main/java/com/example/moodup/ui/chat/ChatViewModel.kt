package com.example.moodup.ui.chat

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.moodup.database.ChatDatabase
import com.example.moodup.repository.ChatRepository
import com.example.moodup.ui.chat.business.Chat

class ChatViewModel : ViewModel() {

    private val chatRepository = ChatRepository()
    private val chatDatabase = ChatDatabase()

    var onMicButtonClicked = MutableLiveData(false)
    var onSendButtonClicked = MutableLiveData(false)

    var chatList = MutableLiveData<List<Chat>>()
        private set

    val messageInput = MutableLiveData("")

    init {
        fetchChatMessages()
    }

    fun onClickMicButton() {
        onMicButtonClicked.value = true
    }

    fun onClickSendButton() {
        onSendButtonClicked.value = true
    }

    fun createChatCompletion(message: String, chatId: Int) {
        chatRepository.createChatCompletion(message, chatId)
    }

    fun addChatsToDB(chatId: Int, message: String, type: String) {
        chatDatabase.addChatsToDB(chatId, message, type)
    }

    private fun fetchChatMessages() {
        chatDatabase.fetchChatMessages(
            onSuccess = { chatList ->
                this.chatList.postValue(chatList)
            },
            onFailure = { exception ->
                Log.e("ChatViewModel", "Error fetching chat messages: ${exception.message}")
            }
        )
    }

}
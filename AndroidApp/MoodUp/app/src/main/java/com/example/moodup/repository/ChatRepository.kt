package com.example.moodup.repository

import android.util.Log
import com.example.moodup.database.ChatDatabase
import com.example.moodup.network.ApiClient
import com.example.moodup.response.ChatRequest
import com.example.moodup.response.ChatResponse
import com.example.moodup.response.Message
import com.example.moodup.utils.CHAT_GPT_MODEL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class ChatRepository {

    private val apiClient = ApiClient.getInstance()
    private val chatDatabase = ChatDatabase()
    fun createChatCompletion(message: String, chatId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val chatRequest = ChatRequest(
                    arrayListOf(
                        Message(
                            message,
                            "user"
                        )
                    ),
                    CHAT_GPT_MODEL
                )
                apiClient.createChatCompletion(chatRequest)
                    .enqueue(object : Callback<ChatResponse> {
                        override fun onResponse(
                            call: Call<ChatResponse>,
                            response: Response<ChatResponse>
                        ) {
                            val code = response.code()
                            if (code == 200) {
                                response.body()?.choices?.get(0)?.message?.let { chatResponse ->
                                    Log.d("message", chatResponse.toString())
                                    chatDatabase.addChatsToDB(
                                        chatId,
                                        chatResponse.content,
                                        "receiver"
                                    )
                                }
                            } else {
                                Log.d("error", response.errorBody().toString())
                                Log.d("error", response.errorBody()?.string() ?: "Unknown error")
                            }
                        }

                        override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                            t.printStackTrace()
                            Log.e("API Error", "Failed to make API call: ${t.message}", t)
                        }
                    }
                    )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
package com.example.moodup.response

data class ChatRequest(
    val messages: List<Message>,
    val model: String
)
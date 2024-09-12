package com.example.moodup.ui.chat.business

import java.util.Date

data class Chat(
    var chatId: Int = 0,
    var message: String = "",
    var messageType: String = "",
    var date: Date = Date(),
    var sentAnScore: String = ""
)
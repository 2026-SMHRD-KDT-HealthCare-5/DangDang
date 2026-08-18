package com.dangdang.data.model.chat

import androidx.annotation.Keep

@Keep
data class ChatResponse (
    val aiChatNo: Int,
    val aiMessage: String,
    val chatType: String
)
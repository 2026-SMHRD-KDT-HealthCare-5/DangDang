package com.dangdang.data.model.chat

import androidx.annotation.Keep

@Keep
data class ChatHistory (
    val aiChatNo: Int,
    val userMessage: String?,
    val aiMessage: String,
    val chatType: String,
    val chattedAt: String,
    val cardData: FoodConfirmResponse?
)
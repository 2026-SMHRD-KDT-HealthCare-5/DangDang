package com.dangdang.data.model.chat

import androidx.annotation.Keep

@Keep
data class ChatHistoryResponse (
    val date: String,
    val messages: List<ChatHistory>
)
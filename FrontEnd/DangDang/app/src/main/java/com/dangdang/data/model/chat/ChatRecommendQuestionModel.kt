package com.dangdang.data.model.chat

import androidx.annotation.Keep

@Keep
data class ChatRecommendQuestionModel (
    val question: String,
    val type: String
)
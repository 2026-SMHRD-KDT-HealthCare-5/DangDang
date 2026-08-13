package com.dangdang.data.model.chat

import androidx.annotation.Keep
import com.dangdang.data.enums.ChatUserType
import java.time.LocalDateTime

@Keep
data class ChatModel(
    val chatUserType: ChatUserType,
    val message: String,
    val date: LocalDateTime,
    val chatType: String,
    val chatStageType: String,
    val isChatAble: Boolean,
    val isInputComplete: Boolean,
    val analysisFoodInfo: AnalysisFoodModel?,
    val recommendWalkInfo: AIRecommendWalkModel?,
    val glucoseFeedbackInfo: GlucoseFeedbackModel?
)

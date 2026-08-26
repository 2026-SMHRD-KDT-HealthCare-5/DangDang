package com.dangdang.data.model.chat

import androidx.annotation.Keep

@Keep
data class FoodConfirmResponse(
    val logNo: Int,
    val missionNo: Int,
    val predictedGlucoseRise: Float,
    val targetDistance: Float,
    val targetKcal: Float,
    val targetTimeMinutes: Int,
    val chatbotMessage: String
)

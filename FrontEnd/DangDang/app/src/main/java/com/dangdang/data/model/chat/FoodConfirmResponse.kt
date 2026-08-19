package com.dangdang.data.model.chat

import androidx.annotation.Keep

@Keep
data class FoodConfirmResponse(
    val logNo: Int,
    val missionNo: Int,
    val predictedGlucoseRise: Double,
    val targetDistance: Double,
    val targetKcal: Double,
    val chatbotMessage: String
)

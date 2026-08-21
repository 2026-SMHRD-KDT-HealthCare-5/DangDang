package com.dangdang.data.model.chat

import androidx.annotation.Keep

@Keep
data class FoodAnalysisResponse (
    val matched: Boolean,
    val foodNo: Int,
    val foodName: String,
    val nutrition: AnalysisNutritionResponse,
    val predictedGlucoseRise: Double,
    val source: String,
    val chatbotMessage: String,
    val serving_size: Int,
    val servingSize: Int
)
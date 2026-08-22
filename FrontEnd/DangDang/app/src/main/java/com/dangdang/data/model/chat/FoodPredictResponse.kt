package com.dangdang.data.model.chat

import androidx.annotation.Keep

@Keep
data class FoodPredictResponse(
    val predictedGlucoseRise: Float,
    val predictedPeak: Float,
    val targetDistance: Float,
    val targetKcal: Float,
    val nutritionUsed: AnalysisNutritionResponse
)

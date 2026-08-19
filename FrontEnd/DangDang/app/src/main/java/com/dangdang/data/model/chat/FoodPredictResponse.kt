package com.dangdang.data.model.chat

import androidx.annotation.Keep

@Keep
data class FoodPredictResponse(
    val predictedGlucoseRise: Double,
    val predictedPeak: Double,
    val targetDistance: Double,
    val targetKcal: Double,
    val nutritionUsed: AnalysisNutritionResponse
)

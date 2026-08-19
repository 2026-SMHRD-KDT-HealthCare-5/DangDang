package com.dangdang.data.model.chat

import androidx.annotation.Keep

@Keep
data class AnalysisNutritionResponse (
    val carb: Double,
    val sugar: Double,
    val protein: Double,
    val fat: Double,
    val fiber: Double,
    val calorie: Double
)
package com.dangdang.data.model.chat

import androidx.annotation.Keep

@Keep
data class AnalysisNutritionResponse (
    val carb: Float,
    val sugar: Float,
    val protein: Float,
    val fat: Float,
    val fiber: Float,
    val calorie: Float
)
package com.dangdang.data.model.chat

import androidx.annotation.Keep

@Keep
data class FoodNutritionModel(
    val name: String,
    val unit: String,
    val value: Float
)

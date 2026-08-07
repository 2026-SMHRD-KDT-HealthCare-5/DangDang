package com.dangdang.data.model.chat

import androidx.annotation.Keep

@Keep
data class FoodInfoModel (
    val name: String,
    val nutritionInfo: String,
    val nutritionList: List<FoodNutritionModel>
)
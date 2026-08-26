package com.dangdang.data.model.chat

import androidx.annotation.Keep

@Keep
data class AnalysisFoodModel (
    val predictedGlucoseRise: Float, //예상 혈당 상승량
    val beginGlucose: Float, //식전 혈당
    val foodInfo: FoodInfoModel,
)
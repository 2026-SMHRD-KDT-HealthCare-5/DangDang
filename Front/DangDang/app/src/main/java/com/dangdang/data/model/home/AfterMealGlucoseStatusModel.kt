package com.dangdang.data.model.home

import androidx.annotation.Keep

@Keep
data class AfterMealGlucoseStatusModel (
    var goal: Float,
    val afterMealGlucoseStatus: List<Float>
)
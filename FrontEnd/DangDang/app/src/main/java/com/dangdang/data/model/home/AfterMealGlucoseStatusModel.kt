package com.dangdang.data.model.home

import androidx.annotation.Keep

@Keep
data class AfterMealGlucoseStatusModel (
    var targetGlucose: Float,
    val points: List<GlucoseChartPointModel>
)
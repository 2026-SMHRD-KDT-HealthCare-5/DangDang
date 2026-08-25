package com.dangdang.data.model.home

import androidx.annotation.Keep

@Keep
data class HomeWalkingDistanceModel(
    val todayDistance: Float,
    val monthlyDistance: Float,
    val totalDistance: Float
)

package com.dangdang.data.model.walk

import androidx.annotation.Keep

@Keep
data class WalkMissionTrackingInputForm(
    val latitude: Double,
    val longitude: Double,
    val currentDistance: Double
)

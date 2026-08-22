package com.dangdang.data.model.walk

import androidx.annotation.Keep

@Keep
data class WalkMissionTrackingInputForm(
    val latitude: Float,
    val longitude: Float,
    val currentDistance: Float
)

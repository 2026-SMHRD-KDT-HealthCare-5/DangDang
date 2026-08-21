package com.dangdang.data.model.walk

import androidx.annotation.Keep

@Keep
data class WalkMissionEndResponse(
    val status: String,
    val actualDistance: Double,
    val durationMinutes: Int,
    val burnedKcal: Double,
    val postGlucosePrompted: Boolean
)

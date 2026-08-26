package com.dangdang.data.model.walk

import androidx.annotation.Keep

@Keep
data class WalkMissionEndResponse(
    val status: String,
    val actualDistance: Float,
    val durationMinutes: Int,
    val burnedKcal: Float,
    val postGlucosePrompted: Boolean
)

package com.dangdang.data.model.walk

import androidx.annotation.Keep

@Keep
data class WalkMissionTrackingResponse(
    val goalReached: Boolean,
    val anomalyDetected: Boolean
)

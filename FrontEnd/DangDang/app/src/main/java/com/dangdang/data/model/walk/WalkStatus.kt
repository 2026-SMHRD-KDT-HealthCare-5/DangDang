package com.dangdang.data.model.walk

import androidx.annotation.Keep

@Keep
data class WalkStatus (
    var walkTargetDistance: Float,
    var currentWalkDistance: Float,
    var currentWalkCount: Int,
    var currentWalkKcal: Int
)
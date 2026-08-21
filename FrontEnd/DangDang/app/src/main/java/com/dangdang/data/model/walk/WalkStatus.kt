package com.dangdang.data.model.walk

import androidx.annotation.Keep

@Keep
data class WalkStatus (
    val missionNo: Int,
    val status: String,
    var targetDistance: Float,
    var actualDistance: Float,
    var currentWalkCount: Int,
    var currentWalkKcal: Int,
    val startTime: String,
    val lastTrackedAt: String,
    val createdAt: String
)
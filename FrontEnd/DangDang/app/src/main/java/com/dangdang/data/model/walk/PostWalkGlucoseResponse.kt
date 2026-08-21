package com.dangdang.data.model.walk

import androidx.annotation.Keep

@Keep
data class PostWalkGlucoseResponse (
    val missionNo: Int,
    val preGlucose: Int,
    val postGlucoseEst: Int,
    val postWalkGlucose: Int,
    val targetDistance: Double,
    val actualDistance: Double,
    val goalAchieved: Boolean,
    val feedbackMessage: String
)
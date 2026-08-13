package com.dangdang.data.model.chat

import androidx.annotation.Keep

@Keep
data class AIRecommendWalkModel (
    val targetDistance: Float,
    val minute: Int
)
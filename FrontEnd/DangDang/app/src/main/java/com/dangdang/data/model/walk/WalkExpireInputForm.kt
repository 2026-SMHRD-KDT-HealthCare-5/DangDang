package com.dangdang.data.model.walk

import androidx.annotation.Keep

@Keep
data class WalkExpireInputForm (
    val expireReason: String,
    val actualDistance: Float
)
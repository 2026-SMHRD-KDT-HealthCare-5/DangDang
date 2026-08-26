package com.dangdang.data.model.walk

import androidx.annotation.Keep

@Keep
data class WalkExpireResponse (
    val status: String,
    val expireReason: String,
    val noticeMessage: String
)
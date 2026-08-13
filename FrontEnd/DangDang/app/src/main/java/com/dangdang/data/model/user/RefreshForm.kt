package com.dangdang.data.model.user

import androidx.annotation.Keep

@Keep
data class RefreshForm (
    val refreshToken: String
)
package com.dangdang.data.model.user

import androidx.annotation.Keep

@Keep
data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
)

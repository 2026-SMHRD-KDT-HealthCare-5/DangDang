package com.dangdang.data.model.user

import androidx.annotation.Keep

@Keep
data class LoginForm (
    val email: String,
    val password: String
)
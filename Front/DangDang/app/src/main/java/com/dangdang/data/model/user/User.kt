package com.dangdang.data.model.user

import androidx.annotation.Keep

@Keep
data class User(
    val id: String,
    val isSignUp: Boolean,
    val nickname: String,
    val profileImageUrl: String,
    val email: String,
    val createdDt: String,
    val sinceDays: Int,
    val updatedDt: String
)

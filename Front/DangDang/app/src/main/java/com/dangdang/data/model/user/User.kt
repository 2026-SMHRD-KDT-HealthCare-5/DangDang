package com.dangdang.data.model.user

import androidx.annotation.Keep

@Keep
data class User(
    val id: String,
    val email: String,
    val createdDt: String,
    val updatedDt: String
)

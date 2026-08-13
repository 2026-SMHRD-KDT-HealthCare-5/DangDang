package com.dangdang.data.model

import androidx.annotation.Keep

@Keep
data class ResponseErrorModel (
    val code: String,
    val message: String
)
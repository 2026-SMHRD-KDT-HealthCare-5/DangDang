package com.dangdang.data.model.home

import androidx.annotation.Keep

@Keep
data class WeeklyGlucoseCheckModel(
    val day: String,
    var status: String
)

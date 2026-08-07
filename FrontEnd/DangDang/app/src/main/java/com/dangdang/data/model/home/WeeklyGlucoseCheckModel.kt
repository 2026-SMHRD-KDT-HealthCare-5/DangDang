package com.dangdang.data.model.home

import androidx.annotation.Keep

@Keep
data class WeeklyGlucoseCheckModel(
    val dayOfWeek: String,
    var isGlucoseManagement: Boolean
)

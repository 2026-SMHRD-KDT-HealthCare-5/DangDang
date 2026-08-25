package com.dangdang.data.model.home

import androidx.annotation.Keep

@Keep
data class GlucoseChartPointModel(
    val time: String,
    val glucose: Int
)

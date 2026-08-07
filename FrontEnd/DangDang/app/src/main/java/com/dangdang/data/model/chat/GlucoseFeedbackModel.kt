package com.dangdang.data.model.chat

import androidx.annotation.Keep

@Keep
data class GlucoseFeedbackModel(
    val beginGlucose: Int,
    val aiPredictAfterGlucose: Int,
    val realAfterGlucose: Int,
    val decreaseGlucose: Int
)
package com.dangdang.data.model.chat

import androidx.annotation.Keep
import com.dangdang.data.model.walk.WalkStatus

@Keep
data class GlucoseFeedbackTemplateModel(
    val title: String,
    val value: (glucoseFeedbackModel: GlucoseFeedbackModel) -> Any,
)
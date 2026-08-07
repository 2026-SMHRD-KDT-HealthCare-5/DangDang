package com.dangdang.common.utils

import com.dangdang.data.model.chat.GlucoseFeedbackTemplateModel

val GlucoseFeedbackTemplates = listOf(
    GlucoseFeedbackTemplateModel(
        title = "식전 혈당",
        value = {
            it.beginGlucose
        },
    ),
    GlucoseFeedbackTemplateModel(
        title = "AI 예상 식후 혈당",
        value = {
            it.aiPredictAfterGlucose
        },
    ),
    GlucoseFeedbackTemplateModel(
        title = "실제 식후 혈당",
        value = {
            it.realAfterGlucose
        }
    ),
    GlucoseFeedbackTemplateModel(
        title = "감소한 혈당량",
        value = {
            it.decreaseGlucose
        }
    ),
)
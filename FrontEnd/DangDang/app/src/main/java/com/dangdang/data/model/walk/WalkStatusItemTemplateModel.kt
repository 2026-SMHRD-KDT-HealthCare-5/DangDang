package com.dangdang.data.model.walk

import androidx.annotation.Keep

@Keep
data class WalkStatusItemTemplateModel(
    val title: String,
    val value: (walkStatus: WalkStatus, stepTime: Int) -> Any,
    val unit: String?
)

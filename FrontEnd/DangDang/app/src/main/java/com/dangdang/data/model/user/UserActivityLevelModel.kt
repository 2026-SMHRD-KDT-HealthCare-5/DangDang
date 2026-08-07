package com.dangdang.data.model.user

import androidx.annotation.Keep

@Keep
data class UserActivityLevelModel(
    val titleIconResourceId: Int,
    val title: String,
    val description: String
)

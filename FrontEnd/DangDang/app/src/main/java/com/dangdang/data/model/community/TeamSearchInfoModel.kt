package com.dangdang.data.model.community

import androidx.annotation.Keep

@Keep
data class TeamSearchInfoModel(
    val teamNo: Long,
    val profileImageUrl: String,
    val teamName: String,
    val memberCount: Int,
    val capacity: Int,
    val currentDistance: Float,
    val targetDistance: Float,
    val teamIntro: String,
)

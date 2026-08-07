package com.dangdang.data.model.community

import androidx.annotation.Keep

@Keep
data class TeamSearchInfoModel(
    val id: Long,
    val profileImageUrl: String,
    val name: String,
    val currentMemberCount: Int,
    val maxMemberCount: Int,
    val currentDistance: Float,
    val targetDistance: Float,
    val introduction: String,
)

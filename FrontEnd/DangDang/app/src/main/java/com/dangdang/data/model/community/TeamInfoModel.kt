package com.dangdang.data.model.community

import androidx.annotation.Keep

@Keep
data class TeamInfoModel(
    val teamNo: Int,
    var isCreator: Boolean,
    val memberCount: Int,
    val capacity: Int,
    val profileImageUrl: String,
    val teamIntro: String,
    var teamName: String,
    var targetDistance: Float,
    var currentDistance: Float,
    val members: List<TeamMemberChallengeStatusModel>
)
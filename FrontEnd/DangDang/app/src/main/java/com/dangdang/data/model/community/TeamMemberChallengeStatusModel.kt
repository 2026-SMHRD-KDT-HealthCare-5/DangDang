package com.dangdang.data.model.community

import androidx.annotation.Keep

@Keep
data class TeamMemberChallengeStatusModel(
    var rank: Int,
    var profileImageUrl: String,
    var nickname: String,
    var currentDistance: Float,
    var targetDistance: Float
)

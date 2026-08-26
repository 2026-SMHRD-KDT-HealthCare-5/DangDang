package com.dangdang.data.model.community

import androidx.annotation.Keep

@Keep
data class TeamMemberChallengeStatusModel(
    var nickname: String,
    var totalDistance: Float,
)

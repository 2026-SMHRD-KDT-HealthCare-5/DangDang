package com.dangdang.data.model.community

import androidx.annotation.Keep

@Keep
data class TeamRankingStatusModel (
    var rank: Int,
    var profileImageUrl: String,
    var teamName: String,
    var monthlyDistance: Float,
)
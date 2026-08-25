package com.dangdang.data.model.community

import androidx.annotation.Keep

@Keep
data class TeamRankingStatusResponse (
    val rankings: List<TeamRankingStatusModel>
)
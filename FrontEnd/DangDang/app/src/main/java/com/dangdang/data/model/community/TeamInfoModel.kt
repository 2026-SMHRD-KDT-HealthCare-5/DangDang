package com.dangdang.data.model.community

import androidx.annotation.Keep

@Keep
data class TeamInfoModel(
    var isLeader: Boolean,
    var name: String,
    var targetDistance: Float,
    var currentDistance: Float,
    var currentTeamDistance: Float
)
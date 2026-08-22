package com.dangdang.data.model.community

import android.net.Uri
import androidx.annotation.Keep

@Keep
data class TeamMakeForm(
    var uri: Uri?,
    var teamName: String,
    var teamIntro: String,
    var targetDistance: String
)

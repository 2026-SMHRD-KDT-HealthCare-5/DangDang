package com.dangdang.data.model.community

import android.net.Uri
import androidx.annotation.Keep

@Keep
data class TeamMakeForm(
    var uri: Uri?,
    var name: String,
    var introduction: String,
    var targetDistance: String
)

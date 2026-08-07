package com.dangdang.data.enums

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class AvatarSize(val backgroundSize: Dp, val avatarSize: Dp) {
    Large(
        backgroundSize = 72.dp,
        avatarSize = 48.dp
    ),
    Small(
        backgroundSize = 48.dp,
        avatarSize = 24.dp
    ),
    XSmall(
        backgroundSize = 24.dp,
        avatarSize = 12.dp
    )
}
package com.dangdang.data.enums

import androidx.compose.ui.graphics.Color
import com.dangdang.R
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.Gray
import com.dangdang.ui.theme.Navy
import com.dangdang.ui.theme.White

enum class ChatUserType(
    val speechBubbleBackground: Color,
    val speechBubbleTextColor: Color,
    val isSpeechBubbleBorder: Boolean,
    val tailDirection: Direction,
    val tailImageId: Int
) {
    AI(
        speechBubbleBackground = White,
        speechBubbleTextColor = Black,
        isSpeechBubbleBorder = true,
        tailDirection = Direction.Left,
        tailImageId = R.drawable.gray_tail
    ),
    User(
        speechBubbleBackground = Navy,
        speechBubbleTextColor = White,
        isSpeechBubbleBorder = false,
        tailDirection = Direction.Right,
        tailImageId = R.drawable.navy_tail
    )
}
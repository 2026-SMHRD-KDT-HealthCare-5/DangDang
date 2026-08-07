package com.dangdang.component.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.common.utils.medium
import com.dangdang.common.utils.regular
import com.dangdang.data.enums.ChatUserType
import com.dangdang.data.enums.Direction
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.Gray

@Preview
@Composable
fun SpeechBubblePreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SpeechBubble(
            chatUserType = ChatUserType.AI,
            message = "안녕하세요"
        )

        SpeechBubble(
            chatUserType = ChatUserType.User,
            message = "반갑습니다"
        )
    }
}

@Composable
fun SpeechBubble(
    chatUserType: ChatUserType,
    message: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy((-5).dp)
    ) {
        if(chatUserType.tailDirection == Direction.Left){
            Image(
                painter = painterResource(chatUserType.tailImageId),
                contentDescription = null,
                modifier = Modifier
                    .size(15.dp)
            )
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .background(
                    color = chatUserType.speechBubbleBackground,
                    shape = RoundedCornerShape(12.dp)
                )
                .then(
                    if(chatUserType.isSpeechBubbleBorder){
                        Modifier.border(
                            width = 1.dp,
                            color = Gray,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }else{
                        Modifier
                    }
                )
                .padding(12.dp)
        ){
            Text(
                text = message,
                style = AppTypography.labelMedium.regular,
                color = chatUserType.speechBubbleTextColor,
            )
        }

        if(chatUserType.tailDirection == Direction.Right){
            Image(
                painter = painterResource(chatUserType.tailImageId),
                contentDescription = null,
                modifier = Modifier
                    .size(15.dp)
            )
        }
    }
}
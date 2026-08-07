package com.dangdang.component.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.common.utils.regular
import com.dangdang.data.enums.ChatUserType
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Gray
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Preview
@Composable
fun UserChatViewPreview() {
    UserChatView(
        message = "안녕하세요",
        sendTime = LocalDateTime.of(
            LocalDate.now(),
            LocalTime.of(8, 30)
        )
    )
}

@Composable
fun UserChatView(
    message: String,
    sendTime: LocalDateTime
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(210.dp)
        ){
            SpeechBubble(
                chatUserType = ChatUserType.User,
                message = message
            )
        }

        Row(
            modifier = Modifier
                .padding(15.dp)
        ) {
            Text(
                text = sendTime.format(
                    DateTimeFormatter
                        .ofPattern("yyyy.MM.dd\n" +
                                "a hh:mm", Locale.KOREAN)
                ),
                style = AppTypography.labelLarge.regular,
                color = Gray,
            )
        }
    }
}
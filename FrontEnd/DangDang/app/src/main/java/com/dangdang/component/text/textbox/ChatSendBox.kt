package com.dangdang.component.text.textbox

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.R
import com.dangdang.common.utils.componentWidthModifier
import com.dangdang.common.utils.regular
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.CapsuleRoundShape
import com.dangdang.ui.theme.DarkGray
import com.dangdang.ui.theme.Gray
import com.dangdang.ui.theme.ThinLineDp
import com.dangdang.ui.theme.White

@Preview
@Composable
fun ChatSendBoxPreview(){
    ChatSendBox(
        value = "",
        onValueChange = {},
        onSendClick = {}
    )
}

@Composable
fun ChatSendBox(
    enabled: Boolean = true,
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 10.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        BasicTextField(
            enabled = enabled,
            value = value,
            onValueChange = { newValue ->
                onValueChange(newValue)
            },
            textStyle = AppTypography.labelLarge.regular,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.None
            ),
            singleLine = false,
            modifier = Modifier
                .weight(1f),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .background(
                            color = if(enabled) White else Gray,
                            shape = CapsuleRoundShape
                        )
                        .border(
                            width = ThinLineDp,
                            color = Gray,
                            shape = CapsuleRoundShape
                        )
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty()) {
                            Text(
                                text = "메시지를 입력하세요...",
                                style = AppTypography.labelLarge.regular,
                                color = if(enabled) Gray else White
                            )
                        }
                        innerTextField()
                    }
                }
            }
        )

        Image(
            painter = painterResource(R.drawable.chat_send_icon),
            contentDescription = "채팅 전송 아이콘",
            modifier = Modifier
                .size(48.dp)
                .clickable(
                    enabled = enabled,
                    onClick = onSendClick
                )
        )
    }
}
package com.dangdang.component.button

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.R
import com.dangdang.common.utils.regular
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.White

@Preview
@Composable
fun ListButtonPreview(

){
    ListButton(
        leftIcon = {
            Icon(
                painter = painterResource(R.mipmap.kakao_login),
                contentDescription = "left icon",
                modifier = Modifier.size(24.dp)
            )
        },
        rightIcon = {
            Icon(
                painter = painterResource(R.mipmap.kakao_login),
                contentDescription = "left icon",
                modifier = Modifier.size(24.dp)
            )
        },
        title = "환경설정",
        onClick = {}
    )
}

@Composable
fun ListButton(
    leftIcon: @Composable () -> Unit = {},
    rightIcon: @Composable () -> Unit = {},
    title: String,
    onClick: () -> Unit
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = White
            )
            .clickable(
                onClick = onClick
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leftIcon()

        Text(
            text = title,
            style = AppTypography.bodyLarge.regular,
            color = Black,
            modifier = Modifier
                .weight(1f)
        )
        
        rightIcon()
    }
}
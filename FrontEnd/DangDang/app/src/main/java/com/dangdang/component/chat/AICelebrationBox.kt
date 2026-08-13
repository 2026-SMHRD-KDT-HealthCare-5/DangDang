package com.dangdang.component.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.R
import com.dangdang.common.utils.bold
import com.dangdang.common.utils.regular
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.EmeraldGreen
import com.dangdang.ui.theme.ForestGreen
import com.dangdang.ui.theme.LightGreen
import com.dangdang.ui.theme.LightScarlet
import com.dangdang.ui.theme.MagentaPink
import com.dangdang.ui.theme.MediumLineDp
import com.dangdang.ui.theme.MediumRoundShape
import com.dangdang.ui.theme.Scarlet

@Preview
@Composable
fun AICelebrationBoxPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AICelebrationBox(
            isSuccess = true
        )

        AICelebrationBox(
            isSuccess = false
        )
    }
}

@Composable
fun AICelebrationBox(
    isSuccess: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MediumRoundShape)
            .background(
                color = if(isSuccess) LightGreen else LightScarlet,
            )
            .border(
                width = MediumLineDp,
                color = if(isSuccess) EmeraldGreen else Scarlet,
                shape = MediumRoundShape
            )
    ){
        Column(
            modifier = Modifier
                .padding(
                    horizontal = 25.dp,
                    vertical = 15.dp
                ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = if(isSuccess){
                    "목표를 달성했어요! \uD83C\uDF89"
                } else {
                    "목표를 달성하지 못했어요...\uD83D\uDE2D"
                },
                style = AppTypography.bodyLarge.bold,
                color = if(isSuccess) ForestGreen else MagentaPink,
            )

            Text(
                text = if(isSuccess){
                    "오늘도 건강한 습관을 실천했네요!\n" +
                    "계속 함께 관리해봐요 \uD83D\uDC99"
                } else {
                    "하지만 오늘도 열심히 하려는 모습이\n" +
                    "정말 멋져요! 계속 함께 관리해봐요 \uD83D\uDC99"
                },
                style = AppTypography.labelMedium.regular,
                color = Black,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(
                    horizontal = if(isSuccess) 40.dp else 28.dp
                ),
            horizontalAlignment = Alignment.End
        ) {
            Spacer(
                Modifier.height(
                    if(isSuccess) 50.dp else 22.dp
                )
            )

            if(isSuccess){
                Image(
                    painter = painterResource(R.drawable.dangdang_login),
                    contentDescription = "당당이",
                    modifier = Modifier
                        .wrapContentSize(unbounded = true)
                        .width(80.dp)
                        .height(110.dp)
                )
            }else{
                Image(
                    painter = painterResource(R.drawable.dangdang_failed),
                    contentDescription = "당당이",
                    modifier = Modifier
                        .width(80.dp)
                        .height(98.dp)
                )
            }
        }
    }
}
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

@Preview
@Composable
fun AICelebrationBoxPreview() {
    AICelebrationBox(
        weeklyCompleteCount = 6
    )
}

@Composable
fun AICelebrationBox(
    weeklyCompleteCount: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                color = LightGreen,
            )
            .border(
                width = 2.dp,
                color = EmeraldGreen,
                shape = RoundedCornerShape(12.dp)
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
                text = "목표를 달성했어요! \uD83C\uDF89",
                style = AppTypography.bodyLarge.bold,
                color = ForestGreen,
            )

            Text(
                text = "오늘도 건강한 습관을 실천했네요.\n" +
                        "이번 주 ${weeklyCompleteCount}번째 성공이에요! \uD83D\uDC4D\n" +
                        "계속 함께 관리해봐요 \uD83D\uDC99",
                style = AppTypography.labelMedium.regular,
                color = Black,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(
                    horizontal = 40.dp
                ),
            horizontalAlignment = Alignment.End
        ) {
            Spacer(Modifier.height(50.dp))
            Image(
                painter = painterResource(R.mipmap.dangdang_login),
                contentDescription = "당당이",
                modifier = Modifier
                    .wrapContentSize(unbounded = true)
                    .width(80.dp)
                    .height(110.dp)
            )
        }
    }
}
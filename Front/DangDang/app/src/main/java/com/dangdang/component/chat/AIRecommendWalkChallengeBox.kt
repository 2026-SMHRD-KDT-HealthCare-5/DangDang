package com.dangdang.component.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.R
import com.dangdang.common.utils.medium
import com.dangdang.common.utils.regular
import com.dangdang.component.button.PrimaryButton
import com.dangdang.data.enums.LayoutSize
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.DarkGray
import com.dangdang.ui.theme.Gray
import com.dangdang.ui.theme.PrimaryBlue
import com.dangdang.ui.theme.White

@Preview
@Composable
fun AIRecommendWalkChallengeBoxPreview() {
    AIRecommendWalkChallengeBox(
        targetDistance = 2.6f,
        minute = 30,
        onChallengeClick = {}
    )
}

@Composable
fun AIRecommendWalkChallengeBox(
    targetDistance: Float,
    minute: Int,
    onChallengeClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = White,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = Gray,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(
                        vertical = 10.dp
                    )
            ){
                Image(
                    painter = painterResource(R.mipmap.walk_challenge_icon),
                    contentDescription = "walk challenge icon",
                    modifier = Modifier
                        .size(50.dp)
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "추천 걷기 챌린지",
                    style = AppTypography.bodyLarge.medium,
                    color = Black,
                )

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "$targetDistance km",
                        style = AppTypography.headlineMedium.medium,
                        color = PrimaryBlue,
                    )

                    Text(
                        text = "걷기",
                        style = AppTypography.titleLarge.medium,
                        color = Black,
                    )
                }

                Text(
                    text = "약 ${minute}분",
                    style = AppTypography.labelLarge.regular,
                    color = DarkGray,
                )
            }
        }

        PrimaryButton(
            text = "걷기 챌린지 시작하기",
            sizeType = LayoutSize.FillMaxSize,
            onClick = onChallengeClick
        )
    }
}
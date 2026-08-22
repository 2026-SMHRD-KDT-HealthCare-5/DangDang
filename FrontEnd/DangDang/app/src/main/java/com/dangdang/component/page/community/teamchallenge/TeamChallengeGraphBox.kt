package com.dangdang.component.page.community.teamchallenge

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.common.utils.bold
import com.dangdang.common.utils.regular
import com.dangdang.data.model.community.TeamInfoModel
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.White
import androidx.compose.ui.platform.LocalLocale
import com.dangdang.Application.Companion.ExamplePictureUrl
import com.dangdang.common.utils.medium
import com.dangdang.component.guage.GuageBar
import com.dangdang.data.enums.GuageBarSize
import com.dangdang.data.model.community.TeamMemberChallengeStatusModel
import com.dangdang.ui.theme.DarkGray
import com.dangdang.ui.theme.DarkGreen
import com.dangdang.ui.theme.Gray
import com.dangdang.ui.theme.LightSlateGray
import com.dangdang.ui.theme.MediumRoundShape
import com.dangdang.ui.theme.ThinLineDp

@Preview
@Composable
fun TeamChallengeGraphBoxPreview(){
    TeamChallengeGraphBox(
        teamInfo = TeamInfoModel(
            teamNo = 1,
            isCreator = false,
            memberCount = 4,
            capacity = 5,
            teamName = "우리팀 5월 걷기 챌린지",
            targetDistance = 150f,
            currentDistance = 30f,
            profileImageUrl = ExamplePictureUrl,
            teamIntro = "하루 7천보 이상 함께 걸어요!",
            members = listOf(
                TeamMemberChallengeStatusModel(
                    nickname = "닉네임",
                    totalDistance = 32.56f,
                ),
                TeamMemberChallengeStatusModel(
                    nickname = "닉네임2",
                    totalDistance = 20.56f,
                ),
                TeamMemberChallengeStatusModel(
                    nickname = "닉네임3",
                    totalDistance = 10.56f,
                ),
                TeamMemberChallengeStatusModel(
                    nickname = "닉네임4",
                    totalDistance = 5.56f,
                ),
                TeamMemberChallengeStatusModel(
                    nickname = "닉네임5",
                    totalDistance = 3.56f,
                )
            )
        )
    )
}

@Composable
fun TeamChallengeGraphBox(
    teamInfo: TeamInfoModel
){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = White,
                shape = MediumRoundShape
            )
            .border(
                width = ThinLineDp,
                color = Gray,
                shape = MediumRoundShape
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ){
        Text(
            text = "팀 목표 진행률",
            style = AppTypography.labelLarge.medium,
            color = Black,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = String.format(
                        LocalLocale.current.platformLocale,
                        "%.2f",
                        teamInfo.currentDistance) +
                        " km",
                    style = AppTypography.titleLarge.medium,
                    color = Black,
                )

                Text(
                    text = "/",
                    style = AppTypography.labelLarge.medium,
                    color = LightSlateGray,
                )

                Text(
                    text = "${teamInfo.targetDistance} km",
                    style = AppTypography.labelLarge.medium,
                    color = LightSlateGray,
                )
            }

            Text(
                text = "${((teamInfo.currentDistance / 
                        teamInfo.targetDistance)*100).toInt()}%",
                style = AppTypography.bodyLarge.bold,
                color = Black,
            )
        }

        GuageBar(
            size = GuageBarSize.Small,
            guageColor = DarkGreen,
            current = teamInfo.currentDistance,
            target = teamInfo.targetDistance
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "남은 거리",
                style = AppTypography.labelSmall.regular,
                color = DarkGray,
            )

            Text(
                text = String.format(
                    LocalLocale.current.platformLocale,
                    "%.2f",
                    teamInfo.targetDistance -
                            teamInfo.currentDistance
                ),
                style = AppTypography.labelSmall.medium,
                color = DarkGray,
            )

            Text(
                text = "km",
                style = AppTypography.labelSmall.regular,
                color = DarkGray,
            )
        }
    }
}
package com.dangdang.component.page.community.teamchallenge

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.dangdang.ui.theme.Navy
import com.dangdang.ui.theme.White
import java.util.Locale
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import com.dangdang.Application.Companion.ExamplePictureUrl
import com.dangdang.R
import com.dangdang.common.utils.medium
import com.dangdang.common.utils.screen
import com.dangdang.component.guage.GuageBar
import com.dangdang.component.guage.ThreeGuageBar
import com.dangdang.data.enums.GuageBarSize
import com.dangdang.ui.theme.DarkGray
import com.dangdang.ui.theme.DarkGreen
import com.dangdang.ui.theme.Gray
import com.dangdang.ui.theme.LightSlateGray
import com.dangdang.ui.theme.MediumRoundShape
import com.dangdang.ui.theme.ThinLineDp
import kotlin.Int

@Preview
@Composable
fun TeamChallengeGraphBoxPreview(){
    TeamChallengeGraphBox(
        teamInfo = TeamInfoModel(
            isLeader = false,
            currentMemberCount = 4,
            maxMemberCount = 5,
            name = "우리팀 5월 걷기 챌린지",
            targetDistance = 150f,
            currentDistance = 20f,
            currentTeamDistance = 30f,
            profileImageUrl = ExamplePictureUrl,
            introduction = "하루 7천보 이상 함께 걸어요!"
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
                        teamInfo.currentTeamDistance) +
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
                text = "${((teamInfo.currentTeamDistance / 
                        teamInfo.targetDistance)*100).toInt()}%",
                style = AppTypography.bodyLarge.bold,
                color = Black,
            )
        }

        GuageBar(
            size = GuageBarSize.Small,
            guageColor = DarkGreen,
            current = teamInfo.currentTeamDistance,
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
                            teamInfo.currentTeamDistance
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
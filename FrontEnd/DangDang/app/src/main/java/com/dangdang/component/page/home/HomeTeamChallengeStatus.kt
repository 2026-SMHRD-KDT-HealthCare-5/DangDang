package com.dangdang.component.page.home

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.Application.Companion.ExamplePictureUrl
import com.dangdang.common.utils.bold
import com.dangdang.common.utils.medium
import com.dangdang.common.utils.regular
import com.dangdang.component.button.MoreButton
import com.dangdang.component.guage.GuageBar
import com.dangdang.data.enums.GuageBarSize
import com.dangdang.data.model.community.TeamInfoModel
import com.dangdang.data.model.community.TeamMemberChallengeStatusModel
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.Gray
import com.dangdang.ui.theme.MediumRoundShape
import com.dangdang.ui.theme.Navy
import com.dangdang.ui.theme.ThinLineDp
import com.dangdang.ui.theme.White

@Preview
@Composable
fun HomeTeamChallengeStatusPreview(){
    HomeTeamChallengeStatus(
        teamInfo = TeamInfoModel(
            teamNo = 1,
            isCreator = false,
            teamName = "우리팀 5월 걷기 챌린지",
            memberCount = 4,
            capacity = 5,
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
        ),
        onMoreClick = {}
    )
}

@Composable
fun HomeTeamChallengeStatus(
    teamInfo: TeamInfoModel,
    onMoreClick: () -> Unit
) {
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
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "팀 챌린지",
                    style = AppTypography.bodyLarge.medium,
                    color = Black,
                )

                Text(
                    text = "목표 " +
                            "${String.format(
                                LocalLocale.current.platformLocale, 
                                "%.2f", 
                                teamInfo.targetDistance)} km",
                    style = AppTypography.labelMedium.regular,
                    color = Gray,
                )
            }

            MoreButton(
                onClick = onMoreClick
            )
        }

        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = String.format(
                    LocalLocale.current.platformLocale,
                    "%.2f",
                    teamInfo.currentDistance
                ),
                style = AppTypography.bodyLarge.bold,
                color = Navy,
            )

            Text(
                text = "/",
                style = AppTypography.labelMedium.regular,
                color = Gray,
            )

            Text(
                text = "${String.format(
                            LocalLocale.current.platformLocale,
                            "%.2f",
                            teamInfo.targetDistance)} km",
                style = AppTypography.labelMedium.regular,
                color = Gray,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
            ){
                GuageBar(
                    size = GuageBarSize.Small,
                    guageColor = Navy,
                    current = teamInfo.currentDistance,
                    target = teamInfo.targetDistance
                )
            }

            Text(
                text = "${((teamInfo.currentDistance/teamInfo.targetDistance)*100).toInt()}%",
                style = AppTypography.bodyLarge.bold,
                color = Black,
            )
        }

        Spacer(Modifier.height(4.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            teamInfo.members.forEach { teamMemberChallengeStatus ->
                ChallengeBar(
                    targetDistance = teamInfo.targetDistance,
                    teamMemberChallengeStatusModel = teamMemberChallengeStatus
                )
            }
        }
    }
}
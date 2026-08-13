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
import androidx.compose.foundation.shape.RoundedCornerShape
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
            isLeader = false,
            name = "우리팀 5월 걷기 챌린지",
            currentMemberCount = 4,
            maxMemberCount = 5,
            targetDistance = 150f,
            currentDistance = 20f,
            currentTeamDistance = 30f,
            profileImageUrl = ExamplePictureUrl,
            introduction = "하루 7천보 이상 함께 걸어요!"
        ),
        teamMemberChallengeStatusList = listOf(
            TeamMemberChallengeStatusModel(
                rank = 1,
                profileImageUrl = ExamplePictureUrl,
                nickname = "닉네임",
                currentDistance = 32.56f,
                targetDistance = 150f
            ),
            TeamMemberChallengeStatusModel(
                rank = 2,
                profileImageUrl = ExamplePictureUrl,
                nickname = "닉네임2",
                currentDistance = 20.56f,
                targetDistance = 150f
            ),
            TeamMemberChallengeStatusModel(
                rank = 3,
                profileImageUrl = ExamplePictureUrl,
                nickname = "닉네임3",
                currentDistance = 10.56f,
                targetDistance = 150f
            ),
            TeamMemberChallengeStatusModel(
                rank = 4,
                profileImageUrl = ExamplePictureUrl,
                nickname = "닉네임4",
                currentDistance = 5.56f,
                targetDistance = 150f
            ),
            TeamMemberChallengeStatusModel(
                rank = 5,
                profileImageUrl = ExamplePictureUrl,
                nickname = "닉네임5",
                currentDistance = 3.56f,
                targetDistance = 150f
            )
        ),
        onMoreClick = {}
    )
}

@Composable
fun HomeTeamChallengeStatus(
    teamInfo: TeamInfoModel,
    teamMemberChallengeStatusList: List<TeamMemberChallengeStatusModel>,
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
                    teamInfo.currentTeamDistance
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
                text = "${((teamInfo.currentTeamDistance/teamInfo.targetDistance)*100).toInt()}%",
                style = AppTypography.bodyLarge.bold,
                color = Black,
            )
        }

        Spacer(Modifier.height(4.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            teamMemberChallengeStatusList.forEach { teamMemberChallengeStatus ->
                ChallengeBar(
                    teamMemberChallengeStatusModel = teamMemberChallengeStatus
                )
            }
        }
    }
}
package com.dangdang.component.page.community.teamchallenge

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.Application.Companion.ExamplePictureUrl
import com.dangdang.common.utils.GuageColorList
import com.dangdang.component.divider.Divider
import com.dangdang.data.enums.DividerPosition
import com.dangdang.data.model.community.TeamMemberChallengeStatusModel
import com.dangdang.ui.theme.Gray
import com.dangdang.ui.theme.MediumRoundShape
import com.dangdang.ui.theme.ThinLineDp
import com.dangdang.ui.theme.White

@Preview
@Composable
fun TeamMemberStatusBoxPreview(){
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TeamMemberStatusBox(
            isGraph = true,
            teamMemberChallengeStatusList = listOf(
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
            ),
            targetDistance = 60f
        )

        TeamMemberStatusBox(
            isGraph = false,
            teamMemberChallengeStatusList = listOf(
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
                )
            ),
            targetDistance = 60f
        )
    }
}

@Composable
fun TeamMemberStatusBox(
    targetDistance: Float,
    isGraph: Boolean,
    teamMemberChallengeStatusList: List<TeamMemberChallengeStatusModel>
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
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        teamMemberChallengeStatusList.forEachIndexed { index, teamMemberChallengeStatus ->
            TeamMemberStatusItem(
                isGraph = isGraph,
                guageColor = GuageColorList[index % GuageColorList.size],
                teamMemberChallengeStatus = teamMemberChallengeStatus,
                rank = index + 1,
                targetDistance = targetDistance
            )

            if(index < teamMemberChallengeStatusList.size - 1){
                Divider(position = DividerPosition.Horizontal)
            }
        }
    }
}
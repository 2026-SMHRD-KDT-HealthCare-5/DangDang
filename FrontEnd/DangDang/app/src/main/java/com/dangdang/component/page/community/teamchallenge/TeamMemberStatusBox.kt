package com.dangdang.component.page.community.teamchallenge

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.Application.Companion.ExamplePictureUrl
import com.dangdang.common.utils.GuageColorList
import com.dangdang.common.utils.WalkStatusDetailItemTemplates
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
            )
        )

        TeamMemberStatusBox(
            isGraph = false,
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
                )
            )
        )
    }
}

@Composable
fun TeamMemberStatusBox(
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
                guageColor = GuageColorList[index%GuageColorList.size],
                teamMemberChallengeStatus = teamMemberChallengeStatus
            )

            if(index < teamMemberChallengeStatusList.size - 1){
                Divider(position = DividerPosition.Horizontal)
            }
        }
    }
}
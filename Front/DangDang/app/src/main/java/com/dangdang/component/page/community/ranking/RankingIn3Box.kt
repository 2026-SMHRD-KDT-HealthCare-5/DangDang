package com.dangdang.component.page.community.ranking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.Application.Companion.ExamplePictureUrl
import com.dangdang.data.model.community.TeamMemberChallengeStatusModel
import com.dangdang.ui.theme.White

@Preview
@Composable
fun RankingIn3BoxPreview(){
    RankingIn3Box(
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
}

@Composable
fun RankingIn3Box(
    teamMemberChallengeStatusList: List<TeamMemberChallengeStatusModel>
) {
    //3위까지만 들어가있도록 쪼갠다.
    var mixedTeamMemberChallengeStatusList =
        teamMemberChallengeStatusList.slice(0..2)
    //2위, 1위, 3위 순서대로 나오게 섞는다.
    mixedTeamMemberChallengeStatusList = listOf(
        mixedTeamMemberChallengeStatusList[1],
        mixedTeamMemberChallengeStatusList[0],
        mixedTeamMemberChallengeStatusList[2]
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = White
            )
            .padding(horizontal = 42.dp)
    ) {
        mixedTeamMemberChallengeStatusList.forEachIndexed { index, item ->
            RankingIn3Item(
                teamMemberChallengeStatus = item
            )

            if(index < mixedTeamMemberChallengeStatusList.size - 1){
                Spacer(Modifier.weight(1f))
            }
        }
    }
}
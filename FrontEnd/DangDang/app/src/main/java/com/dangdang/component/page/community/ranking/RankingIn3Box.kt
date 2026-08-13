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
import com.dangdang.data.model.community.TeamRankingStatusModel
import com.dangdang.ui.theme.White

@Preview
@Composable
fun RankingIn3BoxPreview(){
    RankingIn3Box(
        teamRankingStatusList = listOf(
            TeamRankingStatusModel(
                rank = 1,
                profileImageUrl = ExamplePictureUrl,
                teamName = "닉네임",
                monthlyDistance = 32.56f,
            ),
            TeamRankingStatusModel(
                rank = 2,
                profileImageUrl = ExamplePictureUrl,
                teamName = "닉네임2",
                monthlyDistance = 20.56f,
            ),
            TeamRankingStatusModel(
                rank = 3,
                profileImageUrl = ExamplePictureUrl,
                teamName = "닉네임3",
                monthlyDistance = 10.56f,
            ),
            TeamRankingStatusModel(
                rank = 4,
                profileImageUrl = ExamplePictureUrl,
                teamName = "닉네임4",
                monthlyDistance = 5.56f,
            ),
            TeamRankingStatusModel(
                rank = 5,
                profileImageUrl = ExamplePictureUrl,
                teamName = "닉네임5",
                monthlyDistance = 3.56f,
            )
        )
    )
}

@Composable
fun RankingIn3Box(
    teamRankingStatusList: List<TeamRankingStatusModel>
) {
    //2위, 1위, 3위 순서대로 나오게 섞는다.
    val mixedTeamMemberChallengeStatusList =
        listOf(
            if(teamRankingStatusList.size >= 2){
                teamRankingStatusList[1]
            }else{
                null
            },
            teamRankingStatusList[0],
            if(teamRankingStatusList.size >= 3){
                teamRankingStatusList[2]
            }else{
                null
            }
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
            if(item != null){
                RankingIn3Item(
                    teamRankingStatus = item
                )
            }

            if(index < mixedTeamMemberChallengeStatusList.size - 1){
                Spacer(Modifier.weight(1f))
            }
        }
    }
}
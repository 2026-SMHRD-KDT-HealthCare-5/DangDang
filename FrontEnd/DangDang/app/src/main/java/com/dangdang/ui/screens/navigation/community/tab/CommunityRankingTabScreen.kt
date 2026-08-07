package com.dangdang.ui.screens.navigation.community.tab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.dangdang.Application.Companion.ExamplePictureUrl
import com.dangdang.common.utils.mainScreen
import com.dangdang.common.utils.regular
import com.dangdang.component.page.community.ranking.RankingIn3Box
import com.dangdang.component.page.community.teamchallenge.TeamMemberStatusBox
import com.dangdang.data.model.community.TeamMemberChallengeStatusModel
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.viewmodel.community.CommunityTeamChallengeViewModel

@Preview
@Composable
fun CommunityRankingTabScreenPreview(){
    CommunityRankingTabScreenContent(
        teamChallengeStatusList = listOf(
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
fun CommunityRankingTabScreen(
    communityTeamChallengeViewModel: CommunityTeamChallengeViewModel = hiltViewModel(),
) {
    val teamChallengeStatusList by
        communityTeamChallengeViewModel.teamChallengeStatusList.collectAsState()

    CommunityRankingTabScreenContent(
        teamChallengeStatusList = teamChallengeStatusList ?: emptyList()
    )
}

@Composable
fun CommunityRankingTabScreenContent(
    teamChallengeStatusList: List<TeamMemberChallengeStatusModel>,
){
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .mainScreen()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(30.dp)
    ) {
        Spacer(Modifier.height(0.dp))
        RankingIn3Box(
            teamMemberChallengeStatusList = teamChallengeStatusList
        )

        TeamMemberStatusBox(
            isGraph = false,
            teamMemberChallengeStatusList = teamChallengeStatusList
        )
        Spacer(Modifier.height(0.dp))
    }
}
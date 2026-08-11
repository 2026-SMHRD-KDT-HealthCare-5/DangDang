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
import com.dangdang.component.page.community.ranking.TeamRankingStatusBox
import com.dangdang.component.page.community.teamchallenge.TeamMemberStatusBox
import com.dangdang.data.model.community.TeamMemberChallengeStatusModel
import com.dangdang.data.model.community.TeamRankingStatusModel
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.viewmodel.community.CommunityTeamChallengeViewModel
import com.dangdang.ui.viewmodel.community.CommunityTeamRankingViewModel

@Preview
@Composable
fun CommunityRankingTabScreenPreview(){
    CommunityRankingTabScreenContent(
        teamRankingStatusList = listOf(
            TeamRankingStatusModel(
                rank = 1,
                profileImageUrl = ExamplePictureUrl,
                name = "팀명",
                currentDistance = 32.56f,
            ),
            TeamRankingStatusModel(
                rank = 2,
                profileImageUrl = ExamplePictureUrl,
                name = "팀명2",
                currentDistance = 20.56f,
            ),
            TeamRankingStatusModel(
                rank = 3,
                profileImageUrl = ExamplePictureUrl,
                name = "팀명3",
                currentDistance = 10.56f,
            ),
            TeamRankingStatusModel(
                rank = 4,
                profileImageUrl = ExamplePictureUrl,
                name = "팀명4",
                currentDistance = 5.56f,
            ),
            TeamRankingStatusModel(
                rank = 5,
                profileImageUrl = ExamplePictureUrl,
                name = "팀명5",
                currentDistance = 3.56f,
            )
        )
    )
}

@Composable
fun CommunityRankingTabScreen(
    communityTeamRankingViewModel: CommunityTeamRankingViewModel = hiltViewModel(),
) {
    val teamRankingStatusList by
        communityTeamRankingViewModel.teamRankingStatusList.collectAsState()

    CommunityRankingTabScreenContent(
        teamRankingStatusList = teamRankingStatusList
    )
}

@Composable
fun CommunityRankingTabScreenContent(
    teamRankingStatusList: List<TeamRankingStatusModel>,
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
            teamRankingStatusList = teamRankingStatusList
        )

        TeamRankingStatusBox(
            teamRankingStatusList = teamRankingStatusList
        )
        Spacer(Modifier.height(0.dp))
    }
}
package com.dangdang.ui.screens.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.dangdang.common.utils.mainScreen
import com.dangdang.data.enums.LoadingState
import com.dangdang.data.model.community.TeamInfoModel
import com.dangdang.ui.screens.navigation.community.CommunityTeamMainScreen
import com.dangdang.ui.screens.navigation.community.teamsearch.CommunityTeamSearchScreen
import com.dangdang.ui.viewmodel.community.CommunityViewModel

@Preview
@Composable
fun CommunityScreenPreview(

){
    CommunityScreenContent(
        teamInfo = null,
        onTeamMakeMove = {}
    )
}

@Composable
fun CommunityScreen(
    communityViewModel: CommunityViewModel = hiltViewModel(),
    onTeamMakeMove: () -> Unit
){
    val teamInfo by
        communityViewModel.teamInfo.collectAsState()

    if(teamInfo.loadingState == LoadingState.Success){
        CommunityScreenContent(
            communityViewModel = communityViewModel,
            teamInfo = teamInfo.data,
            onTeamMakeMove = onTeamMakeMove
        )
    }else{
        Box(
            modifier = Modifier
                .mainScreen()
        )
    }
}

@Composable
fun CommunityScreenContent(
    communityViewModel: CommunityViewModel? = null,
    teamInfo: TeamInfoModel?,
    onTeamMakeMove: () -> Unit
){
    Box(
        modifier = Modifier
            .mainScreen()
    ) {
        //팀 정보가 있을 경우 팀 정보 화면
        if(teamInfo != null) {
            CommunityTeamMainScreen(
                communityViewModel = communityViewModel,
            )
        }else{
            //팀 정보가 없을 경우 팀 검색 화면
            CommunityTeamSearchScreen(
                communityViewModel = communityViewModel,
                onTeamMakeMove = onTeamMakeMove
            )
        }
    }
}
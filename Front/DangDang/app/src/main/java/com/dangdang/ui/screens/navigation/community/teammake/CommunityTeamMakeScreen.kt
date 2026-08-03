package com.dangdang.ui.screens.navigation.community.teammake

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.dangdang.common.utils.mainScreen
import com.dangdang.component.navigation.topnavigation.TopNavigation

@Preview
@Composable
fun CommunityTeamMakeScreenPreview(){
    CommunityTeamMakeScreenContent(
        onBackClick = {}
    )
}

@Composable
fun CommunityTeamMakeScreen(
    navController: NavController,
){
    CommunityTeamMakeScreenContent(
        onBackClick = {
            navController.popBackStack()
        }
    )
}

@Composable
fun CommunityTeamMakeScreenContent(
    onBackClick: () -> Unit,
){
    Column(
        modifier = Modifier
            .mainScreen(),
    ) {
        TopNavigation(
            isBackButton = true,
            onBackClick = onBackClick,
            title = "팀 만들기",
        )
    }
}
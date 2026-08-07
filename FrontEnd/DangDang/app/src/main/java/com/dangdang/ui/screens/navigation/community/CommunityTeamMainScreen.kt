package com.dangdang.ui.screens.navigation.community

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.common.utils.mainScreen
import com.dangdang.common.utils.regular
import com.dangdang.component.button.outlined.PrimaryOutlinedButton
import com.dangdang.component.navigation.topnavigation.TopNavigation
import com.dangdang.data.enums.CommunityTab
import com.dangdang.data.enums.LayoutSize
import com.dangdang.ui.screens.navigation.community.tab.CommunityRankingTabScreen
import com.dangdang.ui.screens.navigation.community.tab.CommunityTeamChallengeTabScreen
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.DarkGray
import com.dangdang.ui.theme.GrayOpacity30
import com.dangdang.ui.theme.Navy
import com.dangdang.ui.theme.White
import com.dangdang.ui.viewmodel.community.CommunityViewModel

//커뮤니티 팀 챌린지,전체 랭킹 화면
@Preview
@Composable
fun CommunityTeamMainScreenPreview(){
    CommunityTeamMainScreenContent(

    )
}

@Composable
fun CommunityTeamMainScreen(
    communityViewModel: CommunityViewModel?,
){
    CommunityTeamMainScreenContent(
        communityViewModel = communityViewModel,
    )
}

@Composable
fun CommunityTeamMainScreenContent(
    communityViewModel: CommunityViewModel? = null,
){
    var tab by
        remember { mutableStateOf(CommunityTab.TeamChallenge) }

    Column(
        modifier = Modifier
            .mainScreen(),
    ) {
        TopNavigation(
            title = "커뮤니티",
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 20.dp,
                    vertical = 10.dp
                ),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CommunityTab.entries.forEach { tabItem ->
                val selected = tab == tabItem
                Box(
                    modifier = Modifier
                        .weight(1f)
                ){
                    PrimaryOutlinedButton(
                        text = tabItem.title,
                        isBorder = selected,
                        color = if(selected) Navy else DarkGray,
                        backgroundColor =
                            if(selected) White else GrayOpacity30,
                        selected = selected,
                        sizeType = LayoutSize.FillMaxSize,
                        onClick = {
                            if(!selected){
                                tab = tabItem
                            }
                        }
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ){
            if(tab == CommunityTab.TeamChallenge){
                CommunityTeamChallengeTabScreen(
                    communityViewModel = communityViewModel,
                )
            }else if(tab == CommunityTab.Ranking){
                CommunityRankingTabScreen()
            }
        }
    }
}
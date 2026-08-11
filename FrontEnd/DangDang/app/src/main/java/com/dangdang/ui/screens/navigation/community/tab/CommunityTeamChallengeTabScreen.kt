package com.dangdang.ui.screens.navigation.community.tab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.dangdang.Application.Companion.ExamplePictureUrl
import com.dangdang.R
import com.dangdang.common.utils.bold
import com.dangdang.common.utils.mainScreen
import com.dangdang.common.utils.medium
import com.dangdang.common.utils.regular
import com.dangdang.component.button.outlined.PrimaryOutlinedButton
import com.dangdang.component.page.community.teamchallenge.TeamChallengeGraphBox
import com.dangdang.component.page.community.teamchallenge.TeamChallengeInfoBox
import com.dangdang.component.page.community.teamchallenge.TeamMemberStatusBox
import com.dangdang.data.enums.LayoutSize
import com.dangdang.data.enums.LoadingState
import com.dangdang.data.model.community.TeamInfoModel
import com.dangdang.data.model.community.TeamMemberChallengeStatusModel
import com.dangdang.ui.screens.navigation.CommunityScreenContent
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.Red
import com.dangdang.ui.theme.White
import com.dangdang.ui.viewmodel.community.CommunityTeamChallengeViewModel
import com.dangdang.ui.viewmodel.community.CommunityViewModel

@Preview
@Composable
fun CommunityTeamChallengeTabScreenPreview(){
    CommunityTeamChallengeTabScreenContent(
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
        ),
        onOutTeam = {}
    )
}

@Composable
fun CommunityTeamChallengeTabScreen(
    communityTeamChallengeViewModel: CommunityTeamChallengeViewModel = hiltViewModel(),
    communityViewModel: CommunityViewModel?,
) {
    val teamInfo =
        communityViewModel?.teamInfo?.collectAsState()

    val teamChallengeStatusList by
        communityTeamChallengeViewModel.teamChallengeStatusList.collectAsState()

    if(teamInfo?.value?.loadingState == LoadingState.Success 
        && teamInfo.value.data != null){
        teamInfo.value.data?.let {
            CommunityTeamChallengeTabScreenContent(
                teamInfo = it,
                teamChallengeStatusList = teamChallengeStatusList ?: emptyList(),
                onOutTeam = {
                    communityViewModel.outTeam()
                }
            )
        }
    }else{
        Box(
            modifier = Modifier
                .mainScreen()
        )
    }
}

@Composable
fun CommunityTeamChallengeTabScreenContent(
    teamInfo: TeamInfoModel,
    teamChallengeStatusList: List<TeamMemberChallengeStatusModel>,
    onOutTeam: () -> Unit
){
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .mainScreen()
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Spacer(Modifier.height(20.dp))

            TeamChallengeInfoBox(
                teamInfo = teamInfo
            )

            Spacer(Modifier.height(15.dp))

            TeamChallengeGraphBox(
                teamInfo = teamInfo
            )
        }

        Text(
            text = "팀원 현황",
            style = AppTypography.labelLarge.medium,
            color = Black,
        )

        TeamMemberStatusBox(
            isGraph = true,
            teamMemberChallengeStatusList = teamChallengeStatusList
        )

        if(!teamInfo.isLeader){
            PrimaryOutlinedButton(
                text = "팀 나가기",
                color = Red,
                leftIcon = {
                    Icon(
                        painter = painterResource(R.drawable.logout),
                        contentDescription = "나가기",
                        modifier = Modifier
                            .size(24.dp),
                        tint = Red
                    )
                },
                sizeType = LayoutSize.FillMaxSize,
                onClick = onOutTeam
            )
        }
        Spacer(Modifier.height(12.dp))
    }
}
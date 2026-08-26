package com.dangdang.ui.screens.navigation.community.tab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.dangdang.Application.Companion.ExamplePictureUrl
import com.dangdang.R
import com.dangdang.common.utils.mainScreen
import com.dangdang.common.utils.medium
import com.dangdang.component.button.outlined.PrimaryOutlinedButton
import com.dangdang.component.errorview.ErrorView
import com.dangdang.component.page.community.teamchallenge.TeamChallengeGraphBox
import com.dangdang.component.page.community.teamchallenge.TeamChallengeInfoBox
import com.dangdang.component.page.community.teamchallenge.TeamMemberStatusBox
import com.dangdang.data.enums.LayoutSize
import com.dangdang.data.enums.LoadingState
import com.dangdang.data.model.community.TeamInfoModel
import com.dangdang.data.model.community.TeamMemberChallengeStatusModel
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.Red
import com.dangdang.ui.viewmodel.community.CommunityTeamChallengeViewModel
import com.dangdang.ui.viewmodel.community.CommunityViewModel

@Preview
@Composable
fun CommunityTeamChallengeTabScreenPreview(){
    CommunityTeamChallengeTabScreenContent(
        teamInfo = TeamInfoModel(
            teamNo = 1,
            isCreator = false,
            teamName = "우리팀 5월 걷기 챌린지",
            memberCount = 4,
            capacity = 5,
            targetDistance = 150f,
            currentDistance = 30f,
            profileImageUrl = ExamplePictureUrl,
            teamIntro = "하루 7천보 이상 함께 걸어요!",
            members = listOf(
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
    val context = LocalContext.current
    val teamInfo =
        communityViewModel?.teamInfo?.collectAsState()

    if(teamInfo?.value?.loadingState == LoadingState.Success 
        && teamInfo.value.data != null){
        teamInfo.value.data?.let {
            CommunityTeamChallengeTabScreenContent(
                teamInfo = it,
                onOutTeam = {
                    communityViewModel.outTeam(context, it.teamNo)
                }
            )
        }
    }else{
        ErrorView(
            loadingState = if(
                (teamInfo?.value?.loadingState?:LoadingState.Loading) ==
                    LoadingState.Error
            ){
                LoadingState.Error
            }else{
                LoadingState.Loading
            },
            message = "팀 랭킹 정보 불러오기를 실패했습니다."
        )
    }
}

@Composable
fun CommunityTeamChallengeTabScreenContent(
    teamInfo: TeamInfoModel,
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
            targetDistance = teamInfo.targetDistance,
            isGraph = true,
            teamMemberChallengeStatusList = teamInfo.members
        )

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
        Spacer(Modifier.height(12.dp))
    }
}
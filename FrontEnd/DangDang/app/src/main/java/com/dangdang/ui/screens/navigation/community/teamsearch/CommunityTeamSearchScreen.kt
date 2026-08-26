package com.dangdang.ui.screens.navigation.community.teamsearch

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.dangdang.component.button.PrimaryButton
import com.dangdang.component.navigation.topnavigation.TopNavigation
import com.dangdang.component.page.community.teamsearch.TeamSearchInfoBox
import com.dangdang.component.text.textfield.TextField
import com.dangdang.data.enums.LayoutSize
import com.dangdang.data.enums.LoadingState
import com.dangdang.data.model.PendingModel
import com.dangdang.data.model.community.TeamSearchInfoModel
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.viewmodel.community.CommunityTeamSearchViewModel
import com.dangdang.ui.viewmodel.community.CommunityViewModel

@Preview
@Composable
fun CommunityTeamSearchScreenPreview(){
    CommunityTeamSearchScreenContent(
        searchValue = "",
        onSearchValueChange = {},
        teamList = PendingModel(listOf(
            TeamSearchInfoModel(
                teamNo = 1,
                profileImageUrl = ExamplePictureUrl,
                teamName = "건강한 습관 만들기",
                memberCount = 4,
                capacity = 5,
                currentDistance = 30.2f,
                targetDistance = 150f,
                teamIntro = "하루 7천보 이상 함께 걸어요!"
            ),
            TeamSearchInfoModel(
                teamNo = 2,
                profileImageUrl = ExamplePictureUrl,
                teamName = "매일 만보 걷기",
                memberCount = 3,
                capacity = 5,
                currentDistance = 60.2f,
                targetDistance = 150f,
                teamIntro = "만보 걷기 습관을 만들어요!"
            ),
            TeamSearchInfoModel(
                teamNo = 3,
                profileImageUrl = ExamplePictureUrl,
                teamName = "아침 걷기 챌린지",
                memberCount = 2,
                capacity = 5,
                currentDistance = 40.2f,
                targetDistance = 150f,
                teamIntro = "아침에 함께 걸어요!"
            ),
            TeamSearchInfoModel(
                teamNo = 4,
                profileImageUrl = ExamplePictureUrl,
                teamName = "건강한 습관 만들기",
                memberCount = 1,
                capacity = 5,
                currentDistance = 10.2f,
                targetDistance = 150f,
                teamIntro = "하루 7천보 이상 함께 걸어요!"
            ),
            TeamSearchInfoModel(
                teamNo = 5,
                profileImageUrl = ExamplePictureUrl,
                teamName = "주말 러닝 & 걷기",
                memberCount = 1,
                capacity = 5,
                currentDistance = 50.2f,
                targetDistance = 150f,
                teamIntro = "주말에 함께 러닝과 걷기!"
            )
        ), LoadingState.Success),
        onSearchClick = {},
        onTeamMakeMove = {},
        onJoinClick = {}
    )
}

@Composable
fun CommunityTeamSearchScreen(
    communityViewModel: CommunityViewModel?,
    communityTeamSearchViewModel: CommunityTeamSearchViewModel = hiltViewModel(),
    onTeamMakeMove: () -> Unit
) {
    val context = LocalContext.current
    var searchValue by remember { mutableStateOf("") }
    val teamList by
        communityTeamSearchViewModel.teamList.collectAsState()

    CommunityTeamSearchScreenContent(
        searchValue = searchValue,
        onSearchValueChange = {
            searchValue = it
        },
        onSearchClick = {
            communityTeamSearchViewModel.getTeamList(searchValue)
        },
        onTeamMakeMove = onTeamMakeMove,
        onJoinClick = {
            communityTeamSearchViewModel.joinTeam(
                context = context,
                teamId = it.teamNo,
                onJoinSuccess = {
                    communityViewModel?.getUserTeamInfo()
                }
            )
        },
        teamList = teamList
    )
}

@Composable
fun CommunityTeamSearchScreenContent(
    searchValue: String,
    onSearchValueChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onTeamMakeMove: () -> Unit,
    onJoinClick: (TeamSearchInfoModel) -> Unit,
    teamList: PendingModel<List<TeamSearchInfoModel>>,
){
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .mainScreen()
    ) {
        TopNavigation(
            title = "팀 검색 / 가입",
        )
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(
                    horizontal = 10.dp,
                    vertical = 12.dp
                )
        ){
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                ){
                    TextField(
                        isMaxLengthView = false,
                        rightIcon = {
                            Icon(
                                painter = painterResource(R.drawable.search),
                                contentDescription = "검색",
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable(
                                        onClick = onSearchClick
                                    )
                            )
                        },
                        value = searchValue,
                        onValueChange = onSearchValueChange,
                        placeholderText = "팀 이름을 검색하세요",
                        maxLength = 20,
                        sizeType = LayoutSize.FillMaxSize
                    )
                }

                PrimaryButton(
                    text = "+ 팀 만들기",
                    onClick = onTeamMakeMove
                )
            }

            Spacer(Modifier.height(20.dp))

            if(teamList.loadingState == LoadingState.Success){
                teamList.data?.let{
                    Text(
                        text = "검색 결과(${it.size})",
                        style = AppTypography.labelLarge.medium,
                        color = Black,
                    )

                    Spacer(Modifier.height(8.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        it.forEach { team ->
                            TeamSearchInfoBox(
                                teamSearchInfoModel = team,
                                onJoinClick = onJoinClick
                            )
                        }
                    }
                }
            }else if(teamList.loadingState == LoadingState.Error){
                Text(
                    text = "팀 검색 결과를 불러오는데 실패했습니다.",
                    style = AppTypography.labelLarge.medium,
                    color = Black,
                )
            }
        }
    }
}
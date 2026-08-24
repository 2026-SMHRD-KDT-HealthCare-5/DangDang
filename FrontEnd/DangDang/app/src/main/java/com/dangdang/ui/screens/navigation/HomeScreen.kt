package com.dangdang.ui.screens.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.dangdang.Application.Companion.ExamplePictureUrl
import com.dangdang.common.utils.mainScreen
import com.dangdang.component.chart.GlucoseTrendChart
import com.dangdang.component.errorview.ErrorView
import com.dangdang.component.navigation.topnavigation.TopNavigation
import com.dangdang.component.page.home.HomeGuideBox
import com.dangdang.component.page.home.HomeTeamChallengeStatus
import com.dangdang.component.page.home.WeeklyCheckListBox
import com.dangdang.data.enums.LoadingState
import com.dangdang.data.enums.WeeklyAttendanceStatus
import com.dangdang.data.model.community.TeamInfoModel
import com.dangdang.data.model.community.TeamMemberChallengeStatusModel
import com.dangdang.data.model.home.AfterMealGlucoseStatusModel
import com.dangdang.data.model.home.GlucoseChartPointModel
import com.dangdang.data.model.home.WeeklyGlucoseCheckModel
import com.dangdang.ui.viewmodel.home.HomeViewModel

@Preview
@Composable
fun HomeScreenPreview(

){
    HomeScreenContent(
        onFoodInputClick = {},
        onTeamChallengeMoreClick = {},
        weeklyGlucoseCheckList = listOf(
            WeeklyGlucoseCheckModel(
                day = "월",
                status = WeeklyAttendanceStatus.MISSED.name
            ),
            WeeklyGlucoseCheckModel(
                day = "화",
                status = WeeklyAttendanceStatus.DONE.name
            ),
            WeeklyGlucoseCheckModel(
                day = "수",
                status = WeeklyAttendanceStatus.NONE.name
            ),
            WeeklyGlucoseCheckModel(
                day = "목",
                status = WeeklyAttendanceStatus.NONE.name
            ),
            WeeklyGlucoseCheckModel(
                day = "금",
                status = WeeklyAttendanceStatus.NONE.name
            ),
            WeeklyGlucoseCheckModel(
                day = "토",
                status = WeeklyAttendanceStatus.NONE.name
            ),
            WeeklyGlucoseCheckModel(
                day = "일",
                status = WeeklyAttendanceStatus.NONE.name
            )
        ),
        afterMealGlucoseStatus = AfterMealGlucoseStatusModel(
            targetGlucose = 180f,
            points = listOf(
                GlucoseChartPointModel(
                    time = "12:00",
                    glucose = 180
                ),
                GlucoseChartPointModel(
                    time = "13:00",
                    glucose = 170
                ),
            )
        ),
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
    )
}

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = hiltViewModel(),
    onFoodInputClick: () -> Unit,
    onTeamChallengeMoreClick: () -> Unit
){
    val homeData by
        homeViewModel.homeData.collectAsState()

    val teamInfo by
        homeViewModel.teamInfo.collectAsState()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        homeViewModel.getHomeData()
        homeViewModel.getUserTeamInfo()
    }

    if(homeData.loadingState == LoadingState.Success
        && teamInfo.loadingState == LoadingState.Success){
        
        HomeScreenContent(
            onFoodInputClick = onFoodInputClick,
            onTeamChallengeMoreClick = onTeamChallengeMoreClick,
            weeklyGlucoseCheckList = homeData.data?.weeklyAttendance?:emptyList(),
            afterMealGlucoseStatus = homeData.data?.glucoseTrend,
            teamInfo = teamInfo.data,
        )
    }else{
        ErrorView(
            loadingState = if(
                homeData.loadingState == LoadingState.Error
                 || teamInfo.loadingState == LoadingState.Error
            ){
                LoadingState.Error
            }else{
                LoadingState.Loading
            },
            message = "홈 화면 데이터 불러오기를 실패했습니다."
        )
    }
}

@Composable
fun HomeScreenContent(
    onFoodInputClick: () -> Unit,
    onTeamChallengeMoreClick: () -> Unit,
    weeklyGlucoseCheckList : List<WeeklyGlucoseCheckModel>,
    afterMealGlucoseStatus: AfterMealGlucoseStatusModel?,
    teamInfo: TeamInfoModel?,
){
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .mainScreen()
    ) {
        TopNavigation(
            title = "홈"
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(
                    vertical = 20.dp,
                    horizontal = 30.dp
                ),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            HomeGuideBox(
                onButtonClick = onFoodInputClick
            )

            WeeklyCheckListBox(
                weeklyGlucoseCheckList = weeklyGlucoseCheckList
            )

            afterMealGlucoseStatus?.let {
                GlucoseTrendChart(
                    values = it.points,
                    goal = it.targetGlucose
                )
            }

            teamInfo?.let{
                HomeTeamChallengeStatus(
                    teamInfo = it,
                    onMoreClick = onTeamChallengeMoreClick
                )
            }
        }
    }
}
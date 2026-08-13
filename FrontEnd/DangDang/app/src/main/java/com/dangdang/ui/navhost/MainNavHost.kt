package com.dangdang.ui.navhost

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.dangdang.common.utils.AppPrefs
import com.dangdang.common.utils.AppRoute
import com.dangdang.common.utils.CommunityRoute
import com.dangdang.common.utils.DangDangRoute
import com.dangdang.common.utils.MainRoute
import com.dangdang.common.utils.MyPageRoute
import com.dangdang.common.utils.navigateBottomTab
import com.dangdang.ui.screens.first.SignUpScreen
import com.dangdang.ui.screens.navigation.CommunityScreen
import com.dangdang.ui.screens.navigation.DangDangScreen
import com.dangdang.ui.screens.navigation.HomeScreen
import com.dangdang.ui.screens.navigation.MyPageScreen
import com.dangdang.ui.screens.navigation.WalkScreen
import com.dangdang.ui.screens.navigation.community.teammake.CommunityTeamMakeScreen
import com.dangdang.ui.screens.navigation.dangdang.FoodInputDirectlyScreen

@Composable
fun MainNavHost(
    appPrefs: AppPrefs,
    navController: NavHostController?,
) {
    if (navController != null) {
        NavHost(
            navController = navController,
            startDestination = MainRoute.Home.route,
        ) {
            //홈
            composable(MainRoute.Home.route) {
                HomeScreen(
                    onFoodInputClick = {
                        navigateBottomTab(
                            navController = navController,
                            route = MainRoute.DangDang.route
                        )
                    },
                    onTeamChallengeMoreClick = {
                        navigateBottomTab(
                            navController = navController,
                            route = MainRoute.Community.route
                        )
                    }
                )
            }

            //당당이
            composable(
                route = "${MainRoute.DangDang.route}?isWalkComplete={isWalkComplete}",
                arguments = listOf(
                    navArgument("isWalkComplete") {
                        type = NavType.BoolType
                        defaultValue = false
                    }
                )
            ) { backStackEntry ->
                val isWalkComplete = backStackEntry.arguments?.getBoolean("isWalkComplete") ?: false
                DangDangScreen(
                    onWalkChallengeMove = {
                        navigateBottomTab(
                            navController = navController,
                            route = "${MainRoute.Walk.route}?isStart=true"
                        )
                    },
                    isWalkComplete = isWalkComplete,
                    onFoodInputDirectlyClick = {
                        navController.navigate(DangDangRoute.FoodInputDirectly.route)
                    },
                    navController = navController
                )
            }

            //당당이-음식 직접 입력
            composable(DangDangRoute.FoodInputDirectly.route) {
                FoodInputDirectlyScreen(
                    navController = navController
                )
            }

            //걷기
            composable(
                route = "${MainRoute.Walk.route}?isStart={isStart}",
                arguments = listOf(
                    navArgument("isStart") {
                        type = NavType.BoolType
                        defaultValue = false
                    }
                )
            ) { backStackEntry ->
                val isStart = backStackEntry.arguments?.getBoolean("isStart") ?: false
                WalkScreen(
                    isStart = isStart,
                    onSendGlucoseClick = {
                        navigateBottomTab(
                            navController = navController,
                            route = "${MainRoute.DangDang.route}?isWalkComplete=true"
                        )
                        // 탭 복원(restoreState) 시 인자가 유실되는 경우를 대비해 savedStateHandle에도 저장
                        navController.currentBackStackEntry?.savedStateHandle?.set("isWalkComplete", true)
                    }
                )
            }

            //커뮤니티
            composable(MainRoute.Community.route) {
                CommunityScreen(
                    onTeamMakeMove = {
                        navController.navigate(CommunityRoute.TeamMake.route)
                    },
                    navController = navController
                )
            }

            //내 정보
            composable(MainRoute.MyPage.route) {
                MyPageScreen(
                    onMyInfoUpdateMove = {
                        navController.navigate(MyPageRoute.MyInfoUpdate.route)
                    },
                    onFaqClick = {
                        navigateBottomTab(
                            navController = navController,
                            route = MainRoute.DangDang.route
                        )
                    }
                )
            }

            //회원정보 수정
            composable(MyPageRoute.MyInfoUpdate.route) {
                SignUpScreen(
                    navController = navController,
                    isUpdate = true,
                    isEmailDisable = true
                )
            }

            //팀 만들기
            composable(CommunityRoute.TeamMake.route) {
                CommunityTeamMakeScreen(
                    navController = navController
                )
            }
        }
    }
}
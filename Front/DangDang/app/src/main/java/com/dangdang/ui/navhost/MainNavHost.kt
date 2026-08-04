package com.dangdang.ui.navhost

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dangdang.common.utils.AppPrefs
import com.dangdang.common.utils.CommunityRoute
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
            composable(MainRoute.DangDang.route) {
                DangDangScreen(

                )
            }

            //걷기
            composable(MainRoute.Walk.route) {
                WalkScreen(

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
                    appPrefs = appPrefs,
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
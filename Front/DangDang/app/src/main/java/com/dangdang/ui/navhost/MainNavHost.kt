package com.dangdang.ui.navhost

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dangdang.common.utils.MainRoute
import com.dangdang.ui.screens.navigation.CommunityScreen
import com.dangdang.ui.screens.navigation.DangDangScreen
import com.dangdang.ui.screens.navigation.HomeScreen
import com.dangdang.ui.screens.navigation.MyPageScreen
import com.dangdang.ui.screens.navigation.WalkScreen

@Composable
fun MainNavHost(
    navController: NavHostController?,
) {
    if (navController != null) {
        NavHost(
            navController = navController,
            startDestination = MainRoute.Home.route,
        ) {
            composable(MainRoute.Home.route) {
                HomeScreen(

                )
            }

            composable(MainRoute.DangDang.route) {
                DangDangScreen(

                )
            }

            composable(MainRoute.Walk.route) {
                WalkScreen(

                )
            }

            composable(MainRoute.Community.route) {
                CommunityScreen(

                )
            }

            composable(MainRoute.MyPage.route) {
                MyPageScreen(

                )
            }
        }
    }
}
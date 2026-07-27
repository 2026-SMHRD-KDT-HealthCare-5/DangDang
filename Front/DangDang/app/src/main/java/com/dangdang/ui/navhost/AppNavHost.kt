package com.dangdang.ui.navhost

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dangdang.common.utils.AppPrefs
import com.dangdang.common.utils.AppRoute
import com.dangdang.ui.screens.first.IntroScreen
import com.dangdang.ui.screens.first.LoginScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    appPrefs: AppPrefs
) {

    NavHost(
        navController = navController,
        startDestination = AppRoute.Intro.route
    ) {
        //첫 화면
        composable(AppRoute.Intro.route) {
            IntroScreen(
                appPrefs = appPrefs
            )
        }

        //로그인 화면
        composable(AppRoute.Login.route) {
            LoginScreen(

            )
        }
    }
}
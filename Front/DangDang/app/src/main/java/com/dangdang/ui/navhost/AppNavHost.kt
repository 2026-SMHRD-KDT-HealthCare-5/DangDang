package com.dangdang.ui.navhost

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dangdang.common.utils.AppPrefs
import com.dangdang.common.utils.AppRoute
import com.dangdang.ui.screens.first.LoginScreen
import com.dangdang.ui.screens.first.SignUpScreen
import com.dangdang.ui.screens.main.MainScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    appPrefs: AppPrefs
) {
    val isAutoLogin = appPrefs.isAutoLogin()

    NavHost(
        navController = navController,
        startDestination = if(isAutoLogin) AppRoute.Main.route else AppRoute.Login.route
    ) {
        //로그인 화면
        composable(AppRoute.Login.route) {
            LoginScreen(
                onLoginSuccess = { isSignUp ->
                    if(isSignUp){
                        //회원가입 화면으로 이동
                        navController.navigate(AppRoute.SignUp.route)
                    }else{
                        //메인화면으로 이동
                        navController.navigate(AppRoute.Main.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                    }
                }
            )
        }

        //회원가입화면
        composable(AppRoute.SignUp.route) {
            SignUpScreen(

            )
        }

        //메인화면
        composable(AppRoute.Main.route) {
            MainScreen(
                appPrefs = appPrefs
            )
        }
    }
}
package com.dangdang.ui.navhost

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.dangdang.common.utils.AppPrefs
import com.dangdang.common.utils.AppRoute
import com.dangdang.ui.screens.first.LoginScreen
import com.dangdang.ui.screens.first.SignUpCompleteScreen
import com.dangdang.ui.screens.first.SignUpScreen
import com.dangdang.ui.screens.first.SplashScreen
import com.dangdang.ui.screens.main.MainScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Composable
fun AppNavHost(
    navController: NavHostController,
    appPrefs: AppPrefs
) {

    NavHost(
        navController = navController,
        startDestination = AppRoute.Splash.route
    ) {
        composable(AppRoute.Splash.route) {

            val isAutoLogin by appPrefs.autoLoginFlow
                .collectAsStateWithLifecycle(
                    initialValue = null
                )

            LaunchedEffect(isAutoLogin) {

                when (isAutoLogin) {

                    true -> {
                        navController.navigate(
                            AppRoute.Main.route
                        ) {
                            popUpTo(
                                AppRoute.Splash.route
                            ) {
                                inclusive = true
                            }
                        }
                    }

                    false -> {
                        navController.navigate(
                            AppRoute.Login.route
                        ) {
                            popUpTo(
                                AppRoute.Splash.route
                            ) {
                                inclusive = true
                            }
                        }
                    }

                    null -> {

                    }
                }
            }

            SplashScreen()
        }

        //로그인 화면
        composable(AppRoute.Login.route) {
            LoginScreen(
                onLoginSuccess = { isSignUp ->
                    if(isSignUp){
                        //회원가입 화면으로 이동
                        navController.navigate("${AppRoute.SignUp.route}?isSocial=true")
                    }else{
                        //메인화면으로 이동
                        navController.navigate(AppRoute.Main.route) {
                            popUpTo(0) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                },
                onSignupMove = {
                    navController.navigate(AppRoute.SignUp.route)
                }
            )
        }

        //회원가입화면
        composable(
            route = "${AppRoute.SignUp.route}?isSocial={isSocial}",
            arguments = listOf(
                navArgument("isSocial") {
                    type = NavType.StringType
                    defaultValue = "false"
                }
            )
        ) { backStackEntry ->
            val isSocial = backStackEntry.arguments?.getString("isSocial")
            SignUpScreen(
                navController = navController,
                isUpdate = false,
                isSocial = isSocial == "true",
                isEmailDisable = isSocial == "true"
            )
        }

        //회원가입 완료화면
        composable(AppRoute.SignUpComplete.route) {
            SignUpCompleteScreen(
                onHomeMove = {
                    //메인화면으로 이동
                    navController.navigate(AppRoute.Main.route) {
                        popUpTo(0) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
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
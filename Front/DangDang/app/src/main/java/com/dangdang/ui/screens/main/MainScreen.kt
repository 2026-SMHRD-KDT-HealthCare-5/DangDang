package com.dangdang.ui.screens.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dangdang.common.utils.AppPrefs
import com.dangdang.common.utils.CommunityRoute
import com.dangdang.common.utils.MainRoute
import com.dangdang.common.utils.MyPageRoute
import com.dangdang.common.utils.navigateBottomTab
import com.dangdang.common.utils.screen
import com.dangdang.component.navigation.bottomnavigation.BottomNavigation
import com.dangdang.ui.navhost.MainNavHost

@Preview
@Composable
fun MainScreenPreview(

){
    MainScreenContent(
        currentRoute = MainRoute.Home,
        onNavItemClick = {}
    )
}

@Composable
fun MainScreen(
    appPrefs: AppPrefs
){
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val currentRoute =
        MainRoute.values.find {
            it.route == navBackStackEntry?.destination?.route
        } ?:
        if(
            MyPageRoute.stringValues.contains(
                navBackStackEntry?.destination?.route?:""
            )
        ){
            MainRoute.MyPage
        }else if(
            CommunityRoute.stringValues.contains(
                navBackStackEntry?.destination?.route?:""
            )
        ){
            MainRoute.Community
        }
        else{
            MainRoute.Home
        }

    MainScreenContent(
        mainNavHost = {
            MainNavHost(
                appPrefs = appPrefs,
                navController = navController
            )
        },
        navController = navController,
        currentRoute = currentRoute,
        onNavItemClick = { route ->
            navigateBottomTab(
                navController = navController,
                route = route.route
            )
        }
    )
}

@Composable
fun MainScreenContent(
    mainNavHost: @Composable (NavHostController?) -> Unit = {},
    navController: NavHostController? = null,
    currentRoute: MainRoute,
    onNavItemClick: (MainRoute) -> Unit
){
    Column(
        modifier = Modifier
            .screen()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ){
            mainNavHost(navController)
        }

        BottomNavigation(
            currentRoute = currentRoute,
            onItemClick = onNavItemClick
        )
    }
}
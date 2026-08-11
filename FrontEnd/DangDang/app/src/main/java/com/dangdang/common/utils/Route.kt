package com.dangdang.common.utils

import com.dangdang.R
import com.dangdang.common.utils.MainRoute.Community
import com.dangdang.common.utils.MainRoute.DangDang
import com.dangdang.common.utils.MainRoute.Home
import com.dangdang.common.utils.MainRoute.MyPage
import com.dangdang.common.utils.MainRoute.Walk
import com.dangdang.common.utils.MyPageRoute.MyInfoUpdate

sealed class AppRoute(
    val route: String
) {
    data object Splash : AppRoute("splash")
    data object Login : AppRoute("login")
    data object SignUp : AppRoute("signUp")
    data object SignUpComplete : AppRoute("signUpComplete")
    data object Main : AppRoute("main")
}

sealed class MyPageRoute(
    val route: String
){
    data object MyInfoUpdate : MyPageRoute("myInfoUpdate")

    companion object {
        val values: List<MyPageRoute>
            get() = listOf(
                MyInfoUpdate
            )
        val stringValues: List<String>
            get() = values.map { it.route }
    }
}

sealed class DangDangRoute(
    val route: String
){
    data object FoodInputDirectly : DangDangRoute("foodInputDirectly")

    companion object {
        val values: List<DangDangRoute>
            get() = listOf(
                FoodInputDirectly
            )
        val stringValues: List<String>
            get() = values.map { it.route }
    }
}

sealed class CommunityRoute(
    val route: String
){
    data object TeamMake : CommunityRoute("teamMake")

    companion object {
        val values: List<CommunityRoute>
            get() = listOf(
                TeamMake
            )
        val stringValues: List<String>
            get() = values.map { it.route }
    }
}

sealed class MainRoute(
    val route: String,
    val name: String,
    val enableIcon: Int,
    val disableIcon: Int
) {
    data object Home : MainRoute(
        route = "home",
        name = "홈",
        enableIcon = R.drawable.home_blue,
        disableIcon = R.drawable.home_black
    )
    data object DangDang : MainRoute(
        route = "dangdang",
        name = "당당이",
        enableIcon = R.drawable.chat_blue,
        disableIcon = R.drawable.chat_black
    )
    data object Walk : MainRoute(
        route = "walk",
        name = "걷기",
        enableIcon = R.drawable.walk_blue,
        disableIcon = R.drawable.walk_black
    )
    data object Community : MainRoute(
        route = "community",
        name = "커뮤니티",
        enableIcon = R.drawable.community_blue,
        disableIcon = R.drawable.community_black
    )
    data object MyPage : MainRoute(
        route = "mypage",
        name = "내 정보",
        enableIcon = R.drawable.mypage_blue,
        disableIcon = R.drawable.mypage_black
    )

    companion object {
        val values: List<MainRoute>
            get() = listOf(
                Home, DangDang, Walk, Community, MyPage
            )
    }
}
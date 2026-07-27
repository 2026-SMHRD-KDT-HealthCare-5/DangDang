package com.dangdang.common.utils

import com.dangdang.R

sealed class AppRoute(
    val route: String
) {
    data object Intro : AppRoute("intro")
    data object Login : AppRoute("login")
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
        enableIcon = R.mipmap.home_blue,
        disableIcon = R.mipmap.home_black
    )
    data object DangDang : MainRoute(
        route = "dangdang",
        name = "당당이",
        enableIcon = R.mipmap.chat_blue,
        disableIcon = R.mipmap.chat_black
    )
    data object Walk : MainRoute(
        route = "walk",
        name = "걷기",
        enableIcon = R.mipmap.walk_blue,
        disableIcon = R.mipmap.walk_black
    )
    data object Community : MainRoute(
        route = "community",
        name = "커뮤니티",
        enableIcon = R.mipmap.community_blue,
        disableIcon = R.mipmap.community_black
    )
    data object MyPage : MainRoute(
        route = "mypage",
        name = "내 정보",
        enableIcon = R.mipmap.mypage_blue,
        disableIcon = R.mipmap.mypage_black
    )

    companion object {
        val values = listOf(
            Home, DangDang, Walk, Community, MyPage
        )
    }
}
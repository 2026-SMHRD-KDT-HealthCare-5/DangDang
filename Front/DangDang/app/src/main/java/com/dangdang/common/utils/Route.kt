package com.dangdang.common.utils

sealed class AppRoute(
    val route: String
) {
    data object Intro : AppRoute("intro")
}
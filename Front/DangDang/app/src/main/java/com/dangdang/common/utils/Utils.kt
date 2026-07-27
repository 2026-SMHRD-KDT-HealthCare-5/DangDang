package com.dangdang.common.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.dangdang.data.enums.LayoutSize

//화면마다 공통으로 사용하는 modifier
fun Modifier.screen() = this
    .fillMaxSize()
    .background(Color.White)
    .systemBarsPadding()

fun Modifier.mainScreen() = this
    .fillMaxSize()
    .background(Color.White)

fun componentWidthModifier(
    fixWidth: Dp? = null,
    sizeType: LayoutSize = LayoutSize.DefaultSize,
): Modifier{
    return when(sizeType){
        LayoutSize.DefaultSize ->
            Modifier
        LayoutSize.FixSize ->
            if(fixWidth != null){
                Modifier.width(fixWidth)
            } else {
                Modifier
            }
        LayoutSize.FillMaxSize ->
            Modifier.fillMaxWidth()
    }
}

val TextStyle.regular: TextStyle
    get() = copy(fontWeight = FontWeight.Normal)

val TextStyle.medium: TextStyle
    get() = copy(fontWeight = FontWeight.Medium)

val TextStyle.bold: TextStyle
    get() = copy(fontWeight = FontWeight.Bold)

fun navigateBottomTab(
    navController: NavHostController,
    route: String
) {
    navController.navigate(route) {

        popUpTo(
            navController.graph.findStartDestination().id
        ) {
            saveState = true
        }

        launchSingleTop = true
        restoreState = true
    }
}
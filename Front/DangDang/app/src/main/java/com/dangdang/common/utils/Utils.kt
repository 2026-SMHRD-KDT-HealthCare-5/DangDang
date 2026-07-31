package com.dangdang.common.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.core.net.toUri
import com.dangdang.Application.Companion.InquiryEmail
import com.dangdang.ui.theme.DarkGreen
import com.dangdang.ui.theme.HotPink
import com.dangdang.ui.theme.Orange
import com.dangdang.ui.theme.PrimaryBlue
import com.dangdang.ui.theme.PrimaryPurple
import java.text.DecimalFormat

//화면마다 공통으로 사용하는 modifier
fun Modifier.screen() = this
    .fillMaxSize()
    .background(Color.White)
    .systemBarsPadding()

fun Modifier.mainScreen() = this
    .fillMaxSize()
    .background(Color.White)

fun Modifier.componentWidthModifier(
    fixWidth: Dp? = null,
    sizeType: LayoutSize = LayoutSize.DefaultSize,
) = this
    .then(
        when(sizeType){
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
    )

val TextStyle.regular: TextStyle
    get() = copy(fontWeight = FontWeight.Normal)

val TextStyle.medium: TextStyle
    get() = copy(fontWeight = FontWeight.Medium)

val TextStyle.bold: TextStyle
    get() = copy(fontWeight = FontWeight.Bold)

fun addComma(number: Int): String {
    val formatter = DecimalFormat("#,###")
    val formattedNumber = formatter.format(number)
    return formattedNumber
}

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

fun sendMail(context: Context){
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = "mailto:".toUri()
        putExtra(Intent.EXTRA_EMAIL, arrayOf(InquiryEmail))
    }

    context.startActivity(intent)
}

val GuageColorList = listOf(
    PrimaryBlue,
    DarkGreen,
    Orange,
    PrimaryPurple,
    HotPink
)
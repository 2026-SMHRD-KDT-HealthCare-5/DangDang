package com.dangdang.ui.screens.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.dangdang.common.utils.mainScreen
import com.dangdang.common.utils.regular
import com.dangdang.ui.theme.AppTypography

@Preview
@Composable
fun MyPageScreenPreview(

){
    MyPageScreenContent()
}

@Composable
fun MyPageScreen(

){
    MyPageScreenContent()
}

@Composable
fun MyPageScreenContent(

){
    Column(
        modifier = Modifier
            .mainScreen()
    ) {
        Text(
            text = "내정보",
            style = AppTypography.labelMedium.regular,
        )
    }
}
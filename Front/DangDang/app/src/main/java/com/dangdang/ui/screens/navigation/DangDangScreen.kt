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
fun DangDangScreenPreview(

){
    DangDangScreenContent()
}

@Composable
fun DangDangScreen(

){
    DangDangScreenContent()
}

@Composable
fun DangDangScreenContent(

){
    Column(
        modifier = Modifier
            .mainScreen()
    ) {
        Text(
            text = "당당이",
            style = AppTypography.labelMedium.regular,
        )
    }
}
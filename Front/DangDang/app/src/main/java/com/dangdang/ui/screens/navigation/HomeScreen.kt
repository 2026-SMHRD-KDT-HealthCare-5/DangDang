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
fun HomeScreenPreview(

){
    HomeScreenContent()
}

@Composable
fun HomeScreen(

){
    HomeScreenContent()
}

@Composable
fun HomeScreenContent(

){
    Column(
        modifier = Modifier
            .mainScreen()
    ) {
        Text(
            text = "홈",
            style = AppTypography.labelMedium.regular,
        )
    }
}
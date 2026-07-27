package com.dangdang.ui.screens.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.dangdang.common.utils.mainScreen
import com.dangdang.common.utils.regular
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.Navy

@Preview
@Composable
fun CommunityScreenPreview(

){
    CommunityScreenContent()
}

@Composable
fun CommunityScreen(

){
    CommunityScreenContent()
}

@Composable
fun CommunityScreenContent(

){
    Column(
        modifier = Modifier
            .mainScreen()
    ) {
        Text(
            text = "커뮤니티",
            style = AppTypography.labelMedium.regular,
        )
    }
}
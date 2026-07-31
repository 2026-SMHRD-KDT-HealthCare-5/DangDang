package com.dangdang.ui.screens.navigation.community.tab

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.dangdang.common.utils.mainScreen
import com.dangdang.common.utils.regular
import com.dangdang.ui.theme.AppTypography

@Preview
@Composable
fun CommunityRankingTabScreenPreview(){
    CommunityRankingTabScreenContent()
}

@Composable
fun CommunityRankingTabScreen() {
    CommunityRankingTabScreenContent()
}

@Composable
fun CommunityRankingTabScreenContent(){
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .mainScreen()
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "전체 랭킹",
            style = AppTypography.labelMedium.regular,
        )
    }
}
package com.dangdang.ui.screens.first

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.dangdang.common.utils.regular
import com.dangdang.common.utils.screen
import com.dangdang.ui.theme.AppTypography

@Preview
@Composable
fun SignUpScreenPreview(

){
    SignUpScreenContent()
}

@Composable
fun SignUpScreen(

){
    SignUpScreenContent()
}

@Composable
fun SignUpScreenContent(

){
    Column(
        modifier = Modifier
            .screen()
    ) {
        Text(
            text = "회원가입",
            style = AppTypography.labelMedium.regular,
        )
    }
}
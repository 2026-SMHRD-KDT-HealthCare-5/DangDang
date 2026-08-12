package com.dangdang.component.errorview

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.dangdang.common.utils.mainScreen
import com.dangdang.common.utils.medium
import com.dangdang.data.enums.LoadingState
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.Red

@Preview
@Composable
fun ErrorViewPreview() {
    ErrorView(
        loadingState = LoadingState.Error,
        message = "오류가 발생했습니다."
    )
}

@Composable
fun ErrorView(
    loadingState: LoadingState,
    message:String
) {
    Box(
        modifier = Modifier
            .mainScreen(),
        contentAlignment = Alignment.Center
    ){
        if(loadingState == LoadingState.Error){
            Text(
                text = message,
                style = AppTypography.titleLarge.medium,
                color = Red,
            )
        }
    }
}
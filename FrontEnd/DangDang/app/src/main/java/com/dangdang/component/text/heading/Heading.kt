package com.dangdang.component.text.heading

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.common.utils.medium
import com.dangdang.common.utils.regular
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.Gray
import com.dangdang.ui.theme.White

@Preview
@Composable
fun HeadingPreview(

){
    Heading(
        title = "기본 정보를 입력해주세요",
        description = "계정 생성에 필요한 정보를 입력해주세요"
    )
}

@Composable
fun Heading(
    title: String,
    description: String
){
    Column(
        modifier = Modifier
            .background(
                color = White
            ),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = AppTypography.bodyLarge.medium,
            color = Black
        )

        Text(
            text = description,
            style = AppTypography.labelMedium.regular,
            color = Gray
        )
    }
}
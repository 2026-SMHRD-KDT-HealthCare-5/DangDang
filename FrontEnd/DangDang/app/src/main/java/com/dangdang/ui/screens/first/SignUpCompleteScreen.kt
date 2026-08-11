package com.dangdang.ui.screens.first

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.R
import com.dangdang.common.utils.bold
import com.dangdang.common.utils.medium
import com.dangdang.common.utils.regular
import com.dangdang.common.utils.screen
import com.dangdang.component.button.outlined.SecondaryOutlinedButton
import com.dangdang.data.enums.LayoutSize
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.SkyBlueOpacity30

@Preview
@Composable
fun SignUpCompleteScreenPreview(
){
    SignUpCompleteScreenContent(
        onHomeMove = {}
    )
}

@Composable
fun SignUpCompleteScreen(
    onHomeMove: ()-> Unit
){
    SignUpCompleteScreenContent(
        onHomeMove = onHomeMove
    )
}

@Composable
fun SignUpCompleteScreenContent(
    onHomeMove: ()-> Unit
){
    Column(
        modifier = Modifier
            .screen()
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = SkyBlueOpacity30
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.dangdang_signupcomplete),
                contentDescription = "회원가입 완료 이미지",
                modifier = Modifier
                    .size(120.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "\uD83C\uDF89 회원가입이 완료되었습니다!",
                    style = AppTypography.bodyLarge.medium,
                    color = Black,
                )

                Text(
                    text = "당당이가 맞춤형 건강 관리로 \n" +
                            "여러분의 건강한 습관을 함께 만들어갈게요.",
                    style = AppTypography.labelMedium.regular,
                    color = Black,
                )

                SecondaryOutlinedButton(
                    text = "홈으로 이동",
                    fixWidth = 150.dp,
                    sizeType = LayoutSize.FixSize,
                    onClick = onHomeMove
                )
            }
        }
    }
}
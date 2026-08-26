package com.dangdang.component.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.R
import com.dangdang.common.utils.addComma
import com.dangdang.common.utils.medium
import com.dangdang.common.utils.regular
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.LightBlack
import com.dangdang.ui.theme.MediumRoundShape
import com.dangdang.ui.theme.WhiteSmoke

@Preview
@Composable
fun FoodNotRecognizeBoxPreview(

){
    FoodNotRecognizeBox()
}

@Composable
fun FoodNotRecognizeBox(

){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = WhiteSmoke,
                shape = MediumRoundShape
            )
            .padding(
                vertical = 30.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.magnifire),
            contentDescription = "식품 인식 실패",
            modifier = Modifier
                .width(102.dp)
                .height(98.dp)
        )

        Text(
            text = buildAnnotatedString {
                withStyle(
                    AppTypography.titleLarge.regular
                        .toSpanStyle()
                        .copy(
                            color = Black
                        )
                ) {
                    append("검색 결과가 없어요")
                }
                withStyle(
                    AppTypography.labelMedium.regular
                        .toSpanStyle()
                        .copy(
                            color = LightBlack
                        )
                ) {
                    append("\n\n" +
                            "다른 이름으로 검색해보거나\n" +
                            "AI 분석 또는 직접 영양정보를 입력해주세요")
                }
            },
            textAlign = TextAlign.Center
        )
    }
}
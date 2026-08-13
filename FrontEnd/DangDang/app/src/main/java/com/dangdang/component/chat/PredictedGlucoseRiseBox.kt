package com.dangdang.component.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.common.utils.bold
import com.dangdang.common.utils.medium
import com.dangdang.common.utils.regular
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.LightScarlet
import com.dangdang.ui.theme.MediumLineDp
import com.dangdang.ui.theme.MediumRoundShape
import com.dangdang.ui.theme.Scarlet

//예상 혈당 상승량 박스
@Preview
@Composable
fun PredictedGlucoseRiseBoxPreview() {
    PredictedGlucoseRiseBox(
        predictedGlucoseRise = 35,
        beginGlucose = 140
    )
}

@Composable
fun PredictedGlucoseRiseBox(
    predictedGlucoseRise: Int, //예상 혈당 상승량
    beginGlucose: Int //식전 혈당
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = LightScarlet,
                shape = MediumRoundShape
            )
            .border(
                width = MediumLineDp,
                color = Scarlet,
                shape = MediumRoundShape
            )
            .padding(
                vertical = 28.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "예상 혈당 상승량",
            style = AppTypography.bodyLarge.medium,
            color = Black,
        )

        Text(
            text = "+ $predictedGlucoseRise mg/dL",
            style = AppTypography.titleMedium.bold,
            color = Black,
        )

        Text(
            text = "(식전 혈당 $beginGlucose mg/dL 기준)",
            style = AppTypography.labelMedium.regular,
            color = Black,
        )
    }
}
package com.dangdang.component.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.R
import com.dangdang.common.utils.GlucoseFeedbackTemplates
import com.dangdang.common.utils.bold
import com.dangdang.common.utils.regular
import com.dangdang.data.model.chat.GlucoseFeedbackModel
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.ForestGreen
import com.dangdang.ui.theme.LightBlue
import com.dangdang.ui.theme.SapphireBlue

@Preview
@Composable
fun AIGlucoseFeedbackBoxPreview() {
    AIGlucoseFeedbackBox(
        goalGlucose = 180,
        glucoseFeedbackModel = GlucoseFeedbackModel(
            beginGlucose = 140,
            aiPredictAfterGlucose = 175,
            realAfterGlucose = 170,
            decreaseGlucose = -5
        )
    )
}

@Composable
fun AIGlucoseFeedbackBox(
    goalGlucose: Int,
    glucoseFeedbackModel: GlucoseFeedbackModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = LightBlue,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 2.dp,
                color = SapphireBlue,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(
                horizontal = 35.dp,
                vertical = 20.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        GlucoseFeedbackTemplates.forEach { glucoseFeedbackTemplate ->
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = glucoseFeedbackTemplate.title,
                    style = AppTypography.labelMedium.regular,
                    color = Black,
                )

                Text(
                    text = "${glucoseFeedbackTemplate.value(
                        glucoseFeedbackModel
                    )} mg/dL",
                    style = AppTypography.labelMedium.regular,
                    color = Black,
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "목표 (${goalGlucose} mg/dL 미만) 달성!",
                style = AppTypography.labelMedium.bold,
                color = Black,
            )

            Image(
                painter = painterResource(R.mipmap.green_checkbox),
                contentDescription = "green checkbox",
                modifier = Modifier
                    .size(12.dp)
            )
        }
    }
}
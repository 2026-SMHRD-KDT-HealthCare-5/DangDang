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
import com.dangdang.ui.theme.MediumLineDp
import com.dangdang.ui.theme.MediumRoundShape
import com.dangdang.ui.theme.SapphireBlue

@Preview
@Composable
fun AIGlucoseFeedbackBoxPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AIGlucoseFeedbackBox(
            glucoseFeedbackModel = GlucoseFeedbackModel(
                beginGlucose = 140,
                aiPredictAfterGlucose = 175,
                realAfterGlucose = 170,
                walkDistance = 2.5f,
                targetDistance = 2.6f
            )
        )

        AIGlucoseFeedbackBox(
            glucoseFeedbackModel = GlucoseFeedbackModel(
                beginGlucose = 140,
                aiPredictAfterGlucose = 175,
                realAfterGlucose = 170,
                walkDistance = 2.6f,
                targetDistance = 2.6f
            )
        )
    }
}

@Composable
fun AIGlucoseFeedbackBox(
    glucoseFeedbackModel: GlucoseFeedbackModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = LightBlue,
                shape = MediumRoundShape
            )
            .border(
                width = MediumLineDp,
                color = SapphireBlue,
                shape = MediumRoundShape
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
                    )}${glucoseFeedbackTemplate.unit}",
                    style = AppTypography.labelMedium.regular,
                    color = Black,
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val isSuccess = glucoseFeedbackModel.targetDistance <=
                    glucoseFeedbackModel.walkDistance

            Text(
                text = "목표 거리 " +
                        "[ ${glucoseFeedbackModel.targetDistance} km ] " +
                        if(isSuccess){
                            "달성!"
                        } else {
                            "실패"
                        },
                style = AppTypography.labelMedium.bold,
                color = Black,
            )

            Image(
                painter = painterResource(
                    if(isSuccess){
                        R.drawable.green_checkbox
                    }else{
                        R.drawable.failed_icon
                    }
                ),
                contentDescription = "green checkbox",
                modifier = Modifier
                    .size(12.dp)
            )
        }
    }
}
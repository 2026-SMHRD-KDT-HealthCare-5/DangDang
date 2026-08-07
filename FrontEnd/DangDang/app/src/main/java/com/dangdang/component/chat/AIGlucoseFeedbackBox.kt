package com.dangdang.component.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.common.utils.GlucoseFeedbackTemplates
import com.dangdang.common.utils.bold
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
                    style = AppTypography.bodyLarge.bold,
                    color = Black,
                )
            }
        }
    }
}
package com.dangdang.component.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.R
import com.dangdang.common.utils.bold
import com.dangdang.common.utils.regular
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.ForestGreen
import com.dangdang.ui.theme.LightYellow
import com.dangdang.ui.theme.MediumLineDp
import com.dangdang.ui.theme.MediumRoundShape
import com.dangdang.ui.theme.Yellow

@Preview
@Composable
fun AIWarningPreview() {
    AIWarning()
}

@Composable
fun AIWarning(

) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = LightYellow,
                shape = MediumRoundShape
            )
            .border(
                width = MediumLineDp,
                color = Yellow,
                shape = MediumRoundShape
            )
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.notice_icon),
            contentDescription = "warning icon",
            modifier = Modifier
                .size(20.dp)
        )

        Text(
            text = "예상 혈당 상승량은 AI 예측 결과이며,\n" +
                    "정확한 혈당을 의미하지 않아요.\n" +
                    "데이터가 쌓일수록 예측 정확도가\n" +
                    "높아져요!",
            style = AppTypography.labelMedium.regular,
            color = Black,
        )
    }
}
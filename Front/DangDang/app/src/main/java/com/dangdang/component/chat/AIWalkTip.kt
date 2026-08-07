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
import com.dangdang.common.utils.bold
import com.dangdang.common.utils.regular
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.EmeraldGreen
import com.dangdang.ui.theme.ForestGreen
import com.dangdang.ui.theme.LightGreen

@Preview
@Composable
fun AIWalkTipPreview() {
    AIWalkTip()
}

@Composable
fun AIWalkTip(

) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = LightGreen,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 2.dp,
                color = EmeraldGreen,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "\uD83D\uDCA1",
            style = AppTypography.bodyLarge.bold,
            color = ForestGreen,
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "당당이 TIP",
                style = AppTypography.bodyLarge.bold,
                color = ForestGreen,
            )

            Text(
                text = "식후 30분 이후 걷기를 시작하면\n" +
                        "혈당 관리에 도움이 될 수 있어요.\n" +
                        "\n" +
                        "식사 직후 바로 운동하면 오히려 \n" +
                        "혈당이 일시적으로 상승할 수도 있어요!",
                style = AppTypography.labelMedium.regular,
                color = Black,
            )
        }
    }
}
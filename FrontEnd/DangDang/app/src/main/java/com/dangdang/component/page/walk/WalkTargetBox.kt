package com.dangdang.component.page.walk

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.common.utils.medium
import com.dangdang.common.utils.regular
import com.dangdang.component.guage.GuageBar
import com.dangdang.data.enums.GuageBarSize
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.DarkGray
import com.dangdang.ui.theme.DarkGreen
import com.dangdang.ui.theme.Gray
import com.dangdang.ui.theme.PrimaryBlue
import com.dangdang.ui.theme.White
import java.util.Locale
import androidx.compose.ui.platform.LocalLocale
import com.dangdang.ui.theme.MediumRoundShape
import com.dangdang.ui.theme.ThinLineDp

@Preview
@Composable
fun WalkTargetBoxPreview(){
    WalkTargetBox(
        walkTarget = 2.6f,
        currentWalk = 0.85f
    )
}

@Composable
fun WalkTargetBox(
    walkTarget: Float,
    currentWalk: Float,
){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = White,
                shape = MediumRoundShape
            )
            .border(
                width = ThinLineDp,
                color = Gray,
                shape = MediumRoundShape
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "오늘의 목표",
            style = AppTypography.labelLarge.medium,
            color = Black
        )

        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ){
            Text(
                text = "$walkTarget km",
                style = AppTypography.headlineMedium.medium,
                color = PrimaryBlue
            )
            Text(
                text = "걷기",
                style = AppTypography.titleLarge.medium,
                color = Black
            )
        }

        GuageBar(
            size = GuageBarSize.Small,
            guageColor = DarkGreen,
            current = currentWalk,
            target = walkTarget
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "현재",
                style = AppTypography.labelSmall.regular,
                color = DarkGray
            )

            Text(
                text = String.format(LocalLocale.current.platformLocale, "%.2f", currentWalk),
                style = AppTypography.labelSmall.medium,
                color = Black
            )

            Text(
                text = "km (${((currentWalk/walkTarget)*100).toInt()}%)",
                style = AppTypography.labelSmall.regular,
                color = DarkGray
            )
        }
    }
}
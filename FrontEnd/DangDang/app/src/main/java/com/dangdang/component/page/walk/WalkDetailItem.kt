package com.dangdang.component.page.walk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.common.utils.medium
import com.dangdang.common.utils.regular
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.DarkGray
import com.dangdang.ui.theme.White

@Preview
@Composable
fun WalkDetailItemPreview(){
    WalkDetailItem(
        title = "거리",
        current = "0.85",
        unit = "km"
    )
}

@Composable
fun WalkDetailItem(
    title: String,
    current: String,
    unit: String?
) {
    Column(
        modifier = Modifier
            .background(
                color = White
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = AppTypography.labelSmall.regular,
            color = DarkGray,
        )

        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = current,
                style = AppTypography.labelMedium.medium,
                color = Black,
            )

            if(unit != null){
                Text(
                    text = unit,
                    style = AppTypography.labelSmall.regular,
                    color = DarkGray,
                )
            }
        }
    }
}
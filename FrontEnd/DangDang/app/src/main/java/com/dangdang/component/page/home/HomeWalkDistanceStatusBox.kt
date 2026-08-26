package com.dangdang.component.page.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.R
import com.dangdang.component.divider.Divider
import com.dangdang.data.enums.DividerPosition
import com.dangdang.ui.theme.DarkEmeraldGreen
import com.dangdang.ui.theme.DarkPurple
import com.dangdang.ui.theme.DeepBlue
import com.dangdang.ui.theme.Gray
import com.dangdang.ui.theme.MediumRoundShape
import com.dangdang.ui.theme.ThinLineDp
import com.dangdang.ui.theme.White

@Preview
@Composable
fun HomeWalkDistanceStatusBoxPreview(
){
    HomeWalkDistanceStatusBox(
        todayDistance = 3.2f,
        monthlyDistance = 48.6f,
        totalDistance = 1258.2f
    )
}

@Composable
fun HomeWalkDistanceStatusBox(
    todayDistance: Float,
    monthlyDistance: Float,
    totalDistance: Float
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .background(
                color = White,
                shape = MediumRoundShape
            )
            .border(
                width = ThinLineDp,
                color = Gray,
                shape = MediumRoundShape
            )
            .padding(
                vertical = 10.dp
            )
    ) {
        HomeWalkDistanceStatusItem(
            modifier = Modifier
                .weight(1f),
            iconResourceId = R.drawable.walk_icon,
            title = "오늘 걸은 거리",
            distance = todayDistance,
            textColor = DeepBlue
        )

        Divider(
            position = DividerPosition.Vertical,
            color = Gray
        )

        HomeWalkDistanceStatusItem(
            modifier = Modifier
                .weight(1f),
            iconResourceId = R.drawable.calendar_icon,
            title = "이번달 걸은 거리",
            distance = monthlyDistance,
            textColor = DarkPurple
        )

        Divider(
            position = DividerPosition.Vertical,
            color = Gray
        )

        HomeWalkDistanceStatusItem(
            modifier = Modifier
                .weight(1f),
            iconResourceId = R.drawable.trophy_icon,
            title = "총 걸은 거리",
            distance = totalDistance,
            textColor = DarkEmeraldGreen
        )
    }
}
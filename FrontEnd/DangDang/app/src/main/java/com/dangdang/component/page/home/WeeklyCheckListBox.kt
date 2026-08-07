package com.dangdang.component.page.home

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
import com.dangdang.common.utils.medium
import com.dangdang.common.utils.regular
import com.dangdang.data.model.home.WeeklyGlucoseCheckModel
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.Gray
import com.dangdang.ui.theme.White
import java.time.LocalDate

@Preview
@Composable
fun WeeklyCheckListBoxPreview() {
    WeeklyCheckListBox(
        weeklyGlucoseCheckList = listOf(
            WeeklyGlucoseCheckModel(
                dayOfWeek = "월",
                isGlucoseManagement = false
            ),
            WeeklyGlucoseCheckModel(
                dayOfWeek = "화",
                isGlucoseManagement = true
            ),
            WeeklyGlucoseCheckModel(
                dayOfWeek = "수",
                isGlucoseManagement = false
            ),
            WeeklyGlucoseCheckModel(
                dayOfWeek = "목",
                isGlucoseManagement = false
            ),
            WeeklyGlucoseCheckModel(
                dayOfWeek = "금",
                isGlucoseManagement = false
            ),
            WeeklyGlucoseCheckModel(
                dayOfWeek = "토",
                isGlucoseManagement = false
            ),
            WeeklyGlucoseCheckModel(
                dayOfWeek = "일",
                isGlucoseManagement = false
            )
        )
    )
}

@Composable
fun WeeklyCheckListBox(
    weeklyGlucoseCheckList: List<WeeklyGlucoseCheckModel>
){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = White,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = Gray,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "주간 혈당 관리 현황",
            style = AppTypography.bodyLarge.medium,
            color = Black,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val todayIndex = LocalDate.now().dayOfWeek.value - 1
            weeklyGlucoseCheckList.forEachIndexed { index, it ->
                WeeklyCheck(
                    isPast = index < todayIndex,
                    weeklyGlucoseCheck = it
                )
            }
        }
    }
}
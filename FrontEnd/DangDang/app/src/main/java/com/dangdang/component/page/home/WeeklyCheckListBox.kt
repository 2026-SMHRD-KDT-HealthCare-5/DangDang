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
import com.dangdang.data.enums.WeeklyAttendanceStatus
import com.dangdang.data.model.home.WeeklyGlucoseCheckModel
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.Gray
import com.dangdang.ui.theme.MediumRoundShape
import com.dangdang.ui.theme.ThinLineDp
import com.dangdang.ui.theme.White
import java.time.LocalDate

@Preview
@Composable
fun WeeklyCheckListBoxPreview() {
    WeeklyCheckListBox(
        weeklyGlucoseCheckList = listOf(
            WeeklyGlucoseCheckModel(
                day = "월",
                status = WeeklyAttendanceStatus.MISSED.name
            ),
            WeeklyGlucoseCheckModel(
                day = "화",
                status = WeeklyAttendanceStatus.DONE.name
            ),
            WeeklyGlucoseCheckModel(
                day = "수",
                status = WeeklyAttendanceStatus.NONE.name
            ),
            WeeklyGlucoseCheckModel(
                day = "목",
                status = WeeklyAttendanceStatus.NONE.name
            ),
            WeeklyGlucoseCheckModel(
                day = "금",
                status = WeeklyAttendanceStatus.NONE.name
            ),
            WeeklyGlucoseCheckModel(
                day = "토",
                status = WeeklyAttendanceStatus.NONE.name
            ),
            WeeklyGlucoseCheckModel(
                day = "일",
                status = WeeklyAttendanceStatus.NONE.name
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
                shape = MediumRoundShape
            )
            .border(
                width = ThinLineDp,
                color = Gray,
                shape = MediumRoundShape
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "주간 걷기 미션 달성",
            style = AppTypography.bodyLarge.medium,
            color = Black,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weeklyGlucoseCheckList.forEach {
                WeeklyCheck(
                    weeklyGlucoseCheck = it
                )
            }
        }
    }
}
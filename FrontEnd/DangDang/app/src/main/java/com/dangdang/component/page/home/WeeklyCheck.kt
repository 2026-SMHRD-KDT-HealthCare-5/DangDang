package com.dangdang.component.page.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.R
import com.dangdang.common.utils.regular
import com.dangdang.data.enums.WeeklyAttendanceStatus
import com.dangdang.data.model.home.WeeklyGlucoseCheckModel
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.Gray
import com.dangdang.ui.theme.MediumLineDp
import com.dangdang.ui.theme.White

@Preview
@Composable
fun WeeklyCheckPreview(

){
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        WeeklyCheck(
            weeklyGlucoseCheck = WeeklyGlucoseCheckModel(
                day = "월",
                status = WeeklyAttendanceStatus.MISSED.name
            )
        )

        WeeklyCheck(
            weeklyGlucoseCheck = WeeklyGlucoseCheckModel(
                day = "화",
                status = WeeklyAttendanceStatus.DONE.name
            )
        )

        WeeklyCheck(
            weeklyGlucoseCheck = WeeklyGlucoseCheckModel(
                day = "수",
                status = WeeklyAttendanceStatus.NONE.name
            )
        )
    }
}

@Composable
fun WeeklyCheck(
    weeklyGlucoseCheck: WeeklyGlucoseCheckModel
){
    val status = WeeklyAttendanceStatus.valueOf(weeklyGlucoseCheck.status)
    Column(
        modifier = Modifier
            .background(
                color = White
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = weeklyGlucoseCheck.day,
            style = AppTypography.labelMedium.regular,
            color = Black,
        )

        if(status == WeeklyAttendanceStatus.NONE){
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = White,
                        shape = CircleShape
                    )
                    .border(
                        width = MediumLineDp,
                        color = Gray,
                        shape = CircleShape
                    )
            )
        }else{
            Image(
                painter = painterResource(
                    if(status == WeeklyAttendanceStatus.DONE){
                        R.drawable.check_round_green
                    }else{
                        R.drawable.wrong_round_red
                    }
                ),
                contentDescription = "check",
                modifier = Modifier
                    .size(24.dp)
            )
        }
    }
}
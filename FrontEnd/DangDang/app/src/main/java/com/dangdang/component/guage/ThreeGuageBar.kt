package com.dangdang.component.guage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.data.enums.GuageBarSize
import com.dangdang.ui.theme.BlackOpacity25
import com.dangdang.ui.theme.DarkGray
import com.dangdang.ui.theme.DarkGreen
import com.dangdang.ui.theme.White

@Preview
@Composable
fun ThreeGuageBarPreview(){
    ThreeGuageBar(
        current = 0.85f,
        target = 2.6f,
        teamCurrent = 1.2f
    )
}

@Composable
fun ThreeGuageBar(
    current: Float,
    target: Float,
    teamCurrent: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .background(
                color = BlackOpacity25,
                shape = RoundedCornerShape(100.dp)
            )
    ) {
        //팀 전체 게이지
        Box(
            modifier = Modifier
                .fillMaxWidth(teamCurrent/target)
                .fillMaxHeight()
                .background(
                    color = White,
                    shape = RoundedCornerShape(100.dp)
                )
        )

        //내 게이지
        Box(
            modifier = Modifier
                .fillMaxWidth(current/target)
                .fillMaxHeight()
                .background(
                    color = DarkGreen,
                    shape = RoundedCornerShape(100.dp)
                )
        )
    }
}
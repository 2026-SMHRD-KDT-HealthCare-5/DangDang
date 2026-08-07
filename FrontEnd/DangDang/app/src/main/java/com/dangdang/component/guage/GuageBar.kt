package com.dangdang.component.guage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.dangdang.ui.theme.PrimaryBlue

@Preview
@Composable
fun GuageBarPreview(){
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        GuageBar(
            size = GuageBarSize.Large,
            guageColor = PrimaryBlue,
            current = 0.5f,
            target = 1f
        )

        GuageBar(
            size = GuageBarSize.Small,
            guageColor = PrimaryBlue,
            current = 0.3f,
            target = 1f
        )
    }
}

@Composable
fun GuageBar(
    size: GuageBarSize,
    guageColor: Color,
    current: Float,
    target: Float,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(size.height)
            .background(
                color = BlackOpacity25,
                shape = RoundedCornerShape(100.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(current/target)
                .height(size.height)
                .background(
                    color = guageColor,
                    shape = RoundedCornerShape(100.dp)
                )
        )
    }
}
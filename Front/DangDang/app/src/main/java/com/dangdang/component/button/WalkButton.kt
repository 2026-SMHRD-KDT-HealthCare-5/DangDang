package com.dangdang.component.button

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.common.utils.medium
import com.dangdang.common.utils.regular
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Navy
import com.dangdang.ui.theme.White

@Preview
@Composable
fun WalkButtonPreview(){
    WalkButton(
        isWalking = false,
        onClick = {}
    )
}

@Composable
fun WalkButton(
    isWalking: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .size(100.dp)
            .background(
                color = Navy,
                shape = CircleShape
            )
            .clickable(
                onClick = onClick
            )
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = if(isWalking) "■" else "▶",
            style = AppTypography.bodyLarge.medium,
            color = White
        )

        Text(
            text = if(isWalking) "걷기 종료" else "걷기 시작",
            style = AppTypography.bodyLarge.medium,
            color = White
        )
    }
}
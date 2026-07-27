package com.dangdang.component.button

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dangdang.common.utils.componentWidthModifier
import com.dangdang.common.utils.regular
import com.dangdang.data.enums.LayoutSize
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.PrimaryBlue
import com.dangdang.ui.theme.White

@Preview
@Composable
fun PrimaryButtonPreview(

){
    Column {
        PrimaryButton(
            text = "Button",
            enabled = true,
            fixWidth = 100.dp,
            sizeType = LayoutSize.FixSize,
            onClick = {}
        )
        Spacer(Modifier.height(10.dp))
        PrimaryButton(
            text = "Disable",
            enabled = false,
            onClick = {}
        )
    }
}

@Composable
fun PrimaryButton(
    text: String,
    enabled: Boolean = true,
    fixWidth: Dp? = null,
    sizeType: LayoutSize = LayoutSize.DefaultSize,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                color = if(enabled) PrimaryBlue else Color.Gray,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
            .then(
                componentWidthModifier(
                    fixWidth = fixWidth,
                    sizeType = sizeType
                )
            )
            .clickable(
                enabled = enabled,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = AppTypography.bodyLarge.regular,
            color = White
        )
    }
}
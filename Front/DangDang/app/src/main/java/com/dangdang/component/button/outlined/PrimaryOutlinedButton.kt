package com.dangdang.component.button.outlined

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dangdang.R
import com.dangdang.common.utils.componentWidthModifier
import com.dangdang.common.utils.medium
import com.dangdang.common.utils.regular
import com.dangdang.component.button.PrimaryButton
import com.dangdang.data.enums.LayoutSize
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Navy
import com.dangdang.ui.theme.PrimaryBlue
import com.dangdang.ui.theme.SkyBlueOpacity30
import com.dangdang.ui.theme.White

@Preview
@Composable
fun PrimaryOutlinedButtonPreview(

){
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        PrimaryOutlinedButton(
            leftIcon = {
                Icon(
                    painter = painterResource(R.mipmap.kakao_login),
                    contentDescription = "left icon",
                    modifier = Modifier.size(24.dp)
                )
            },
            rightIcon = {
                Icon(
                    painter = painterResource(R.mipmap.kakao_login),
                    contentDescription = "right icon",
                    modifier = Modifier.size(24.dp)
                )
            },
            text = "Button",
            enabled = true,
            fixWidth = 100.dp,
            sizeType = LayoutSize.FixSize,
            onClick = {}
        )
        PrimaryOutlinedButton(
            text = "Disable",
            enabled = false,
            onClick = {}
        )
        PrimaryOutlinedButton(
            text = "Selected",
            enabled = true,
            selected = true,
            onClick = {}
        )
    }
}

@Composable
fun PrimaryOutlinedButton(
    text: String,
    color: Color = Navy,
    leftIcon: @Composable () -> Unit = {},
    rightIcon: @Composable () -> Unit = {},
    enabled: Boolean = true,
    selected: Boolean = false,
    fixWidth: Dp? = null,
    sizeType: LayoutSize = LayoutSize.DefaultSize,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .background(
                color = if(selected) SkyBlueOpacity30 else if(enabled) White else Color.Gray,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = color,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                enabled = enabled,
                onClick = onClick,
            )
            .padding(12.dp)
            .componentWidthModifier(
                fixWidth = fixWidth,
                sizeType = sizeType
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
    ) {
        leftIcon()

        Text(
            text = text,
            style = AppTypography.labelLarge.medium,
            color = color
        )

        rightIcon()
    }
}
package com.dangdang.component.divider

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dangdang.common.utils.dividerHeightModifier
import com.dangdang.common.utils.dividerWidthModifier
import com.dangdang.data.enums.DividerPosition
import com.dangdang.ui.theme.LightGray
import com.dangdang.ui.theme.ThinLineDp

@Preview
@Composable
fun DividerPreview(

){
    Column{
        Divider(
            position = DividerPosition.Vertical,
            //선택 파라미터
            size = 10.dp,
            color = LightGray
        )

        Spacer(Modifier.height(10.dp))

        Divider(
            position = DividerPosition.Horizontal,
            size = 10.dp,
            color = LightGray
        )
    }
}

@Composable
fun Divider(
    position: DividerPosition,
    size: Dp? = null,
    color: Color = LightGray
){


    Box(
        Modifier
            .background(color)
            .dividerWidthModifier(
                position = position,
                size = size
            )
            .dividerHeightModifier(
                position = position,
                size = size
            )
    ){

    }
}
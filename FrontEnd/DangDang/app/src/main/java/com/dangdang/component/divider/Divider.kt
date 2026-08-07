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
import com.dangdang.data.enums.DividerPosition
import com.dangdang.ui.theme.LightGray

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
    val dividerWidthModifier =
        if(position == DividerPosition.Horizontal){
            if(size != null){
                Modifier.width(size)
            }else{
                Modifier.fillMaxWidth()
            }
        }else{
            Modifier.width(1.dp)
        }

    val dividerHeightModifier =
        if(position == DividerPosition.Vertical){
            if(size != null){
                Modifier.height(size)
            }else{
                Modifier.fillMaxHeight()
            }
        }else{
            Modifier.height(1.dp)
        }

    Box(
        Modifier
            .background(color)
            .then(dividerWidthModifier)
            .then(dividerHeightModifier)
    ){

    }
}
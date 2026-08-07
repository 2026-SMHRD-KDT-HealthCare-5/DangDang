package com.dangdang.component.toggle

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.R
import com.dangdang.ui.theme.Gray
import com.dangdang.ui.theme.PrimaryBlue
import com.dangdang.ui.theme.White

@Preview
@Composable
fun CheckBoxPreview(){
    Column(

    ) {
        CheckBox(
            checked = true,
            onCheckedChange = {}
        )
        CheckBox(
            checked = false,
            onCheckedChange = {}
        )
    }
}

@Composable
fun CheckBox(
    checked: Boolean,
    onCheckedChange: () -> Unit
){
    Box(
        modifier = Modifier
            .size(24.dp)
            .background(
                color = White,
                shape = RoundedCornerShape(4.dp)
            )
            .border(
                width = 2.dp,
                color = if(checked) PrimaryBlue else Gray,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(
                onClick = onCheckedChange
            ),
        contentAlignment = Alignment.Center
    ){
        if(checked){
            Icon(
                painter = painterResource(id = R.mipmap.check),
                contentDescription = "체크 아이콘",
                tint = PrimaryBlue,
                modifier = Modifier
                    .size(12.dp)
            )
        }
    }
}
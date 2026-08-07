package com.dangdang.component.toggle

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import com.dangdang.ui.theme.Navy
import com.dangdang.ui.theme.PrimaryBlue
import com.dangdang.ui.theme.White

@Preview
@Composable
fun RadioButtonPreview() {
    Column(

    ) {
        RadioButton(
            checked = true,
            onCheckedChange = {}
        )
        RadioButton(
            checked = false,
            onCheckedChange = {}
        )
    }
}

@Composable
fun RadioButton(
    checked: Boolean,
    onCheckedChange: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .background(
                color = White,
                shape = CircleShape
            )
            .border(
                width = 3.dp,
                color = if(checked) Navy else Gray,
                shape = CircleShape
            )
            .clickable(
                onClick = onCheckedChange
            ),
        contentAlignment = Alignment.Center
    ){
        if(checked){
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = Navy,
                        shape = CircleShape
                    )
            )
        }
    }
}
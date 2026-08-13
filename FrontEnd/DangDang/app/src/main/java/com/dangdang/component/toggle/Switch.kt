package com.dangdang.component.toggle

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.ui.theme.CapsuleRoundShape
import com.dangdang.ui.theme.Gray
import com.dangdang.ui.theme.Navy
import com.dangdang.ui.theme.White

@Preview
@Composable
fun SwitchPreview(

){
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Switch(
            isCheck = true,
            onCheckChange = {}
        )

        Switch(
            isCheck = false,
            onCheckChange = {}
        )
    }
}

@Composable
fun Switch(
    isCheck: Boolean,
    onCheckChange: () -> Unit
){
    Row(
        modifier = Modifier
            .width(48.dp)
            .background(
                color = if(isCheck) Navy else Gray,
                shape = CapsuleRoundShape
            )
            .clickable(
                onClick = onCheckChange
            )
            .padding(3.dp),
        horizontalArrangement = if(isCheck) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .background(
                    color = White,
                    shape = CircleShape
                )
        )
    }
}
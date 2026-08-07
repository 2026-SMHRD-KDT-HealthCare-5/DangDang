package com.dangdang.component.text.textfield

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.common.utils.medium
import com.dangdang.common.utils.regular
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.Red
import com.dangdang.ui.theme.White

@Preview
@Composable
fun TextFieldTitlePreview(

){
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TextFieldTitle(
            title = "타이틀",
            isRequired = true
        )

        TextFieldTitle(
            title = "타이틀",
            isRequired = false
        )
    }
}

@Composable
fun TextFieldTitle(
    title: String? = null,
    isRequired: Boolean = true,
){
    Row(
        modifier = Modifier
            .background(White),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title?:"",
            style = AppTypography.labelLarge.medium,
            color = Black
        )

        if(isRequired){
            Text(
                text = "*",
                style = AppTypography.labelLarge.medium,
                color = Red
            )
        }
    }
}
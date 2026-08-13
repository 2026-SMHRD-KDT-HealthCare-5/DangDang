package com.dangdang.component.page.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.common.utils.medium
import com.dangdang.component.button.outlined.PrimaryOutlinedButton
import com.dangdang.data.enums.Gender
import com.dangdang.data.enums.LayoutSize
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.Gray
import com.dangdang.ui.theme.Navy
import com.dangdang.ui.theme.White

@Preview
@Composable
fun GenderCheckViewPreview(

){
    GenderCheckView(
        gender = Gender.male,
        onGenderChange = {}
    )
}

@Composable
fun GenderCheckView(
    gender: Gender,
    onGenderChange: (Gender) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = White
            ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "성별",
            style = AppTypography.labelLarge.medium,
            color = Black,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
            ){
                val selected = gender == Gender.male
                PrimaryOutlinedButton(
                    text = "남성",
                    selected = selected,
                    color = if(selected) Navy else Gray,
                    sizeType = LayoutSize.FillMaxSize,
                    onClick = {
                        onGenderChange(Gender.male)
                    }
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
            ){
                val selected = gender == Gender.female
                PrimaryOutlinedButton(
                    text = "여성",
                    selected = selected,
                    color = if(selected) Navy else Gray,
                    sizeType = LayoutSize.FillMaxSize,
                    onClick = {
                        onGenderChange(Gender.female)
                    }
                )
            }
        }
    }
}
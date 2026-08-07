package com.dangdang.component.toggle

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.R
import com.dangdang.common.utils.medium
import com.dangdang.common.utils.regular
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.Gray
import com.dangdang.ui.theme.PrimaryBlue
import com.dangdang.ui.theme.SkyBlueOpacity30
import com.dangdang.ui.theme.White

@Preview
@Composable
fun RadioSelectBoxPreview() {
    Column(

    ) {
        RadioSelectBox(
            checked = true,
            onCheckedChange = {},
            titleIconResourceId = R.mipmap.password_visible,
            title = "거의 안함",
            description = "운동을 거의 하지 않아요"
        )
        RadioSelectBox(
            checked = false,
            onCheckedChange = {},
            titleIconResourceId = R.mipmap.password_visible,
            title = "거의 안함",
            description = "운동을 거의 하지 않아요"
        )
    }
}

@Composable
fun RadioSelectBox(
    checked: Boolean,
    onCheckedChange: () -> Unit,
    titleIconResourceId: Int,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if(checked) SkyBlueOpacity30 else White,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = Gray,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                onClick = onCheckedChange
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        RadioButton(
            checked = checked,
            onCheckedChange = onCheckedChange
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ){
                Icon(
                    painter = painterResource(id = titleIconResourceId),
                    contentDescription = "제목 아이콘",
                    modifier = Modifier
                        .size(24.dp)
                )
                Text(
                    text = title,
                    style = AppTypography.labelLarge.medium,
                    color = if(checked) PrimaryBlue else Black
                )
            }

            Text(
                text = description,
                style = AppTypography.labelMedium.regular,
                color = Gray
            )
        }
    }
}
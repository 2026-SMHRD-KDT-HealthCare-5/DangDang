package com.dangdang.component.button

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.R
import com.dangdang.common.utils.regular
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.White

@Preview
@Composable
fun MoreButtonPreview(

){
    MoreButton(
        onClick = {}
    )
}

@Composable
fun MoreButton(
    onClick: () -> Unit
){
    Row(
        modifier = Modifier
            .background(
                color = White
            )
            .clickable(
                onClick = onClick
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.plus),
            contentDescription = "더보기 아이콘",
            modifier = Modifier
                .size(24.dp)
        )

        Text(
            text = "더보기",
            style = AppTypography.labelLarge.regular,
            color = Black
        )
    }
}
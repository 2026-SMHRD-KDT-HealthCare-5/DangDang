package com.dangdang.component.page.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.R
import com.dangdang.common.utils.addComma
import com.dangdang.common.utils.medium
import com.dangdang.common.utils.regular
import com.dangdang.common.utils.screen
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.DarkPurple

@Preview
@Composable
fun HomeWalkDistanceStatusItemPreview(
){
    HomeWalkDistanceStatusItem(
        iconResourceId = R.drawable.walk_icon,
        title = "오늘 걸은 거리",
        distance = 1258.2f,
        textColor = DarkPurple
    )
}

@Composable
fun HomeWalkDistanceStatusItem(
    modifier: Modifier = Modifier,
    iconResourceId: Int,
    title: String,
    distance: Float,
    textColor: Color
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(
                color = Color.White
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            Image(
                painter = painterResource(iconResourceId),
                contentDescription = null,
                modifier = Modifier
                    .size(20.dp)
            )

            Text(
                text = title,
                style = AppTypography.labelSmall.regular,
                color = Black,
            )
        }

        Row(
            modifier = Modifier
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        AppTypography.titleLarge.regular.toSpanStyle()
                    ) {
                        append(addComma(distance))
                    }
                    withStyle(
                        AppTypography.labelSmall.regular.toSpanStyle()
                    ) {
                        append(" km")
                    }
                },
                color = textColor
            )
        }
    }
}
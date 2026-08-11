package com.dangdang.component.navigation.topnavigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.R
import com.dangdang.common.utils.medium
import com.dangdang.common.utils.regular
import com.dangdang.component.divider.Divider
import com.dangdang.data.enums.BackgroundType
import com.dangdang.data.enums.DividerPosition
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.PrimaryBlue
import com.dangdang.ui.theme.White

@Preview
@Composable
fun TopNavigationPreview(

){
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TopNavigation(
            isBackButton = true,
            onBackClick = {},
            title = "팀 검색 / 가입",
            backgroundType = BackgroundType.Blue
        )
        TopNavigation(
            isBackButton = false,
            onBackClick = {},
            title = "마이페이지",
            backgroundType = BackgroundType.Blue
        )
        TopNavigation(
            isBackButton = true,
            onBackClick = {},
            title = "회원가입",
            backgroundType = BackgroundType.White
        )
    }
}

@Composable
fun TopNavigation(
    isBackButton: Boolean = false,
    onBackClick: ()-> Unit = {},
    title: String,
    backgroundType: BackgroundType = BackgroundType.Blue
){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = when(backgroundType){
                    BackgroundType.Blue -> PrimaryBlue
                    BackgroundType.White -> White
                }
            )
    ) {
        Box(
            modifier = Modifier
                .padding(12.dp),
            contentAlignment = Alignment.CenterStart
        ){
            Text(
                text = title,
                style = AppTypography.bodyLarge.medium,
                color = when(backgroundType){
                    BackgroundType.Blue -> White
                    BackgroundType.White -> Black
                },
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            if(isBackButton){
                Icon(
                    painter = painterResource(R.drawable.arrow_back),
                    contentDescription = "left icon",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable(
                            onClick = onBackClick
                        ),
                    tint = when(backgroundType){
                        BackgroundType.Blue -> White
                        BackgroundType.White -> Black
                    }
                )
            }
        }

        if(backgroundType == BackgroundType.White){
            Divider(
                position = DividerPosition.Horizontal,
            )
        }
    }
}
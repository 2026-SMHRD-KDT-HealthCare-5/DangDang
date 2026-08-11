package com.dangdang.component.navigation.bottomnavigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.dangdang.R
import com.dangdang.common.utils.regular
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.Navy
import com.dangdang.ui.theme.White

@Preview
@Composable
fun BottomNaviMenuPreview(

){
    Row{
        BottomNaviMenu(
            isSelected = false,
            name = "홈",
            enableIcon = R.drawable.home_blue,
            disableIcon = R.drawable.home_black
        )

        Spacer(Modifier.width(10.dp))

        BottomNaviMenu(
            isSelected = true,
            name = "홈",
            enableIcon = R.drawable.home_blue,
            disableIcon = R.drawable.home_black
        )
    }
}

@Composable
fun BottomNaviMenu(
    isSelected: Boolean,
    name: String,
    enableIcon: Int,
    disableIcon: Int
){
    Column(
        modifier = Modifier
            .background(White),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Image(
            painter = painterResource(id = if(isSelected) enableIcon else disableIcon),
            contentDescription = "BottomNavi 아이콘",
            modifier = Modifier.size(24.dp)
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = name,
            style = AppTypography.labelMedium.regular,
            color = if(isSelected) Navy else Black
        )
    }
}

package com.dangdang.component.navigation.bottomnavigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.common.utils.MainRoute
import com.dangdang.component.divider.Divider
import com.dangdang.data.enums.DividerPosition
import com.dangdang.ui.theme.White

@Preview
@Composable
fun BottomNavigationPreview(

){
    Column(
        modifier = Modifier
            .width(360.dp)
            .height(600.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ){
        MainRoute.values.forEach { mainRoute ->
            BottomNavigation(
                currentRoute = mainRoute,
                onItemClick = {}
            )
        }
    }
}

@Composable
fun BottomNavigation(
    currentRoute: MainRoute?,
    onItemClick: (MainRoute) -> Unit
){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(White),
    ) {
        Divider(
            position = DividerPosition.Horizontal,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
        ){
            MainRoute.values.forEach { mainRouteItem ->
                val isSelected = currentRoute?.name == mainRouteItem.name
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            enabled = !isSelected,
                            onClick = {
                                onItemClick(mainRouteItem)
                            }
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ){
                    BottomNaviMenu(
                        isSelected = isSelected,
                        name = mainRouteItem.name,
                        enableIcon = mainRouteItem.enableIcon,
                        disableIcon = mainRouteItem.disableIcon
                    )
                }
            }
        }
    }
}
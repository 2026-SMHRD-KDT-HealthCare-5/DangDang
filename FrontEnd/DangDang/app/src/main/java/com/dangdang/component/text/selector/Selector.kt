package com.dangdang.component.text.selector

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.dangdang.R
import com.dangdang.common.utils.bold
import com.dangdang.common.utils.medium
import com.dangdang.common.utils.regular
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.Gray
import com.dangdang.ui.theme.White

@Preview
@Composable
fun SelectorPreview() {
    var selected by remember {
        mutableStateOf("일이삼사오육칠팔구십일이삼사오육칠팔구십")
    }

    Column(
        Modifier
            .height(500.dp)
            .background(
                color = White
            )
    ){
        Selector(
            title = "타이틀",
            items = listOf(
                "Apple",
                "Banana",
                "Orange",
                "Melon"
            ),
            selectedItem = selected,
            itemText = { it },
            onSelected = {
                selected = it
            }
        )

        Text(
            text = "다른 텍스트",
            style = AppTypography.bodyLarge.bold,
            color = Black,
        )
    }
}

@Composable
fun <T> Selector(
    title: String,
    items: List<T>,
    selectedItem: T,
    itemText: (T) -> String,
    onSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    var anchorBounds by remember {
        mutableStateOf(IntRect.Zero)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = White
            ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ){
        Text(
            text = title,
            style = AppTypography.labelLarge.medium,
            color = Black,
        )

        Box(
            Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .background(
                        color = White,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        1.dp,
                        Gray,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        expanded = !expanded
                    }
                    .onGloballyPositioned {
                        val position = it.positionInParent()

                        anchorBounds = IntRect(
                            position.x.toInt(),
                            position.y.toInt(),
                            (position.x + it.size.width).toInt(),
                            (position.y + it.size.height).toInt()
                        )
                    }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = itemText(selectedItem),
                    modifier = Modifier.weight(1f),
                    style = AppTypography.labelLarge.regular,
                )

                Icon(
                    painter = painterResource(
                        if (expanded) R.mipmap.up_icon
                        else R.mipmap.down
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
            }

            if (expanded) {
                Popup(
                    alignment = Alignment.TopStart,
                    offset = IntOffset(
                        anchorBounds.left,
                        anchorBounds.bottom
                    ),
                    onDismissRequest = {
                        expanded = false
                    },
                    properties = PopupProperties(focusable = true)
                ) {
                    Card(
                        modifier = Modifier
                            .offset(y = (-17).dp)
                            .width(
                                with(LocalDensity.current) {
                                    anchorBounds.width.toDp()
                                }
                            ),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        items.forEach {
                            Text(
                                text = itemText(it),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelected(it)
                                        expanded = false
                                    }
                                    .padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
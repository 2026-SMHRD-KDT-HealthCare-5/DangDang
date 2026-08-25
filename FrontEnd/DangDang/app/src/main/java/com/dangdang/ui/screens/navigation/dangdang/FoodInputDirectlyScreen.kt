package com.dangdang.ui.screens.navigation.dangdang

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dangdang.common.utils.mainScreen
import com.dangdang.common.utils.regular
import com.dangdang.component.button.PrimaryButton
import com.dangdang.component.chat.AIChatView
import com.dangdang.component.chat.FoodInputDirectlyBox
import com.dangdang.component.navigation.topnavigation.TopNavigation
import com.dangdang.data.enums.LayoutSize
import com.dangdang.data.model.chat.FoodInputDirectlyForm
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.DarkGray
import com.dangdang.ui.theme.GrayOpacity30
import java.time.LocalDateTime

@Preview
@Composable
fun FoodInputDirectlyScreenPreview(){
    FoodInputDirectlyScreenContent(
        foodInputDirectlyForm = FoodInputDirectlyForm(
            foodName = "",
            servingSize = "",
            calorie = "",
            carb = "",
            sugar = "",
            fiber = "",
            protein = "",
            fat = ""
        ),
        onFormChange = {},
        onSendClick = {}
    )
}

@Composable
fun FoodInputDirectlyScreen(
    navController: NavController,
) {
    var foodInputDirectlyForm by remember {
        mutableStateOf(FoodInputDirectlyForm(
            foodName = "",
            servingSize = "",
            calorie = "",
            carb = "",
            sugar = "",
            fiber = "",
            protein = "",
            fat = ""
        ))
    }

    FoodInputDirectlyScreenContent(
        foodInputDirectlyForm = foodInputDirectlyForm,
        onFormChange = {
            foodInputDirectlyForm = it
        },
        onSendClick = {
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set("isFoodInputDirectlySend", true)
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set("foodInputDirectlyForm", foodInputDirectlyForm)
            navController.popBackStack()
        }
    )
}

@Composable
fun FoodInputDirectlyScreenContent(
    foodInputDirectlyForm: FoodInputDirectlyForm,
    onFormChange: (FoodInputDirectlyForm) -> Unit,
    onSendClick: () -> Unit
){
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .mainScreen()
            .imePadding()
    ) {
        TopNavigation(
            title = "AI 건강 비서 당당이"
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(
                    horizontal = 8.dp,
                    vertical = 15.dp
                ),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            AIChatView(
                message = "음식 이름과 영양성분을\n" +
                        "입력해 주세요.",
                sendTime = LocalDateTime.now()
            )

            FoodInputDirectlyBox(
                foodInputDirectlyForm = foodInputDirectlyForm,
                onFormChange = onFormChange
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = GrayOpacity30,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ){
                Text(
                    text = "모르는 항목은 비워두셔도 돼요.",
                    style = AppTypography.bodyLarge.regular,
                    color = DarkGray,
                )
            }

            PrimaryButton(
                text = "저장하고 확인하기",
                sizeType = LayoutSize.FillMaxSize,
                onClick = onSendClick
            )
        }
    }
}
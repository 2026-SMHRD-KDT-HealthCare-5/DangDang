package com.dangdang.component.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.common.utils.regular
import com.dangdang.component.text.textfield.TextField
import com.dangdang.data.enums.LayoutSize
import com.dangdang.data.model.chat.FoodInputDirectlyForm
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.Gray
import com.dangdang.ui.theme.White

@Preview
@Composable
fun FoodInputDirectlyBoxPreview() {
    FoodInputDirectlyBox(
        foodInputDirectlyForm = FoodInputDirectlyForm(
            name = "",
            intake = "",
            kcal = "",
            carbohydrate = "",
            dietaryFiber = "",
            protein = "",
            fat = "",
            sugar = ""
        ),
        onFormChange = {}
    )
}

@Composable
fun FoodInputDirectlyBox(
    foodInputDirectlyForm: FoodInputDirectlyForm,
    onFormChange: (FoodInputDirectlyForm) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = White
            ),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        TextField(
            title = "음식 이름",
            isMaxLengthView = false,
            isRequired = false,
            value = foodInputDirectlyForm.name,
            onValueChange = {
                onFormChange(
                    foodInputDirectlyForm.copy(
                        name = it
                    )
                )
            },
            placeholderText = "예) 두부김치",
            maxLength = 50,
            sizeType = LayoutSize.FillMaxSize
        )

        TextField(
            title = "섭취량(g)",
            isMaxLengthView = false,
            isRequired = false,
            value = foodInputDirectlyForm.intake,
            onValueChange = {
                onFormChange(
                    foodInputDirectlyForm.copy(
                        intake = it
                    )
                )
            },
            rightIcon = {
                Text(
                    text = "g",
                    style = AppTypography.labelLarge.regular,
                    color = Gray,
                )
            },
            keyboardType = KeyboardType.Number,
            placeholderText = "예) 200",
            maxLength = 5,
            sizeType = LayoutSize.FillMaxSize
        )

        TextField(
            title = "칼로리(kcal)",
            isMaxLengthView = false,
            isRequired = false,
            value = foodInputDirectlyForm.kcal,
            onValueChange = {
                onFormChange(
                    foodInputDirectlyForm.copy(
                        kcal = it
                    )
                )
            },
            rightIcon = {
                Text(
                    text = "kcal",
                    style = AppTypography.labelLarge.regular,
                    color = Gray,
                )
            },
            keyboardType = KeyboardType.Number,
            placeholderText = "예) 250",
            maxLength = 5,
            sizeType = LayoutSize.FillMaxSize
        )

        TextField(
            title = "탄수화물(g)",
            isMaxLengthView = false,
            isRequired = false,
            value = foodInputDirectlyForm.carbohydrate,
            onValueChange = {
                onFormChange(
                    foodInputDirectlyForm.copy(
                        carbohydrate = it
                    )
                )
            },
            rightIcon = {
                Text(
                    text = "g",
                    style = AppTypography.labelLarge.regular,
                    color = Gray,
                )
            },
            keyboardType = KeyboardType.Number,
            placeholderText = "예) 300",
            maxLength = 5,
            sizeType = LayoutSize.FillMaxSize
        )

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
            ){
                TextField(
                    title = "당류(g)",
                    isMaxLengthView = false,
                    isRequired = false,
                    value = foodInputDirectlyForm.sugar,
                    onValueChange = {
                        onFormChange(
                            foodInputDirectlyForm.copy(
                                sugar = it
                            )
                        )
                    },
                    rightIcon = {
                        Text(
                            text = "g",
                            style = AppTypography.labelLarge.regular,
                            color = Gray,
                        )
                    },
                    keyboardType = KeyboardType.Number,
                    placeholderText = "예) 15",
                    maxLength = 5,
                    sizeType = LayoutSize.FillMaxSize
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
            ){
                TextField(
                    title = "식이섬유(g)",
                    isMaxLengthView = false,
                    isRequired = false,
                    value = foodInputDirectlyForm.dietaryFiber,
                    onValueChange = {
                        onFormChange(
                            foodInputDirectlyForm.copy(
                                dietaryFiber = it
                            )
                        )
                    },
                    rightIcon = {
                        Text(
                            text = "g",
                            style = AppTypography.labelLarge.regular,
                            color = Gray,
                        )
                    },
                    keyboardType = KeyboardType.Number,
                    placeholderText = "예) 3",
                    maxLength = 5,
                    sizeType = LayoutSize.FillMaxSize
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
            ){
                TextField(
                    title = "지방(g)",
                    isMaxLengthView = false,
                    isRequired = false,
                    value = foodInputDirectlyForm.fat,
                    onValueChange = {
                        onFormChange(
                            foodInputDirectlyForm.copy(
                                fat = it
                            )
                        )
                    },
                    rightIcon = {
                        Text(
                            text = "g",
                            style = AppTypography.labelLarge.regular,
                            color = Gray,
                        )
                    },
                    keyboardType = KeyboardType.Number,
                    placeholderText = "예) 10",
                    maxLength = 5,
                    sizeType = LayoutSize.FillMaxSize
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
            ){
                TextField(
                    title = "단백질(g)",
                    isMaxLengthView = false,
                    isRequired = false,
                    value = foodInputDirectlyForm.protein,
                    onValueChange = {
                        onFormChange(
                            foodInputDirectlyForm.copy(
                                protein = it
                            )
                        )
                    },
                    rightIcon = {
                        Text(
                            text = "g",
                            style = AppTypography.labelLarge.regular,
                            color = Gray,
                        )
                    },
                    keyboardType = KeyboardType.Number,
                    placeholderText = "예) 15",
                    maxLength = 5,
                    sizeType = LayoutSize.FillMaxSize
                )
            }
        }
    }
}
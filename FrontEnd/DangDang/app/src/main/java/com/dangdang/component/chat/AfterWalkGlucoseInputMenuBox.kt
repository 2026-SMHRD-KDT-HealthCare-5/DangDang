package com.dangdang.component.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.common.utils.bold
import com.dangdang.common.utils.isValidPostPrandialGlucose
import com.dangdang.common.utils.regular
import com.dangdang.component.button.PrimaryButton
import com.dangdang.component.text.textfield.TextField
import com.dangdang.data.enums.LayoutSize
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.ForestGreen
import com.dangdang.ui.theme.Gray
import com.dangdang.ui.theme.White

@Preview
@Composable
fun AfterWalkGlucoseInputMenuBoxPreview() {
    AfterWalkGlucoseInputMenuBox(
        afterWalkGlucoseValue = "",
        onAfterWalkGlucoseValueChange = {},
        onGlucoseInputCompleteClick = {}
    )
}

@Composable
fun AfterWalkGlucoseInputMenuBox(
    afterWalkGlucoseValue: String,
    onAfterWalkGlucoseValueChange: (String) -> Unit,
    onGlucoseInputCompleteClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = White
            ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TextField(
            title = "식후 혈당(mg/dL)",
            isMaxLengthView = false,
            isRequired = false,
            rightIcon = {
                Text(
                    text = "mg/dL",
                    style = AppTypography.labelLarge.regular,
                    color = Gray,
                )
            },
            keyboardType = KeyboardType.Number,
            value = afterWalkGlucoseValue,
            onValueChange = onAfterWalkGlucoseValueChange,
            placeholderText = "예) 165(범위 : 50~500)",
            maxLength = 5,
            sizeType = LayoutSize.FillMaxSize
        )

        PrimaryButton(
            text = "기록 저장하기",
            enabled = afterWalkGlucoseValue.isNotEmpty() &&
                    isValidPostPrandialGlucose(afterWalkGlucoseValue),
            sizeType = LayoutSize.FillMaxSize,
            onClick = onGlucoseInputCompleteClick
        )
    }
}
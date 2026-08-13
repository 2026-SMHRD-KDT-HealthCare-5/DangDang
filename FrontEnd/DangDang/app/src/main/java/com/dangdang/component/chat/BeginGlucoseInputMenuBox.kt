package com.dangdang.component.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.common.utils.GlucoseMaxValue
import com.dangdang.common.utils.GlucoseMinValue
import com.dangdang.common.utils.isValidPostPrandialGlucose
import com.dangdang.common.utils.medium
import com.dangdang.common.utils.regular
import com.dangdang.component.button.PrimaryButton
import com.dangdang.component.button.outlined.SecondaryOutlinedButton
import com.dangdang.component.divider.Divider
import com.dangdang.component.text.textfield.TextField
import com.dangdang.data.enums.DividerPosition
import com.dangdang.data.enums.LayoutSize
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.Gray
import com.dangdang.ui.theme.MediumRoundShape
import com.dangdang.ui.theme.ThinLineDp
import com.dangdang.ui.theme.White

//식전 혈당 입력 박스
@Preview
@Composable
fun BeginGlucoseInputMenuBoxPreview() {
    BeginGlucoseInputMenuBox(
        glucoseValue = "",
        onGlucoseValueChange = {},
        onGlucoseInputCompleteClick = {},
        onGlucoseInputCancelClick = {}
    )
}

@Composable
fun BeginGlucoseInputMenuBox(
    glucoseValue : String,
    onGlucoseValueChange : (String) -> Unit,
    onGlucoseInputCompleteClick: () -> Unit,
    onGlucoseInputCancelClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = White,
                shape = MediumRoundShape
            )
            .border(
                width = ThinLineDp,
                color = Black,
                shape = MediumRoundShape
            )
            .padding(
                horizontal = 15.dp,
                vertical = 10.dp
            ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "식전 혈당 입력",
            style = AppTypography.labelLarge.regular,
            color = Black,
        )

        Divider(
            position = DividerPosition.Horizontal,
            color = Black
        )

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
            ) {
                TextField(
                    isMaxLengthView = false,
                    rightIcon = {
                        Text(
                            text = "mg/dL",
                            style = AppTypography.labelLarge.regular,
                            color = Gray,
                        )
                    },
                    keyboardType = KeyboardType.Number,
                    value = glucoseValue,
                    onValueChange = onGlucoseValueChange,
                    isError = !isValidPostPrandialGlucose(glucoseValue),
                    errorText = "${GlucoseMinValue}~${GlucoseMaxValue} 범위여야 합니다.",
                    placeholderText = "${GlucoseMinValue}~${GlucoseMaxValue}",
                    maxLength = 5,
                    sizeType = LayoutSize.FillMaxSize
                )
            }

            PrimaryButton(
                text = "확인",
                enabled = glucoseValue.isNotEmpty() && isValidPostPrandialGlucose(glucoseValue),
                onClick = onGlucoseInputCompleteClick
            )
        }

        SecondaryOutlinedButton(
            text = "모르겠어요",
            sizeType = LayoutSize.FillMaxSize,
            onClick = onGlucoseInputCancelClick
        )
    }
}
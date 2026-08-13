package com.dangdang.component.page.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.common.utils.isValidHbA1c
import com.dangdang.common.utils.medium
import com.dangdang.common.utils.regular
import com.dangdang.component.text.textfield.TextField
import com.dangdang.component.toggle.CheckBox
import com.dangdang.data.enums.LayoutSize
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.Gray
import com.dangdang.ui.theme.White

@Preview
@Composable
fun HemoglobinTextFieldPreview(
){
    HemoglobinTextField(
        value = "",
        onValueChange = {},
        isUnknown = false,
        onUnknownChange = {}
    )
}

@Composable
fun HemoglobinTextField(
    value: String,
    onValueChange: (String) -> Unit,
    isUnknown: Boolean,
    onUnknownChange: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = White
            ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        //당화혈색소 텍스트필드
        TextField(
            title = "당화혈색소 (HbA1c)",
            isMaxLengthView = false,
            isRequired = false,
            isBorder = true,
            value = value,
            onValueChange = onValueChange,
            isError = !isValidHbA1c(value),
            errorText = "4~15% 범위여야 합니다.",
            placeholderText = "% 단위로 숫자만 입력해주세요(4~15)",
            maxLength = 4,
            sizeType = LayoutSize.FillMaxSize,
            keyboardType = KeyboardType.Number
        )

        Text(
            text = "최근 검사 결과를 입력해주세요",
            style = AppTypography.labelMedium.regular,
            color = Gray,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CheckBox(
                checked = isUnknown,
                onCheckedChange = onUnknownChange
            )

            Text(
                text = "최근 검사 결과를 모르겠어요",
                style = AppTypography.labelMedium.regular,
                color = Black,
            )
        }
    }
}
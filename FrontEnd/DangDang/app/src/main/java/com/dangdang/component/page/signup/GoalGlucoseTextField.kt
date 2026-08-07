package com.dangdang.component.page.signup

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
import com.dangdang.common.utils.regular
import com.dangdang.component.text.textfield.TextField
import com.dangdang.data.enums.LayoutSize
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Gray
import com.dangdang.ui.theme.White

@Preview
@Composable
fun GoalGlucoseTextFieldPreview(
){
    GoalGlucoseTextField(
        value = "",
        onValueChange = {}
    )
}

@Composable
fun GoalGlucoseTextField(
    value: String,
    onValueChange: (String) -> Unit,
){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = White
            ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        //식후 2시간 목표 혈당 텍스트필드
        TextField(
            title = "식후 2시간 목표 혈당",
            isMaxLengthView = false,
            isRequired = false,
            isBorder = true,
            value = value,
            onValueChange = onValueChange,
            placeholderText = "mg/dL 단위로 숫자만 입력해주세요",
            maxLength = 3,
            sizeType = LayoutSize.FillMaxSize,
            keyboardType = KeyboardType.Number
        )

        Text(
            text = "일반적으로 권장되는 목표는 180 mg/dL 미만입니다.",
            style = AppTypography.labelMedium.regular,
            color = Gray,
        )
    }
}
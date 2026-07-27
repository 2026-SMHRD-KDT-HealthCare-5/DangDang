package com.dangdang.component.text.textfield

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dangdang.R
import com.dangdang.common.utils.componentWidthModifier
import com.dangdang.common.utils.regular
import com.dangdang.data.enums.LayoutSize
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Gray
import com.dangdang.ui.theme.White

@Preview
@Composable
fun TextFieldPreview(

){
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ){


        TextField(
            leftIcon = {
                Icon(
                    painter = painterResource(R.mipmap.kakao_login),
                    contentDescription = "Clear text",
                    modifier = Modifier.size(24.dp)
                )
            },
            isBorder = false,
            value = "일이삼사오육칠팔구십일이삼사오육칠팔구십일이삼사오육칠팔구십",
            onValueChange = {},
            placeholderText = "이메일 주소를 입력해주세요",
            sizeType = LayoutSize.FillMaxSize
        )
    }
}

@Composable
fun TextField(
    isBorder: Boolean = true,
    leftIcon: @Composable () -> Unit,
    value: String,
    onValueChange: (String) -> Unit,
    placeholderText: String,
    fixWidth: Dp? = null,
    sizeType: LayoutSize = LayoutSize.DefaultSize,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = AppTypography.labelLarge.regular,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done
        ),
        singleLine = true,
        modifier = Modifier
            .then(
                componentWidthModifier(
                    fixWidth = fixWidth,
                    sizeType = sizeType
                )
            ),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .background(White, shape = RoundedCornerShape(12.dp))
                    .border(if(isBorder) 1.dp else 0.dp, Gray, shape = RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                leftIcon()

                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholderText,
                            style = AppTypography.labelLarge.regular,
                            color = Gray
                        )
                    }
                    innerTextField()
                }

                if (value.isNotEmpty()) {
                    IconButton(
                        onClick = { onValueChange("") },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.mipmap.kakao_login),
                            contentDescription = "Clear text",
                        )
                    }
                }
            }
        }
    )
}
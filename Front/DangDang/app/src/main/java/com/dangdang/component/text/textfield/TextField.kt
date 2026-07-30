package com.dangdang.component.text.textfield

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dangdang.R
import com.dangdang.common.utils.componentWidthModifier
import com.dangdang.common.utils.medium
import com.dangdang.common.utils.regular
import com.dangdang.data.enums.LayoutSize
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.DarkGray
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
            isMaxLengthView = false,
            isRequired = false,
            isBorder = true,
            value = "",
            onValueChange = {},
            placeholderText = "이메일 주소를 입력해주세요",
            maxLength = 50,
            sizeType = LayoutSize.FillMaxSize
        )

        TextField(
            isMaxLengthView = false,
            isRequired = false,
            leftIcon = {
                Icon(
                    painter = painterResource(R.mipmap.kakao_login),
                    contentDescription = "left icon",
                    modifier = Modifier.size(24.dp)
                )
            },
            rightIcon = {
                Icon(
                    painter = painterResource(R.mipmap.kakao_login),
                    contentDescription = "left icon",
                    modifier = Modifier.size(24.dp)
                )
            },
            isBorder = false,
            value = "일이삼사",
            onValueChange = {},
            placeholderText = "이메일 주소를 입력해주세요",
            maxLength = 50,
            sizeType = LayoutSize.FillMaxSize
        )

        TextField(
            title = "타이틀",
            isMaxLengthView = true,
            isRequired = true,
            leftIcon = {
                Icon(
                    painter = painterResource(R.mipmap.kakao_login),
                    contentDescription = "left icon",
                    modifier = Modifier.size(24.dp)
                )
            },
            rightIcon = {
                Icon(
                    painter = painterResource(R.mipmap.kakao_login),
                    contentDescription = "left icon",
                    modifier = Modifier.size(24.dp)
                )
            },
            isBorder = true,
            value = "일이삼사오육칠팔구십일이삼사오육칠팔구십일이삼사오육칠팔구십",
            onValueChange = {},
            placeholderText = "이메일 주소를 입력해주세요",
            maxLength = 50,
            sizeType = LayoutSize.FillMaxSize
        )

        TextField(
            isEnabled = false,
            title = "타이틀",
            isMaxLengthView = true,
            isRequired = true,
            leftIcon = {
                Icon(
                    painter = painterResource(R.mipmap.kakao_login),
                    contentDescription = "left icon",
                    modifier = Modifier.size(24.dp)
                )
            },
            rightIcon = {
                Icon(
                    painter = painterResource(R.mipmap.kakao_login),
                    contentDescription = "left icon",
                    modifier = Modifier.size(24.dp)
                )
            },
            isBorder = true,
            value = "일이삼사오육칠팔구십일이삼사오육칠팔구십일이삼사오육칠팔구십",
            onValueChange = {},
            placeholderText = "이메일 주소를 입력해주세요",
            maxLength = 50,
            sizeType = LayoutSize.FillMaxSize
        )
    }
}

@Composable
fun TextField(
    isEnabled: Boolean = true,
    title: String? = null,
    isMaxLengthView: Boolean = true,
    isRequired: Boolean = true,
    isBorder: Boolean = true,
    leftIcon: @Composable () -> Unit = {},
    rightIcon: @Composable () -> Unit = {},
    keyboardType: KeyboardType = KeyboardType.Text,
    value: String,
    onValueChange: (String) -> Unit,
    placeholderText: String,
    maxLength: Int,
    fixWidth: Dp? = null,
    sizeType: LayoutSize = LayoutSize.DefaultSize,
) {
    Column(
        modifier = Modifier
            .background(White),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if(title != null){
            TextFieldTitle(
                title = title,
                isRequired = isRequired
            )
        }

        BasicTextField(
            enabled = isEnabled,
            value = value,
            onValueChange = { newValue ->
                onValueChange(newValue.take(maxLength))
            },
            textStyle = AppTypography.labelLarge.regular,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = ImeAction.Done
            ),
            singleLine = true,
            modifier = Modifier
                .componentWidthModifier(
                    fixWidth = fixWidth,
                    sizeType = sizeType
                ),
            visualTransformation =
                if(keyboardType == KeyboardType.Password ||
                    keyboardType == KeyboardType.NumberPassword){
                    PasswordVisualTransformation(mask = '*')
                }else{
                    VisualTransformation.None
                }
            ,
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .background(if(isEnabled) White else DarkGray, shape = RoundedCornerShape(12.dp))
                        .border(1.dp, if(isBorder) Gray else White, shape = RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    leftIcon()

                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholderText,
                                style = AppTypography.labelLarge.regular,
                                color = if(isEnabled) Gray else White
                            )
                        }
                        innerTextField()
                    }

                    rightIcon()
                }
            }
        )

        if(isMaxLengthView){
            Text(
                text = "${value.length}/${maxLength}",
                style = AppTypography.labelMedium.medium,
                color = Black,
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }
    }
}
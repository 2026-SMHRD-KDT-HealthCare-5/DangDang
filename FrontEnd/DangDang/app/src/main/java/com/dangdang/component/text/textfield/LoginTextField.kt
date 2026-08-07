package com.dangdang.component.text.textfield

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.R
import com.dangdang.component.divider.Divider
import com.dangdang.data.enums.DividerPosition
import com.dangdang.data.enums.LayoutSize
import com.dangdang.ui.theme.LightGray
import com.dangdang.ui.theme.White

@Preview
@Composable
fun LoginTextFieldPreview(

){
    LoginTextField(
        emailValue = "email",
        passwordValue = "abcd",
        onEmailChange = {},
        onPasswordChange = {}
    )
}

@Composable
fun LoginTextField(
    emailValue: String,
    passwordValue: String,
    onEmailChange: (String)-> Unit,
    onPasswordChange: (String)-> Unit
){
    var isPasswordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = White,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = LightGray,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TextField(
            isMaxLengthView = false,
            isBorder = false,
            leftIcon = {
                Icon(
                    painter = painterResource(R.mipmap.email_icon),
                    contentDescription = "left icon",
                    modifier = Modifier.size(24.dp)
                )
            },
            value = emailValue,
            onValueChange = onEmailChange,
            placeholderText = "이메일 주소를 입력해주세요",
            maxLength = 100,
            sizeType = LayoutSize.FillMaxSize
        )
        
        Divider(
            position = DividerPosition.Horizontal
        )

        TextField(
            isMaxLengthView = false,
            isBorder = false,
            leftIcon = {
                Icon(
                    painter = painterResource(R.mipmap.lock_icon),
                    contentDescription = "left icon",
                    modifier = Modifier.size(24.dp)
                )
            },
            rightIcon = {
                Icon(
                    painter = painterResource(
                        if(isPasswordVisible){
                            R.mipmap.password_visible
                        }else{
                            R.mipmap.password_invisible
                        }
                    ),
                    contentDescription = "right icon",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable(
                            onClick = {
                                isPasswordVisible = !isPasswordVisible
                            }
                        )
                )
            },
            value = passwordValue,
            onValueChange = onPasswordChange,
            placeholderText = "비밀번호를 입력해주세요",
            keyboardType =
                if(isPasswordVisible){
                    KeyboardType.Text
                } else {
                    KeyboardType.Password
                },
            maxLength = 100,
            sizeType = LayoutSize.FillMaxSize
        )
    }
}
package com.dangdang.component.text.textbox

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.common.utils.componentWidthModifier
import com.dangdang.common.utils.medium
import com.dangdang.common.utils.regular
import com.dangdang.component.text.textfield.TextFieldTitle
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.DarkGray
import com.dangdang.ui.theme.Gray
import com.dangdang.ui.theme.White

@Preview
@Composable
fun TextBoxPreview(){
    TextBox(
        title = "타이틀",
        isRequired = true,
        value = "",
        onValueChange = {},
        placeholderText = "팀을 소개해주세요(100자)",
        maxLength = 100
    )
}

@Composable
fun TextBox(
    modifier: Modifier = Modifier,
    title: String,
    isRequired: Boolean,
    value: String,
    onValueChange: (String) -> Unit,
    placeholderText: String,
    maxLength: Int,
){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = White
            ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TextFieldTitle(
            title = title,
            isRequired = isRequired
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = White,
                    shape = RoundedCornerShape(12.dp)
                )
                .border(
                    width = 1.dp,
                    color = Gray,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = { newValue ->
                    onValueChange(newValue.take(maxLength))
                },
                textStyle = AppTypography.labelLarge.regular,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.None
                ),
                singleLine = false,
                modifier = modifier
                    .fillMaxWidth()
                    .heightIn(min = 50.dp),
                decorationBox = { innerTextField ->
                    Row(

                    ) {
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
                    }
                }
            )

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
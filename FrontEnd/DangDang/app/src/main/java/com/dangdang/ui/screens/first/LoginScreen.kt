package com.dangdang.ui.screens.first

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.dangdang.R
import com.dangdang.common.utils.bold
import com.dangdang.common.utils.medium
import com.dangdang.common.utils.screen
import com.dangdang.component.button.GoogleLoginButton
import com.dangdang.component.button.KakaoLoginButton
import com.dangdang.component.button.PrimaryButton
import com.dangdang.component.divider.Divider
import com.dangdang.component.text.textfield.LoginTextField
import com.dangdang.data.enums.DividerPosition
import com.dangdang.data.enums.LayoutSize
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.Navy
import com.dangdang.ui.viewmodel.first.LoginViewModel

@Preview
@Composable
fun LoginScreenPreview(

){
    LoginScreenContent(
        emailValue = "",
        passwordValue = "",
        onEmailChange = {},
        onPasswordChange = {},
        onEmailLoginClick = {},
        onGoogleLoginClick = {},
        onKakaoLoginClick = {},
        onSignupMove = {}
    )
}

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: (isSignUp: Boolean) -> Unit,
    onSignupMove: () -> Unit
) {
    val context = LocalContext.current
    var emailValue by remember { mutableStateOf("") }
    var passwordValue by remember { mutableStateOf("") }

    LoginScreenContent(
        emailValue = emailValue,
        passwordValue = passwordValue,
        onEmailChange = {
            emailValue = it
        },
        onPasswordChange = {
            passwordValue = it
        },
        onEmailLoginClick = {
            loginViewModel.emailLogin(
                context = context,
                onLoginSuccess = {
                    onLoginSuccess(false)
                },
                email = emailValue,
                password = passwordValue
            )
        },
        onGoogleLoginClick = {
            loginViewModel.googleLogin(
                context = context,
                onLoginSuccess = { isSignUp ->
                    onLoginSuccess(isSignUp)
                }
            )
        },
        onKakaoLoginClick = {
            loginViewModel.kakaoLogin(
                context = context,
                onLoginSuccess = { isSignUp ->
                    onLoginSuccess(isSignUp)
                }
            )
        },
        onSignupMove = onSignupMove
    )
}

@Composable
fun LoginScreenContent(
    emailValue: String,
    passwordValue: String,
    onEmailChange: (String)-> Unit,
    onPasswordChange: (String)-> Unit,
    onEmailLoginClick: ()-> Unit,
    onGoogleLoginClick: ()-> Unit,
    onKakaoLoginClick: ()-> Unit,
    onSignupMove: ()-> Unit
){
    Column(
        modifier = Modifier
            .screen()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.dangdang_login),
            contentDescription = "당당이",
            modifier = Modifier
                .width(135.dp)
                .height(190.dp)
        )
        Text(
            text = "당당이와 함께",
            style = AppTypography.titleLarge.bold,
            color = Black,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "건강한 하루를 시작해보세요!",
            style = AppTypography.bodyLarge.bold,
            color = Black,
        )
        Spacer(Modifier.height(20.dp))
        LoginTextField(
            emailValue = emailValue,
            passwordValue = passwordValue,
            onEmailChange = onEmailChange,
            onPasswordChange = onPasswordChange
        )
        Spacer(Modifier.height(20.dp))
        PrimaryButton(
            text = "이메일로 로그인",
            enabled = emailValue.isNotEmpty()
                    && passwordValue.isNotEmpty(),
            sizeType = LayoutSize.FillMaxSize,
            onClick = onEmailLoginClick
        )
        Spacer(Modifier.height(10.dp))
        Divider(position = DividerPosition.Horizontal)
        Spacer(Modifier.height(10.dp))
        GoogleLoginButton(
            sizeType = LayoutSize.FillMaxSize,
            onClick = onGoogleLoginClick
        )
        Spacer(Modifier.height(10.dp))
        KakaoLoginButton(
            sizeType = LayoutSize.FillMaxSize,
            onClick = onKakaoLoginClick
        )
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "계정이 없으신가요?",
                style = AppTypography.labelLarge.bold,
                color = Black,
            )

            Text(
                text = "회원가입하기",
                style = AppTypography.labelLarge.bold,
                color = Navy,
                modifier = Modifier
                    .clickable(
                        onClick = onSignupMove
                    )
            )
        }
    }
}
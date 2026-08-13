package com.dangdang.ui.screens.first

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.dangdang.R
import com.dangdang.common.utils.AppRoute
import com.dangdang.common.utils.SignUpDefault
import com.dangdang.common.utils.regular
import com.dangdang.common.utils.screen
import com.dangdang.component.button.PrimaryButton
import com.dangdang.component.errorview.ErrorView
import com.dangdang.component.navigation.topnavigation.TopNavigation
import com.dangdang.component.page.signup.SignUpFormContent
import com.dangdang.component.text.heading.Heading
import com.dangdang.data.enums.BackgroundType
import com.dangdang.data.enums.LayoutSize
import com.dangdang.data.enums.LoadingState
import com.dangdang.data.model.user.SignUpForm
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.viewmodel.first.SignUpViewModel

@Preview
@Composable
fun SignUpScreenPreview(

){
    SignUpScreenContent(
        onBackClick = {},
        isUserInfoInputComplete = false,
        isUpdate = false,
        isEmailDisable = false,
        signUpForm = SignUpDefault,
        onSignUpCompleteClick = {}
    )
}

@Composable
fun SignUpScreen(
    signUpViewModel: SignUpViewModel = hiltViewModel(),
    navController: NavController,
    isUpdate: Boolean,
    isSocial: Boolean? = null,
    isEmailDisable: Boolean
){
    val context = LocalContext.current
    val userInfoDetail by
        signUpViewModel.userInfoDetail.collectAsState()

    val isUserInfoInputComplete by
        signUpViewModel.isUserInfoInputComplete.collectAsState()

    LaunchedEffect(Unit) {
        signUpViewModel.getUserInfoDetail(isUpdate, isSocial)
    }

    if(userInfoDetail.loadingState == LoadingState.Success){
        SignUpScreenContent(
            signUpViewModel = signUpViewModel,
            isUserInfoInputComplete = isUserInfoInputComplete,
            onBackClick = {
                navController.popBackStack()
            },
            onSignUpCompleteClick = {
                if(isUpdate){
                    signUpViewModel.userInfoUpdate(
                        context = context,
                        //회원정보 수정 성공 시
                        onSuccess = {
                            navController.popBackStack()
                        }
                    )
                }else{
                    signUpViewModel.signUp(
                        context = context,
                        onSuccess = {
                            //회원가입 완료로 이동
                            navController.navigate(AppRoute.SignUpComplete.route) {
                                popUpTo(0) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            },
            isUpdate = isUpdate,
            isEmailDisable = isEmailDisable,
            signUpForm = userInfoDetail.data ?: SignUpDefault
        )
    }else{
        ErrorView(
            loadingState = userInfoDetail.loadingState,
            message = "회원정보 불러오기를 실패했습니다."
        )
    }
}

@Composable
fun SignUpScreenContent(
    signUpViewModel: SignUpViewModel? = null,
    isUserInfoInputComplete: Boolean,
    onBackClick: () -> Unit,
    onSignUpCompleteClick: () -> Unit,
    isUpdate: Boolean,
    isEmailDisable: Boolean,
    signUpForm: SignUpForm,
){
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .screen()
    ) {
        TopNavigation(
            isBackButton = true,
            onBackClick = onBackClick,
            title = if(isUpdate) "회원정보 수정" else "회원가입",
            backgroundType = BackgroundType.White
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
            Row(
                modifier = Modifier
                    .padding(
                        horizontal = 20.dp,
                        vertical = 12.dp
                    )
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                ){
                    Heading(
                        title = "기본 정보를 입력해주세요",
                        description = (if(isUpdate) "회원정보 수정" else "계정 생성") +
                                "에 필요한 정보를 입력해주세요"
                    )
                }

                Image(
                    painter = painterResource(R.drawable.dangdang),
                    contentDescription = "당당이",
                    modifier = Modifier
                        .width(50.dp)
                        .height(70.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SignUpFormContent(
                    isEmailDisable = isEmailDisable,
                    signUpForm = signUpForm,
                    onFormChange = {
                        signUpViewModel?.onUserInfoUpdate(it)
                    }
                )

                PrimaryButton(
                    text = "${if(isUpdate) "회원정보 수정" else "회원가입"} 완료",
                    enabled = isUserInfoInputComplete,
                    sizeType = LayoutSize.FillMaxSize,
                    onClick = onSignUpCompleteClick
                )

                Spacer(Modifier.height(12.dp))
            }
        }
    }
}
package com.dangdang.component.page.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.common.utils.activityLevelList
import com.dangdang.component.text.selector.Selector
import com.dangdang.component.text.textfield.TextField
import com.dangdang.data.enums.Gender
import com.dangdang.data.enums.LayoutSize
import com.dangdang.data.model.user.SignUpForm
import com.dangdang.ui.theme.White

@Preview
@Composable
fun SignUpFormContentPreview(

){
    SignUpFormContent(
        isEmailDisable = false,
        signUpForm = SignUpForm(
            isSocial = false,
            nickname = "",
            email = "",
            password = "",
            passwordCheck = "",
            gender = Gender.Male,
            birthday = "",
            height = "",
            weight = "",
            hemoglobin = "",
            isHemoglobinRecentResultUnknown = false,
            goalGlucose = "",
            activityLevel = "거의 안함"
        ),
        onFormChange = {}
    )
}

@Composable
fun SignUpFormContent(
    isEmailDisable: Boolean,
    signUpForm: SignUpForm,
    onFormChange: (SignUpForm) -> Unit,
){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = White
            ),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        //닉네임 텍스트필드
        TextField(
            title = "이름(닉네임)",
            isMaxLengthView = false,
            isRequired = false,
            isBorder = true,
            value = signUpForm.nickname,
            onValueChange = {
                onFormChange(
                    signUpForm.copy(nickname = it)
                )
            },
            placeholderText = "닉네임을 입력해주세요",
            maxLength = 100,
            sizeType = LayoutSize.FillMaxSize
        )
        //이메일 텍스트필드
        TextField(
            isEnabled = !isEmailDisable,
            title = "이메일",
            isMaxLengthView = false,
            isRequired = false,
            isBorder = true,
            value = signUpForm.email,
            onValueChange = {
                onFormChange(
                    signUpForm.copy(email = it)
                )
            },
            placeholderText = "이메일을 입력해주세요",
            maxLength = 100,
            sizeType = LayoutSize.FillMaxSize
        )
        //비밀번호 텍스트필드
        TextField(
            isEnabled = !signUpForm.isSocial,
            title = "비밀번호",
            isMaxLengthView = false,
            isRequired = false,
            isBorder = true,
            value = signUpForm.password,
            onValueChange = {
                onFormChange(
                    signUpForm.copy(password = it)
                )
            },
            placeholderText = "비밀번호를 입력해주세요",
            maxLength = 100,
            sizeType = LayoutSize.FillMaxSize,
            keyboardType = KeyboardType.Password
        )
        //비밀번호 확인 텍스트필드
        TextField(
            isEnabled = !signUpForm.isSocial,
            title = "비밀번호 확인",
            isMaxLengthView = false,
            isRequired = false,
            isBorder = true,
            value = signUpForm.passwordCheck,
            onValueChange = {
                onFormChange(
                    signUpForm.copy(passwordCheck = it)
                )
            },
            placeholderText = "비밀번호를 다시 입력해주세요",
            maxLength = 100,
            sizeType = LayoutSize.FillMaxSize,
            keyboardType = KeyboardType.Password
        )
        //성별
        GenderCheckView(
            gender = signUpForm.gender,
            onGenderChange = {
                onFormChange(
                    signUpForm.copy(gender = it)
                )
            }
        )
        //생년월일 텍스트필드
        TextField(
            title = "생년월일",
            isMaxLengthView = false,
            isRequired = false,
            isBorder = true,
            value = signUpForm.birthday,
            onValueChange = {
                onFormChange(
                    signUpForm.copy(birthday = it)
                )
            },
            placeholderText = "YYYY.MM.DD",
            maxLength = 10,
            sizeType = LayoutSize.FillMaxSize
        )
        //키 텍스트필드
        TextField(
            title = "키",
            isMaxLengthView = false,
            isRequired = false,
            isBorder = true,
            value = signUpForm.height,
            onValueChange = {
                onFormChange(
                    signUpForm.copy(height = it)
                )
            },
            placeholderText = "키를 숫자로 입력해주세요(50~500)",
            maxLength = 3,
            sizeType = LayoutSize.FillMaxSize,
            keyboardType = KeyboardType.Number
        )
        //몸무게 텍스트필드
        TextField(
            title = "몸무게",
            isMaxLengthView = false,
            isRequired = false,
            isBorder = true,
            value = signUpForm.weight,
            onValueChange = {
                onFormChange(
                    signUpForm.copy(weight = it)
                )
            },
            placeholderText = "몸무게를 숫자로 입력해주세요(20~300)",
            maxLength = 3,
            sizeType = LayoutSize.FillMaxSize,
            keyboardType = KeyboardType.Number
        )

        HemoglobinTextField(
            value = signUpForm.hemoglobin,
            onValueChange = {
                onFormChange(
                    signUpForm.copy(hemoglobin = it)
                )
            },
            isUnknown = signUpForm.isHemoglobinRecentResultUnknown,
            onUnknownChange = {
                onFormChange(
                    signUpForm.copy(isHemoglobinRecentResultUnknown = !signUpForm.isHemoglobinRecentResultUnknown)
                )
            }
        )

        GoalGlucoseTextField(
            value = signUpForm.goalGlucose,
            onValueChange = {
                onFormChange(
                    signUpForm.copy(goalGlucose = it)
                )
            }
        )

        ActivityLevelCheckView(
            checkedActivityLevel =
                activityLevelList.find {
                    it.title == signUpForm.activityLevel
                }
                ?: activityLevelList[0],
            onCheckedActivityLevelChange = {
                onFormChange(
                    signUpForm.copy(activityLevel = it.title)
                )
            }
        )
    }
}
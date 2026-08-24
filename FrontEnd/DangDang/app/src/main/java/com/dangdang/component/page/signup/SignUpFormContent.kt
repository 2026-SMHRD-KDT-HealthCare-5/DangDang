package com.dangdang.component.page.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.common.utils.HeightMaxValue
import com.dangdang.common.utils.HeightMinValue
import com.dangdang.common.utils.PasswordMinLength
import com.dangdang.common.utils.WeightMaxValue
import com.dangdang.common.utils.WeightMinValue
import com.dangdang.common.utils.activityLevelList
import com.dangdang.common.utils.diagnosisGroupList
import com.dangdang.common.utils.isValidBirthDate
import com.dangdang.common.utils.isValidEmail
import com.dangdang.common.utils.isValidHeight
import com.dangdang.common.utils.isValidPassword
import com.dangdang.common.utils.isValidWeight
import com.dangdang.component.text.selector.Selector
import com.dangdang.component.text.textfield.TextField
import com.dangdang.data.enums.DiagnosisGroup
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
            gender = Gender.male.name,
            birthDate = "",
            height = "",
            weight = "",
            hba1c = "",
            isHemoglobinRecentResultUnknown = false,
            targetGlucose = "",
            activityLevel = "거의 안함",
            joinedAt = "",
            profileImageUrl = "",
            notificationEnabled = false,
            diagnosisGroup = diagnosisGroupList[0]
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
            isError = !isValidEmail(signUpForm.email),
            errorText = "이메일 형식이 아닙니다.",
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
            value = signUpForm.password?:"",
            isError = !isValidPassword(signUpForm.password?:""),
            errorText = "비밀번호는 ${PasswordMinLength}자 이상이어야 합니다.",
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
            value = signUpForm.passwordCheck?:"",
            isError = signUpForm.password != signUpForm.passwordCheck,
            errorText = "비밀번호가 일치하지 않습니다.",
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
            gender = Gender.valueOf(signUpForm.gender),
            onGenderChange = {
                onFormChange(
                    signUpForm.copy(gender = it.name)
                )
            }
        )
        //생년월일 텍스트필드
        TextField(
            title = "생년월일",
            isMaxLengthView = false,
            isRequired = false,
            isBorder = true,
            value = signUpForm.birthDate,
            isError = !isValidBirthDate(signUpForm.birthDate),
            errorText = "만 14세 이상이어야 하며, 유효한 날짜여야 합니다.",
            onValueChange = {
                onFormChange(
                    signUpForm.copy(birthDate = it)
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
            isError = !isValidHeight(signUpForm.height),
            errorText = "키를 숫자로 입력해주세요(" +
                    "${HeightMinValue}~${HeightMaxValue})",
            onValueChange = {
                onFormChange(
                    signUpForm.copy(height = it)
                )
            },
            placeholderText = "키를 숫자로 입력해주세요(" +
                    "${HeightMinValue}~${HeightMaxValue})",
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
            isError = !isValidWeight(signUpForm.weight),
            errorText = "몸무게를 숫자로 입력해주세요(" +
                    "${WeightMinValue.toInt()}~${WeightMaxValue.toInt()})",
            onValueChange = {
                onFormChange(
                    signUpForm.copy(weight = it)
                )
            },
            placeholderText = "몸무게를 숫자로 입력해주세요(" +
                    "${WeightMinValue.toInt()}~${WeightMaxValue.toInt()})",
            maxLength = 5,
            sizeType = LayoutSize.FillMaxSize,
            keyboardType = KeyboardType.Number
        )

        Selector(
            title = "당뇨 유형",
            items = diagnosisGroupList,
            selectedItem = signUpForm.diagnosisGroup,
            itemText = { it?:"" },
            onSelected = {
                onFormChange(
                    signUpForm.copy(diagnosisGroup = it)
                )
            }
        )

        HemoglobinTextField(
            value = signUpForm.hba1c,
            onValueChange = {
                onFormChange(
                    signUpForm.copy(hba1c = it)
                )
            },
            isUnknown = signUpForm.isHemoglobinRecentResultUnknown,
            onUnknownChange = {
                onFormChange(
                    signUpForm.copy(
                        hba1c = when (signUpForm.diagnosisGroup) {
                            DiagnosisGroup.Prediabetes.title -> {
                                "6"
                            }

                            DiagnosisGroup.DiabetesType2.title -> {
                                "7"
                            }

                            else -> {
                                "5"
                            }
                        },
                        isHemoglobinRecentResultUnknown = !signUpForm.isHemoglobinRecentResultUnknown
                    )
                )
            }
        )

        GoalGlucoseTextField(
            value = signUpForm.targetGlucose,
            onValueChange = {
                onFormChange(
                    signUpForm.copy(targetGlucose = it)
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
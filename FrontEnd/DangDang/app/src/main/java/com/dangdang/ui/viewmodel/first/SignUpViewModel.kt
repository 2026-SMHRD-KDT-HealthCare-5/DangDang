package com.dangdang.ui.viewmodel.first

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dangdang.common.utils.AppPrefs
import com.dangdang.common.utils.SignUpDefault
import com.dangdang.common.utils.applyResponse
import com.dangdang.common.utils.getResponseError
import com.dangdang.common.utils.isValidBirthDate
import com.dangdang.common.utils.isValidEmail
import com.dangdang.common.utils.isValidHbA1c
import com.dangdang.common.utils.isValidHeight
import com.dangdang.common.utils.isValidPassword
import com.dangdang.common.utils.isValidPostPrandialGlucose
import com.dangdang.common.utils.isValidWeight
import com.dangdang.data.enums.LoadingState
import com.dangdang.data.model.PendingModel
import com.dangdang.data.model.user.SignUpForm
import com.dangdang.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val appPrefs: AppPrefs,
    private val userRepository: UserRepository
): ViewModel(){
    private val _userInfoDetail = MutableStateFlow<PendingModel<SignUpForm>>(
        PendingModel(null, LoadingState.Loading)
    )
    val userInfoDetail: StateFlow<PendingModel<SignUpForm>> = _userInfoDetail.asStateFlow()

    private val _isUserInfoInputComplete = MutableStateFlow(false)
    val isUserInfoInputComplete: StateFlow<Boolean> = _isUserInfoInputComplete.asStateFlow()

    //유저정보 가져오기
    fun getUserInfoDetail(isUpdate: Boolean, isSocial: Boolean?){
        viewModelScope.launch {
            if(isUpdate || isSocial == true){
                val response = userRepository.getUserInfoDetail()
                _userInfoDetail.applyResponse(response)
                if(response.isSuccessful){
                    response.body()?.let {
                        appPrefs.setNotificationEnabled(it.notificationEnabled)
                    }
                }
            }else{
                _userInfoDetail.value = _userInfoDetail.value.copy(
                    data = SignUpDefault,
                    loadingState = LoadingState.Success
                )
            }

            _isUserInfoInputComplete.value = isUserInfoInputComplete()
        }
    }

    //유저정보 form 키보드로 수정 시
    fun onUserInfoUpdate(signUpForm: SignUpForm){
        _userInfoDetail.value = _userInfoDetail.value.copy(
            data = signUpForm,
            loadingState = LoadingState.Success
        )

        _isUserInfoInputComplete.value = isUserInfoInputComplete()
    }

    //회원가입 완료 버튼 활성화 여부
    fun isUserInfoInputComplete(): Boolean{
        val userInfoDetail = _userInfoDetail.value.data?:return false
        return userInfoDetail.nickname.isNotEmpty()
                && (
                    userInfoDetail.email.isNotEmpty()
                    && isValidEmail(userInfoDetail.email)
                )
                && (userInfoDetail.isSocial
                    ||(
                        userInfoDetail.password?.isNotEmpty() == true
                        && isValidPassword(userInfoDetail.password?:"")
                        && userInfoDetail.passwordCheck?.isNotEmpty() == true
                        && userInfoDetail.password == userInfoDetail.passwordCheck
                    )
                )
                && (
                    userInfoDetail.birthDate.isNotEmpty() &&
                            isValidBirthDate(userInfoDetail.birthDate)
                )
                && (
                    userInfoDetail.height.isNotEmpty() &&
                            isValidHeight(userInfoDetail.height)
                )
                && (
                    userInfoDetail.weight.isNotEmpty() &&
                            isValidWeight(userInfoDetail.weight)
                )
                && (
                    (
                            userInfoDetail.hba1c.isNotEmpty() &&
                                    isValidHbA1c(userInfoDetail.hba1c)
                    )
                            || userInfoDetail.isHemoglobinRecentResultUnknown
                )
                && (
                    userInfoDetail.targetGlucose.isNotEmpty() &&
                            isValidPostPrandialGlucose(userInfoDetail.targetGlucose)
                )
    }

    //회원정보 수정 완료
    fun userInfoUpdate(context: Context, onSuccess: ()-> Unit){
        viewModelScope.launch {
            val response = userRepository.userInfoUpdate(
                _userInfoDetail.value.data
            )
            if(response.isSuccessful){
                _userInfoDetail.value.data?.let {
                    appPrefs.setNotificationEnabled(it.notificationEnabled)
                }
                onSuccess()
            }else{
                Toast.makeText(context, "회원정보 수정 요청이 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //회원가입 api 부르기
    fun signUp(context: Context, onSuccess: ()-> Unit){
        viewModelScope.launch {
            val response = userRepository.signUp(
                _userInfoDetail.value.data
            )
            if(response.isSuccessful){
                onSuccess()
            }else{
                Toast.makeText(context, getResponseError(response).message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
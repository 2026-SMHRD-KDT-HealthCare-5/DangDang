package com.dangdang.ui.viewmodel.first

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dangdang.common.utils.AppPrefs
import com.dangdang.common.utils.SignUpDefault
import com.dangdang.common.utils.activityLevelList
import com.dangdang.data.enums.Gender
import com.dangdang.data.model.user.SignUpForm
import com.dangdang.data.model.user.User
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
    private val _userInfoDetail = MutableStateFlow<SignUpForm?>(null)
    val userInfoDetail: StateFlow<SignUpForm?> = _userInfoDetail.asStateFlow()

    private val _isUserInfoInputComplete = MutableStateFlow(false)
    val isUserInfoInputComplete: StateFlow<Boolean> = _isUserInfoInputComplete.asStateFlow()

    //유저정보 가져오기
    fun getUserInfoDetail(isUpdate: Boolean, isSocial: Boolean?){
        viewModelScope.launch {
            if(isUpdate || isSocial == true){
                val response = userRepository.getUserInfoDetail()
                if(response.isSuccessful){
                    val responseBody = response.body()
                    _userInfoDetail.value = responseBody
                }
            }else{
                _userInfoDetail.value = SignUpDefault
            }

            _isUserInfoInputComplete.value = isUserInfoInputComplete()
        }
    }

    //유저정보 form 키보드로 수정 시
    fun onUserInfoUpdate(signUpForm: SignUpForm){
        _userInfoDetail.value = signUpForm

        _isUserInfoInputComplete.value = isUserInfoInputComplete()
    }

    //회원가입 완료 버튼 활성화 여부
    fun isUserInfoInputComplete(): Boolean{
        val userInfoDetail = _userInfoDetail.value ?: return false
        return userInfoDetail.nickname.isNotEmpty()
                && userInfoDetail.email.isNotEmpty()
                && (userInfoDetail.isSocial
                    ||(
                        userInfoDetail.password.isNotEmpty()
                        && userInfoDetail.passwordCheck.isNotEmpty()
                        && userInfoDetail.password == userInfoDetail.passwordCheck
                    )
                )
                && userInfoDetail.birthday.isNotEmpty()
                && userInfoDetail.height.isNotEmpty()
                && userInfoDetail.weight.isNotEmpty()
                && (userInfoDetail.hemoglobin.isNotEmpty() || userInfoDetail.isHemoglobinRecentResultUnknown)
                && userInfoDetail.goalGlucose.isNotEmpty()
    }

    //회원가입 or 회원정보 수정 완료
    fun userInfoUpdate(onSuccess: ()-> Unit){
        viewModelScope.launch {
            val response = userRepository.userInfoUpdate(_userInfoDetail.value)
            if(response.isSuccessful){
                val responseBody = response.body()

                appPrefs.setAccessToken(responseBody?.accessToken?:"")
                appPrefs.setRefreshToken(responseBody?.refreshToken?:"")

                appPrefs.setAutoLogin(true)

                onSuccess()
            }
        }
    }
}
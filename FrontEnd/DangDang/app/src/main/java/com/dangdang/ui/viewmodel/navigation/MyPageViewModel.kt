package com.dangdang.ui.viewmodel.navigation

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dangdang.common.utils.AppPrefs
import com.dangdang.common.utils.applyResponse
import com.dangdang.data.enums.LoadingState
import com.dangdang.data.repository.UserRepository
import com.dangdang.data.manager.SessionManager
import com.dangdang.data.model.PendingModel
import com.dangdang.data.model.user.SignUpForm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager,
    private val appPrefs: AppPrefs
) : ViewModel() {
    private val _userInfo = MutableStateFlow<PendingModel<SignUpForm>>(
        PendingModel(null, LoadingState.Loading)
    )
    val userInfo: StateFlow<PendingModel<SignUpForm>> = _userInfo.asStateFlow()

    fun getUserInfo(){
        viewModelScope.launch {
            val response = userRepository.getUserInfoDetail()
            _userInfo.applyResponse(response)
            if(response.isSuccessful){
                response.body()?.let {
                    appPrefs.setNotificationEnabled(it.notificationEnabled)
                }
            }
        }
    }

    fun logout(context: Context) {
        viewModelScope.launch {
            val response = userRepository.logout()
            if(response.isSuccessful){
                sessionManager.handleLogout()
            }else{
                Toast.makeText(context, "로그아웃 요청이 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun setNotification(context: Context, isNotification: Boolean){
        viewModelScope.launch {
            val response = userRepository.setNotification(isNotification)
            if(response.isSuccessful){
                appPrefs.setNotificationEnabled(isNotification)
                _userInfo.value = _userInfo.value.copy(
                    data = _userInfo.value.data?.copy(
                        notificationEnabled = isNotification
                    ),
                    loadingState = LoadingState.Success
                )
            }else{
                Toast.makeText(context, "알림 설정 변경 요청이 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

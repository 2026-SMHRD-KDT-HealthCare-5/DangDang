package com.dangdang.ui.viewmodel.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dangdang.common.utils.AppPrefs
import com.dangdang.data.model.user.User
import com.dangdang.data.repository.UserRepository
import com.dangdang.data.manager.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val appPrefs: AppPrefs,
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _userInfo = MutableStateFlow<User?>(null)
    val userInfo: StateFlow<User?> = _userInfo.asStateFlow()

    init {
        getUserInfo()
    }

    fun getUserInfo(){
        viewModelScope.launch {
            val response = userRepository.getUserInfo()
            if(response.isSuccessful){
                val responseBody = response.body()
                _userInfo.value = responseBody
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager.handleLogout()
        }
    }

    fun setNotification(isNotification: Boolean){
        viewModelScope.launch {
            appPrefs.setNotification(isNotification)
        }
    }
}
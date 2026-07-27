package com.dangdang.di

import com.dangdang.common.utils.AppPrefs
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class SessionManager(private val appPrefs: AppPrefs) {

    fun getAccessToken(): String = appPrefs.getAccessToken()

    fun getRefreshToken(): String = appPrefs.getRefreshToken()

    fun saveTokens(accessToken: String, refreshToken: String) {
        appPrefs.setAccessToken(accessToken)
        appPrefs.setRefreshToken(refreshToken)
    }

    // 로그아웃 이벤트를 전달하는 스트림
    private val _logoutEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val logoutEvent = _logoutEvent.asSharedFlow()

    fun handleLogout() {
        appPrefs.logout()
        _logoutEvent.tryEmit(Unit) // 로그아웃 이벤트 발생
    }
}
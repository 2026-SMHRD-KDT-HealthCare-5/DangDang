package com.dangdang.di

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.dangdang.common.utils.AppPrefs
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class SessionManager(
    private val context: Context,
    private val appPrefs: AppPrefs
) {

    fun getAccessToken(): String = appPrefs.getAccessToken()

    fun getRefreshToken(): String = appPrefs.getRefreshToken()

    fun saveTokens(accessToken: String, refreshToken: String) {
        appPrefs.setAccessToken(accessToken)
        appPrefs.setRefreshToken(refreshToken)
    }

    // 로그아웃 이벤트를 전달하는 스트림
    private val _logoutEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val logoutEvent = _logoutEvent.asSharedFlow()

    suspend fun handleLogout() {
        appPrefs.logout()

        //구글 로그인 초기화
        try {
            CredentialManager.create(context)
                .clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            e.printStackTrace()
        }

        _logoutEvent.tryEmit(Unit) // 로그아웃 이벤트 발생
    }
}
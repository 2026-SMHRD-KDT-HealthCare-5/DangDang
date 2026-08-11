package com.dangdang.di

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.dangdang.common.utils.AppPrefs
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class SessionManager(
    private val context: Context,
    private val appPrefs: AppPrefs
) {
    @Volatile
    private var accessToken: String = ""

    @Volatile
    private var refreshToken: String = ""

    private val initialized =
        CompletableDeferred<Unit>()


    private val _logoutEvent =
        MutableSharedFlow<Unit>(
            extraBufferCapacity = 1
        )

    val logoutEvent =
        _logoutEvent.asSharedFlow()


    suspend fun initialize() {

        if (initialized.isCompleted) {
            return
        }

        accessToken =
            appPrefs.getAccessToken()

        refreshToken =
            appPrefs.getRefreshToken()

        initialized.complete(Unit)
    }

    fun getAccessToken(): String {
        return accessToken
    }


    fun getRefreshToken(): String {
        return refreshToken
    }


    suspend fun awaitInitialized() {
        initialized.await()
    }


    suspend fun saveTokens(
        newAccessToken: String,
        newRefreshToken: String
    ) {

        appPrefs.saveTokens(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken
        )

        accessToken = newAccessToken
        refreshToken = newRefreshToken
    }


    suspend fun updateAccessToken(
        newAccessToken: String
    ) {

        appPrefs.setAccessToken(
            newAccessToken
        )

        accessToken = newAccessToken
    }


    suspend fun handleLogout() {

        appPrefs.logout()

        accessToken = ""
        refreshToken = ""

        try {
            CredentialManager
                .create(context)
                .clearCredentialState(
                    ClearCredentialStateRequest()
                )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        UserApiClient.instance.logout { }

        _logoutEvent.tryEmit(Unit)
    }
}
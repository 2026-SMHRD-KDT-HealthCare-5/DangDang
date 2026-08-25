package com.dangdang.data.manager

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.dangdang.common.utils.AppPrefs
import com.dangdang.common.utils.StopStepCounting
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableSharedFlow
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

        _logoutEvent.tryEmit(Unit)

        //걷기 미션 종료 처리
        StepCounterManager.reset()
        val isGranted = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        }else{
            true
        }

        if(isGranted){
            StopStepCounting(context, -1)
        }

        //소셜 로그아웃 처리
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
    }
}
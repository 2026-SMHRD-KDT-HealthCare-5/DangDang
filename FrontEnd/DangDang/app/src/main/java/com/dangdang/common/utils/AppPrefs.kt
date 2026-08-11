package com.dangdang.common.utils

import android.content.Context
import androidx.core.content.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dangdang.data.manager.CryptoManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPrefs @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cryptoManager: CryptoManager
) {
    private val prefsName = "AppPrefs"

    private val Context.appDataStore by preferencesDataStore(
        name = prefsName
    )

    private val prefs = context.appDataStore

    //자동 로그인 여부
    private val autoLoginKey = booleanPreferencesKey("autoLogin")

    //액세스 토큰
    private val accessTokenKey = stringPreferencesKey("accessToken")

    //리프레시 토큰
    private val refreshTokenKey = stringPreferencesKey("refreshToken")

    //알림설정
    private val notificationKey = booleanPreferencesKey("notification")

    //로그아웃
    suspend fun logout(){
        prefs.edit { preferences ->
            preferences.remove(autoLoginKey)
            preferences.remove(accessTokenKey)
            preferences.remove(refreshTokenKey)
        }
    }

    val autoLoginFlow: Flow<Boolean> =
        context.appDataStore.data
            .map { preferences ->
                preferences[autoLoginKey] ?: false
            }

    //자동 로그인 여부
    suspend fun isAutoLogin(): Boolean {
        return prefs.data
            .map { preferences ->
                preferences[autoLoginKey] ?: false
            }
            .first()
    }

    suspend fun setAutoLogin(autoLogin: Boolean) {
        prefs.edit { preferences ->
            preferences[autoLoginKey] = autoLogin
        }
    }

    suspend fun getAccessToken(): String {

        val encryptedToken =
            prefs.data
                .map { preferences ->
                    preferences[accessTokenKey] ?: ""
                }
                .first()

        return if (encryptedToken.isNotEmpty()) {
            cryptoManager.decrypt(encryptedToken)
        } else {
            ""
        }
    }

    suspend fun setAccessToken(
        accessToken: String
    ) {

        val encryptedToken =
            cryptoManager.encrypt(accessToken)

        prefs.edit { preferences ->
            preferences[accessTokenKey] = encryptedToken
        }
    }


    // -------------------------
    // Refresh Token
    // -------------------------

    suspend fun getRefreshToken(): String {

        val encryptedToken =
            prefs.data
                .map { preferences ->
                    preferences[refreshTokenKey] ?: ""
                }
                .first()

        return if (encryptedToken.isNotEmpty()) {
            cryptoManager.decrypt(encryptedToken)
        } else {
            ""
        }
    }

    suspend fun setRefreshToken(
        refreshToken: String
    ) {

        val encryptedToken =
            cryptoManager.encrypt(refreshToken)

        prefs.edit { preferences ->
            preferences[refreshTokenKey] = encryptedToken
        }
    }

    suspend fun saveTokens(
        accessToken: String,
        refreshToken: String
    ) {
        val encryptedAccessToken =
            cryptoManager.encrypt(accessToken)

        val encryptedRefreshToken =
            cryptoManager.encrypt(refreshToken)

        prefs.edit { preferences ->
            preferences[accessTokenKey] = encryptedAccessToken
            preferences[refreshTokenKey] = encryptedRefreshToken
        }
    }

    //알림설정
    suspend fun isNotification(): Boolean{
        return prefs.data
            .map { preferences ->
                preferences[notificationKey] ?: false
            }
            .first()
    }

    val notificationFlow: StateFlow<Boolean> =
        context.appDataStore.data
            .map { preferences ->
                preferences[notificationKey] ?: false
            }
            .stateIn(
                scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
                started = SharingStarted.Eagerly,
                initialValue = false
            )

    suspend fun setNotification(notification: Boolean) {
        prefs.edit { preferences ->
            preferences[notificationKey] = notification
        }
    }
}
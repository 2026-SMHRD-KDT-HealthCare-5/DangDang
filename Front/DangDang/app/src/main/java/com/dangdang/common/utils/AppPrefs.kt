package com.dangdang.common.utils

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefsName = "AppPrefs"
    private val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    //자동 로그인 여부
    private val autoLoginKey = "autoLogin"

    //액세스 토큰
    private val accessTokenKey = "accessToken"

    //리프레시 토큰
    private val refreshTokenKey = "refreshToken"

    //로그아웃
    fun logout(){
        prefs.edit {
            remove(autoLoginKey)
            remove(accessTokenKey)
            remove(refreshTokenKey)
        }
    }

    //자동 로그인 여부
    fun isAutoLogin(): Boolean {
        return prefs.getBoolean(autoLoginKey, false)
    }

    fun setAutoLogin(autoLogin: Boolean) {
        prefs.edit { putBoolean(autoLoginKey, autoLogin) }
    }

    //액세스 토큰
    fun getAccessToken(): String {
        return prefs.getString(accessTokenKey, "") ?: ""
    }

    fun setAccessToken(accessToken: String){
        prefs.edit { putString(accessTokenKey, accessToken) }
    }

    //리프레시 토큰
    fun getRefreshToken(): String{
        return prefs.getString(refreshTokenKey, "") ?: ""
    }

    fun setRefreshToken(refreshToken: String){
        prefs.edit { putString(refreshTokenKey, refreshToken) }
    }
}
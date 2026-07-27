package com.dangdang.di

import com.dangdang.Application.Companion.REFRESH_PATH
import com.dangdang.data.api.UserApiService
import com.dangdang.data.model.user.TokenResponse
import dagger.Lazy
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class ApiInterceptor(
    private val userApiService: Lazy<UserApiService>,
    private val sessionManager: SessionManager,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath

        val token = if (path.contains(REFRESH_PATH)) {
            sessionManager.getRefreshToken() // 리프레시 경로면 Refresh Token 추출
        } else {
            sessionManager.getAccessToken()  // 그 외에는 Access Token 추출
        }
        val requestWithToken = originalRequest.newBuilder()
            .header(
                "Authorization",
                "Bearer $token")
            .build()

        val response = chain.proceed(requestWithToken)

        // 401 Unauthorized 발생 시 리프레시 로직 진입
        if (response.code == 401) {
            synchronized(this) { // 여러 요청이 동시에 401 날 때를 대비해 동기화
                val currentRefreshToken = sessionManager.getRefreshToken()

                if (currentRefreshToken != null) {
                    // 서버에 새 토큰 요청 (동기 방식 실행)
                    val newTokens = runBlocking {
                        refreshTokens()
                    }

                    if (newTokens != null) {
                        // 갱신 성공: 토큰 저장 후 원래 요청 재시도
                        sessionManager.saveTokens(newTokens.accessToken, newTokens.refreshToken)
                        response.close() // 기존 응답 닫기

                        val newRequest = originalRequest.newBuilder()
                            .header("Authorization", "Bearer ${newTokens.accessToken}")
                            .build()
                        return chain.proceed(newRequest)
                    }
                }

                // 갱신 실패 또는 리프레시 토큰 없음: 로그인 화면으로
                response.close()
                handleLogout()
                return response
            }
        }

        return response
    }

    private suspend fun refreshTokens(): TokenResponse? {
        // 별도의 Retrofit 서비스를 통해 동기적(또는 runBlocking 내에서) 호출
        return try {
            val response = userApiService.get().refreshToken()
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            null
        }
    }

    private fun handleLogout() {
        sessionManager.handleLogout()
    }
}
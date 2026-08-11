package com.dangdang.data.network

import com.dangdang.data.api.RefreshApiService
import com.dangdang.data.api.UserApiService
import com.dangdang.data.model.user.TokenResponse
import com.dangdang.di.SessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiAuthenticator @Inject constructor(
    private val refreshApiService: RefreshApiService,
    private val sessionManager: SessionManager
) : Authenticator {

    override fun authenticate(
        route: Route?,
        response: Response
    ): Request? {

        if (responseCount(response) >= 2) {
            return null
        }

        synchronized(this) {

            val currentAccessToken =
                sessionManager.getAccessToken()

            val requestAccessToken =
                response.request
                    .header("Authorization")
                    ?.removePrefix("Bearer ")
                    .orEmpty()


            // 다른 요청이 이미 갱신했다면
            if (
                currentAccessToken.isNotEmpty() &&
                currentAccessToken != requestAccessToken
            ) {

                return response.request
                    .newBuilder()
                    .header(
                        "Authorization",
                        "Bearer $currentAccessToken"
                    )
                    .build()
            }


            val refreshToken =
                sessionManager.getRefreshToken()

            if (refreshToken.isEmpty()) {

                runBlocking {
                    sessionManager.handleLogout()
                }

                return null
            }


            // Refresh API
            val refreshResponse =
                try {

                    refreshApiService
                        .refreshToken(
                            authorization =
                                "Bearer $refreshToken"
                        )
                        .execute()

                } catch (e: Exception) {
                    return null
                }


            if (!refreshResponse.isSuccessful) {

                runBlocking {
                    sessionManager.handleLogout()
                }

                return null
            }


            val tokenResponse =
                refreshResponse.body()
                    ?: return null


            // 새 Token 저장
            runBlocking {

                sessionManager.saveTokens(
                    tokenResponse.accessToken,
                    tokenResponse.refreshToken
                )
            }


            // 원래 요청 재시도
            return response.request
                .newBuilder()
                .header(
                    "Authorization",
                    "Bearer ${tokenResponse.accessToken}"
                )
                .build()
        }
    }


    private fun responseCount(
        response: Response
    ): Int {

        var count = 1

        var priorResponse =
            response.priorResponse

        while (priorResponse != null) {

            count++

            priorResponse =
                priorResponse.priorResponse
        }

        return count
    }
}
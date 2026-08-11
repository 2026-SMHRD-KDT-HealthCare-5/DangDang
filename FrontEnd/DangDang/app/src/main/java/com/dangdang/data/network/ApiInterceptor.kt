package com.dangdang.data.network

import com.dangdang.Application
import com.dangdang.data.manager.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class ApiInterceptor(
    private val sessionManager: SessionManager,
) : Interceptor {

    override fun intercept(
        chain: Interceptor.Chain
    ): Response {

        val originalRequest =
            chain.request()

        val path =
            originalRequest.url.encodedPath


        // Refresh API에는 Access Token을 붙이지 않음
        if (path.contains(Application.REFRESH_PATH)) {
            return chain.proceed(
                originalRequest
            )
        }


        val accessToken =
            sessionManager.getAccessToken()

        if (accessToken.isEmpty()) {
            return chain.proceed(
                originalRequest
            )
        }


        val request =
            originalRequest
                .newBuilder()
                .header(
                    "Authorization",
                    "Bearer $accessToken"
                )
                .build()


        return chain.proceed(request)
    }
}
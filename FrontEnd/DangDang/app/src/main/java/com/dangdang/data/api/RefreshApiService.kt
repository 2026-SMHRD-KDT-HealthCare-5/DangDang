package com.dangdang.data.api

import com.dangdang.Application.Companion.REFRESH_PATH
import com.dangdang.data.model.user.RefreshForm
import com.dangdang.data.model.user.TokenResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface RefreshApiService {
    //토큰 리프레시
    @POST(REFRESH_PATH)
    fun refreshToken(
        @Body refreshForm: RefreshForm
    ): Call<TokenResponse>
}
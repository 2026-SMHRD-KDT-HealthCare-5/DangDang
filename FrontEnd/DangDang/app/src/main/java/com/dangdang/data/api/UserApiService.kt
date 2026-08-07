package com.dangdang.data.api

import com.dangdang.Application.Companion.REFRESH_PATH
import com.dangdang.data.model.user.TokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface UserApiService {

    //토큰 리프레시
    @POST(REFRESH_PATH)
    suspend fun refreshToken(): Response<TokenResponse>
}
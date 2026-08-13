package com.dangdang.data.api

import com.dangdang.Application.Companion.AuthPath
import com.dangdang.Application.Companion.REFRESH_PATH
import com.dangdang.data.model.user.SignUpForm
import com.dangdang.data.model.user.SignUpResponse
import com.dangdang.data.model.user.TokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface UserApiService {
    @POST("${AuthPath}/signup")
    suspend fun signUp(@Body signUpForm: SignUpForm?): Response<SignUpResponse>
}
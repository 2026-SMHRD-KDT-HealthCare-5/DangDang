package com.dangdang.data.api

import com.dangdang.Application.Companion.AuthPath
import com.dangdang.Application.Companion.REFRESH_PATH
import com.dangdang.Application.Companion.UserPath
import com.dangdang.data.model.user.LoginForm
import com.dangdang.data.model.user.NotificationSetForm
import com.dangdang.data.model.user.SignUpForm
import com.dangdang.data.model.user.SignUpResponse
import com.dangdang.data.model.user.TokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface UserApiService {
    @POST("${AuthPath}/logout")
    suspend fun logout(): Response<String>

    //사용자 정보 가져오기
    @GET("${UserPath}/me")
    suspend fun getUserInfoDetail(): Response<SignUpForm>

    //회원정보 수정
    @PATCH("${UserPath}/me")
    suspend fun userInfoUpdate(
        @Body signUpForm: SignUpForm?
    ): Response<SignUpForm>

    //알림설정
    @PATCH("${UserPath}/me/notification")
    suspend fun setNotification(
        @Body notificationSetForm: NotificationSetForm
    ): Response<SignUpForm>
}
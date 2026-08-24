package com.dangdang.data.repository

import com.dangdang.common.utils.safeApiCall
import com.dangdang.data.api.HomeApiService
import com.dangdang.data.api.LoginApiService
import com.dangdang.data.api.UserApiService
import com.dangdang.data.model.home.AfterMealGlucoseStatusModel
import com.dangdang.data.model.home.HomeDataResponse
import com.dangdang.data.model.user.LoginForm
import com.dangdang.data.model.user.NotificationSetForm
import com.dangdang.data.model.user.SignUpForm
import com.dangdang.data.model.user.SignUpResponse
import com.dangdang.data.model.user.TokenResponse
import retrofit2.Response
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userApiService: UserApiService,
    private val loginApiService: LoginApiService,
    private val homeApiService: HomeApiService
){
    //이메일 로그인 api 부르기
    suspend fun emailLogin(email: String, password: String): Response<TokenResponse> = safeApiCall {
        loginApiService.login(
            LoginForm(
                email = email,
                password = password
            )
        )
    }

    //구글 로그인 api 부르기
    suspend fun googleLogin(idToken: String): Response<TokenResponse> {
        val response = TokenResponse(
            accessToken = "AccessToken",
            refreshToken = "RefreshToken",
        )

        return Response.success(response)
    }

    //카카오 로그인 api 부르기
    suspend fun kakaoLogin(token: String): Response<TokenResponse> {
        val response = TokenResponse(
            accessToken = "AccessToken",
            refreshToken = "RefreshToken",
        )

        return Response.success(response)
    }

    //유저 회원정보수정 정보 가져오기 api 부르기
    suspend fun getUserInfoDetail(): Response<SignUpForm> = safeApiCall {
        userApiService.getUserInfoDetail()
    }

    //회원정보수정 완료 api 부르기
    suspend fun userInfoUpdate(signUpForm: SignUpForm?): Response<SignUpForm> = safeApiCall {
        userApiService.userInfoUpdate(
            signUpForm?.copy(
                birthDate = signUpForm.birthDate.replace(".", "-")
            )
        )
    }

    //회원가입 api 부르기
    suspend fun signUp(signUpForm: SignUpForm?): Response<SignUpResponse> = safeApiCall {
        loginApiService.signUp(signUpForm?.copy(
            birthDate = signUpForm.birthDate.replace(".", "-")
        ))
    }

    //로그아웃 api 부르기
    suspend fun logout():Response<String> = safeApiCall {
        userApiService.logout()
    }

    //알람설정 api 부르기
    suspend fun setNotification(enabled: Boolean): Response<SignUpForm> = safeApiCall {
        userApiService.setNotification(
            NotificationSetForm(
                notificationEnabled = enabled
            )
        )
    }

    //홈 api 부르기
    suspend fun getHomeData(): Response<HomeDataResponse> = safeApiCall {
        homeApiService.getHomeData()
    }
}
package com.dangdang.data.repository

import com.dangdang.Application.Companion.ExamplePictureUrl
import com.dangdang.data.api.UserApiService
import com.dangdang.data.enums.Gender
import com.dangdang.data.model.user.SignUpForm
import com.dangdang.data.model.user.TokenResponse
import com.dangdang.data.model.user.User
import retrofit2.Response
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userApiService: UserApiService
){
    //구글 로그인 api 부르기
    suspend fun googleLogin(idToken: String): Response<TokenResponse> {
        val response = TokenResponse(
            accessToken = "AccessToken",
            refreshToken = "RefreshToken",
            user = User(
                id = "1",
                isSignUp = false,
                nickname = "닉네임",
                profileImageUrl = ExamplePictureUrl,
                email = "email@gmail.com",
                sinceDays = 120,
                createdDt = "2026-07-28",
                updatedDt = "2026-07-28",
            )
        )

        return Response.success(response)
    }

    //유저정보 가져오기 api 부르기
    suspend fun getUserInfo(): Response<User> {
        val data = User(
            id = "1",
            isSignUp = false,
            nickname = "닉네임3",
            profileImageUrl = ExamplePictureUrl,
            email = "email@gmail.com",
            sinceDays = 123,
            createdDt = "2026-07-28",
            updatedDt = "2026-07-28",
        )

        return Response.success(data)
    }

    //유저 회원정보수정 정보 가져오기 api 부르기
    suspend fun getUserInfoDetail(): Response<SignUpForm> {
        val data = SignUpForm(
            nickname = "닉네임8",
            email = "email@gmail.com",
            password = "",
            passwordCheck = "",
            gender = Gender.Male,
            birthday = "1997.05.16",
            height = "170",
            weight = "70",
            diabetesType = "제2형 당뇨",
            hemoglobin = "50",
            isHemoglobinRecentResultUnknown = false,
            goalGlucose = "180",
            activityLevel = "주 1 ~2회"
        )

        return Response.success(data)
    }

    //회원가입/회원정보수정 완료 api 부르기
    suspend fun userInfoUpdate(signUpForm: SignUpForm?): Response<TokenResponse>{
        val response = TokenResponse(
            accessToken = "AccessToken",
            refreshToken = "RefreshToken",
            user = User(
                id = "1",
                isSignUp = false,
                nickname = "닉네임",
                profileImageUrl = ExamplePictureUrl,
                email = "email@gmail.com",
                sinceDays = 120,
                createdDt = "2026-07-28",
                updatedDt = "2026-07-28",
            )
        )

        return Response.success(response)
    }
}
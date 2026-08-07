package com.dangdang.data.repository

import com.dangdang.Application.Companion.ExamplePictureUrl
import com.dangdang.data.api.UserApiService
import com.dangdang.data.enums.Gender
import com.dangdang.data.model.home.AfterMealGlucoseStatusModel
import com.dangdang.data.model.home.WeeklyGlucoseCheckModel
import com.dangdang.data.model.user.SignUpForm
import com.dangdang.data.model.user.TokenResponse
import com.dangdang.data.model.user.User
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userApiService: UserApiService
){
    //이메일 로그인 api 부르기
    suspend fun emailLogin(email: String, password: String): Response<TokenResponse>{
        if(email == "email@gmail.com" && password == "1234567"){
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
        }else{
            val errorJson = """
                {
                    "message": "Not Authorized"
                }
            """.trimIndent()

            return Response.error(
                401,
                errorJson.toResponseBody("application/json".toMediaType())
            )
        }
    }

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

    //카카오 로그인 api 부르기
    suspend fun kakaoLogin(token: String): Response<TokenResponse> {
        val response = TokenResponse(
            accessToken = "AccessToken",
            refreshToken = "RefreshToken",
            user = User(
                id = "1",
                isSignUp = true,
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
            isSocial = true,
            nickname = "닉네임8",
            email = "email@gmail.com",
            password = "",
            passwordCheck = "",
            gender = Gender.Male,
            birthday = "1997.05.16",
            height = "170",
            weight = "70",
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

    //주간 혈당 관리 현황 api 부르기
    suspend fun getWeeklyGlucoseCheckList(): Response<List<WeeklyGlucoseCheckModel>>{
        val response = listOf(
            WeeklyGlucoseCheckModel(
                dayOfWeek = "월",
                isGlucoseManagement = false
            ),
            WeeklyGlucoseCheckModel(
                dayOfWeek = "화",
                isGlucoseManagement = true
            ),
            WeeklyGlucoseCheckModel(
                dayOfWeek = "수",
                isGlucoseManagement = false
            ),
            WeeklyGlucoseCheckModel(
                dayOfWeek = "목",
                isGlucoseManagement = false
            ),
            WeeklyGlucoseCheckModel(
                dayOfWeek = "금",
                isGlucoseManagement = false
            ),
            WeeklyGlucoseCheckModel(
                dayOfWeek = "토",
                isGlucoseManagement = false
            ),
            WeeklyGlucoseCheckModel(
                dayOfWeek = "일",
                isGlucoseManagement = false
            )
        )

        return Response.success(response)
    }

    //식후 혈당 추이 부르기
    suspend fun getAfterMealGlucoseStatus(): Response<AfterMealGlucoseStatusModel>{
        val response = AfterMealGlucoseStatusModel(
            goal = 180f,
            afterMealGlucoseStatus = listOf(155f, 148f, 168f, 158f, 178f, 152f, 160f, 160f, 160f, 160f, 160f, 160f, 160f, 160f, 160f, 160f, 160f, 160f, 160f)
        )
        return Response.success(response)
    }
}
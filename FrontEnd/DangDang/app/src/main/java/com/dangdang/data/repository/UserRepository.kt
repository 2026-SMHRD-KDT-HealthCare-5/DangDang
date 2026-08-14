package com.dangdang.data.repository

import com.dangdang.Application.Companion.ExamplePictureUrl
import com.dangdang.common.utils.diagnosisGroupList
import com.dangdang.common.utils.safeApiCall
import com.dangdang.data.api.LoginApiService
import com.dangdang.data.api.UserApiService
import com.dangdang.data.enums.Gender
import com.dangdang.data.enums.WeeklyAttendanceStatus
import com.dangdang.data.model.home.AfterMealGlucoseStatusModel
import com.dangdang.data.model.home.WeeklyGlucoseCheckModel
import com.dangdang.data.model.user.LoginForm
import com.dangdang.data.model.user.SignUpForm
import com.dangdang.data.model.user.SignUpResponse
import com.dangdang.data.model.user.TokenResponse
import com.dangdang.data.model.user.User
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userApiService: UserApiService,
    private val loginApiService: LoginApiService
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
    suspend fun getUserInfoDetail(): Response<SignUpForm> {
        val data = SignUpForm(
            isSocial = true,
            nickname = "닉네임8",
            email = "email@gmail.com",
            password = "",
            passwordCheck = "",
            gender = Gender.male.name,
            birthDate = "1997.05.16",
            height = "170",
            weight = "70",
            hba1c = "12",
            isHemoglobinRecentResultUnknown = false,
            targetGlucose = "180",
            activityLevel = "주 1 ~2회",
            joined_at = "2026-07-28",
            profileImageUrl = ExamplePictureUrl,
            notification_enabled = true,
            diagnosisGroup = diagnosisGroupList[0]
        )

        return Response.success(data)
    }

    //회원정보수정 완료 api 부르기
    suspend fun userInfoUpdate(signUpForm: SignUpForm?): Response<User>{
        val response = User(
            id = "1",
            isSignUp = false,
            nickname = "닉네임",
            profileImageUrl = ExamplePictureUrl,
            email = "email@gmail.com",
            sinceDays = 120,
            createdDt = "2026-07-28",
            updatedDt = "2026-07-28",
        )

        return Response.success(response)
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
    suspend fun setNotification(enabled: Boolean): Response<String>{
        return Response.success("success")
    }

    //주간 혈당 관리 현황 api 부르기
    suspend fun getWeeklyGlucoseCheckList(): Response<List<WeeklyGlucoseCheckModel>>{
        val response = listOf(
            WeeklyGlucoseCheckModel(
                day = "월",
                status = WeeklyAttendanceStatus.MISSED.name
            ),
            WeeklyGlucoseCheckModel(
                day = "화",
                status = WeeklyAttendanceStatus.DONE.name
            ),
            WeeklyGlucoseCheckModel(
                day = "수",
                status = WeeklyAttendanceStatus.NONE.name
            ),
            WeeklyGlucoseCheckModel(
                day = "목",
                status = WeeklyAttendanceStatus.NONE.name
            ),
            WeeklyGlucoseCheckModel(
                day = "금",
                status = WeeklyAttendanceStatus.NONE.name
            ),
            WeeklyGlucoseCheckModel(
                day = "토",
                status = WeeklyAttendanceStatus.NONE.name
            ),
            WeeklyGlucoseCheckModel(
                day = "일",
                status = WeeklyAttendanceStatus.NONE.name
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
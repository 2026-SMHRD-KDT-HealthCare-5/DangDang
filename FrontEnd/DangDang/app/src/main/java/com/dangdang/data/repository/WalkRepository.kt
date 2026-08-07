package com.dangdang.data.repository

import com.dangdang.Application.Companion.ExamplePictureUrl
import com.dangdang.data.api.UserApiService
import com.dangdang.data.model.user.TokenResponse
import com.dangdang.data.model.user.User
import com.dangdang.data.model.walk.WalkStatus
import retrofit2.Response
import javax.inject.Inject

class WalkRepository @Inject constructor(

){
    //현황 불러오기
    suspend fun getWalkStatus(): Response<WalkStatus> {
        val response = WalkStatus(
            walkTargetDistance = 2.6f,
            currentWalkDistance = 0.85f,
            currentWalkCount = 10,
            currentWalkKcal = 20
        )

        return Response.success(response)
    }
}
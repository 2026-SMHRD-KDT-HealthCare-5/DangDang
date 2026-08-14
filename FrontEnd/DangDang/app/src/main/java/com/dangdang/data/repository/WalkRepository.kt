package com.dangdang.data.repository

import com.dangdang.Application.Companion.ExamplePictureUrl
import com.dangdang.data.api.UserApiService
import com.dangdang.data.api.WalkApiService
import com.dangdang.data.model.user.TokenResponse
import com.dangdang.data.model.user.User
import com.dangdang.data.model.walk.WalkStatus
import retrofit2.Response
import javax.inject.Inject

class WalkRepository @Inject constructor(
    private val walkApiService: WalkApiService
){
    //현황 불러오기
    suspend fun getWalkStatus(): Response<WalkStatus> {
        val response = WalkStatus(
            missionNo = 1,
            walkTargetDistance = 2.6f,
            currentWalkDistance = 0f,
            currentWalkCount = 0,
            currentWalkKcal = 0
        )

        return Response.success(response)
    }

    //걷기 미션 종료
    suspend fun endWalkMission(missionNo: Int): Response<String>{
        return Response.success("success")
    }
}
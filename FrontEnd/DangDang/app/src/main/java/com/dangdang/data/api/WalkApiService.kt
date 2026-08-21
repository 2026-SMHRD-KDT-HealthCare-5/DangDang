package com.dangdang.data.api

import com.dangdang.Application.Companion.WalkMissionPath
import com.dangdang.data.model.walk.WalkExpireInputForm
import com.dangdang.data.model.walk.WalkExpireResponse
import com.dangdang.data.model.walk.WalkStatus
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface WalkApiService {
    //미션 상황 가져오기
    @GET("${WalkMissionPath}/active")
    suspend fun getWalkStatus(): Response<WalkStatus>

    //걷기 미션 강제 종료
    @POST("${WalkMissionPath}/{missionNo}/expire")
    suspend fun expireWalkMission(
        @Path("missionNo") missionNo: Int,
        @Body walkExpireInputForm: WalkExpireInputForm
    ): Response<WalkExpireResponse>
}
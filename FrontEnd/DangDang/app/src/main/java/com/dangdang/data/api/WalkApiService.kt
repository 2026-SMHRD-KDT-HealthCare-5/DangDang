package com.dangdang.data.api

import com.dangdang.Application.Companion.WalkMissionPath
import com.dangdang.data.model.walk.PostWalkGlucoseInputForm
import com.dangdang.data.model.walk.PostWalkGlucoseResponse
import com.dangdang.data.model.walk.WalkExpireInputForm
import com.dangdang.data.model.walk.WalkExpireResponse
import com.dangdang.data.model.walk.WalkMissionEndResponse
import com.dangdang.data.model.walk.WalkMissionTrackingInputForm
import com.dangdang.data.model.walk.WalkMissionTrackingResponse
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

    //걷기 미션 시작
    @POST("${WalkMissionPath}/{missionNo}/start")
    suspend fun startWalkMission(
        @Path("missionNo") missionNo: Int
    ): Response<WalkStatus>

    //걷기 미션 폴링(트래킹)
    @POST("${WalkMissionPath}/{missionNo}/track")
    suspend fun trackWalkMission(
        @Path("missionNo") missionNo: Int,
        @Body walkMissionTrackingInputForm: WalkMissionTrackingInputForm
    ): Response<WalkMissionTrackingResponse>

    //걷기 미션 종료
    @POST("${WalkMissionPath}/{missionNo}/end")
    suspend fun endWalkMission(
        @Path("missionNo") missionNo: Int,
    ): Response<WalkMissionEndResponse>

    //걷기 미션 강제 종료
    @POST("${WalkMissionPath}/{missionNo}/expire")
    suspend fun expireWalkMission(
        @Path("missionNo") missionNo: Int,
        @Body walkExpireInputForm: WalkExpireInputForm
    ): Response<WalkExpireResponse>

    //식후 혈당 입력
    @POST("${WalkMissionPath}/{missionNo}/post-glucose")
    suspend fun postWalkGlucose(
        @Path("missionNo") missionNo: Int,
        @Body postWalkGlucoseInputForm: PostWalkGlucoseInputForm
    ): Response<PostWalkGlucoseResponse>
}
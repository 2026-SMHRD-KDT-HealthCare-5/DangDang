package com.dangdang.data.repository

import com.dangdang.common.utils.safeApiCall
import com.dangdang.data.api.WalkApiService
import com.dangdang.data.model.walk.WalkExpireInputForm
import com.dangdang.data.model.walk.WalkExpireResponse
import com.dangdang.data.model.walk.WalkMissionEndResponse
import com.dangdang.data.model.walk.WalkMissionTrackingInputForm
import com.dangdang.data.model.walk.WalkMissionTrackingResponse
import com.dangdang.data.model.walk.WalkStatus
import retrofit2.Response
import javax.inject.Inject

class WalkRepository @Inject constructor(
    private val walkApiService: WalkApiService
){
    //현황 불러오기
    suspend fun getWalkStatus(): Response<WalkStatus> = safeApiCall {
        walkApiService.getWalkStatus()
    }

    //걷기 미션 시작
    suspend fun startWalkMission(missionNo: Int): Response<WalkStatus> = safeApiCall {
        walkApiService.startWalkMission(missionNo)
    }

    //걷기 미션 폴링(트래킹)
    suspend fun trackWalkMission(
        missionNo: Int,
        walkMissionTrackingInputForm: WalkMissionTrackingInputForm
    ): Response<WalkMissionTrackingResponse> = safeApiCall {
        walkApiService.trackWalkMission(missionNo, walkMissionTrackingInputForm)
    }

    //걷기 미션 강제 종료
    suspend fun expireWalkMission(
        missionNo: Int,
        walkExpireInputForm: WalkExpireInputForm
    ): Response<WalkExpireResponse> = safeApiCall {
        walkApiService.expireWalkMission(missionNo, walkExpireInputForm)
    }

    //걷기 미션 종료
    suspend fun endWalkMission(missionNo: Int): Response<WalkMissionEndResponse> = safeApiCall {
        walkApiService.endWalkMission(missionNo)
    }
}
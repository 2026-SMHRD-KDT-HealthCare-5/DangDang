package com.dangdang.data.repository

import com.dangdang.common.utils.safeApiCall
import com.dangdang.data.api.WalkApiService
import com.dangdang.data.model.walk.WalkExpireInputForm
import com.dangdang.data.model.walk.WalkExpireResponse
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

    //걷기 미션 강제 종료
    suspend fun expireWalkMission(
        missionNo: Int,
        walkExpireInputForm: WalkExpireInputForm
    ): Response<WalkExpireResponse> = safeApiCall {
        walkApiService.expireWalkMission(missionNo, walkExpireInputForm)
    }

    //걷기 미션 종료
    suspend fun endWalkMission(missionNo: Int): Response<String>{
        return Response.success("success")
    }
}
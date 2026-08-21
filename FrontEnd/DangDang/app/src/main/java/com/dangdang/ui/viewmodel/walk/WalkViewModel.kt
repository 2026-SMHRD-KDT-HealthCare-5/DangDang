package com.dangdang.ui.viewmodel.walk

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dangdang.common.utils.StopStepCounting
import com.dangdang.common.utils.getMeterToKm
import com.dangdang.data.enums.WalkMissionExpiredReason
import com.dangdang.data.enums.WalkMissionStatus
import com.dangdang.data.repository.WalkRepository
import com.dangdang.data.manager.StepCounterManager
import com.dangdang.data.model.walk.WalkExpireInputForm
import com.dangdang.data.model.walk.WalkStatus
import com.dangdang.data.service.StepCounterService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class WalkViewModel @Inject constructor(
    private val walkRepository: WalkRepository
): ViewModel(){

    //현황 불러오기
    fun getWalkStatus(context: Context){
        viewModelScope.launch {
            val response = walkRepository.getWalkStatus()
            if(response.isSuccessful){
                val walkStatus = response.body()

                if(walkStatus?.status == WalkMissionStatus.IN_PROGRESS.name){
                    //미션 종료 처리
                    val expireResponse = walkRepository.expireWalkMission(
                        missionNo = walkStatus.missionNo,
                        walkExpireInputForm = WalkExpireInputForm(
                            expireReason = WalkMissionExpiredReason.CANCELLED.name,
                            actualDistance = walkStatus.actualDistance
                        )
                    )

                    if(expireResponse.isSuccessful){
                        val expireResponseBody = expireResponse.body()
                        Toast.makeText(context, expireResponseBody?.noticeMessage, Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(context, "미션 종료 처리를 하는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    StepCounterManager.loadWalkState(walkStatus)
                }
            }else{
                StepCounterManager.walkStateLoadingErrorProcess()
            }
        }
    }


    fun startStepCounting(
        context: Context,
        currentStepCount: Int,
        walkStatus: WalkStatus
    ) {
        if(walkStatus.status == WalkMissionStatus.READY.name){
            val intent =
                Intent(
                    context,
                    StepCounterService::class.java
                ).apply {
                    action =
                        StepCounterService.ACTION_START
                }
            intent.putExtra("currentStep", currentStepCount)
            intent.putExtra("missionNo", walkStatus.missionNo)

            ContextCompat.startForegroundService(
                context,
                intent
            )
        }else{
            Toast.makeText(context, "걷기 미션이 없거나 종료되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    fun stopStepCounting(
        context: Context,
        walkStatus: WalkStatus
    ) {
        StopStepCounting(context, walkStatus.missionNo)
    }

    fun endWalkMission(missionNo: Int){
        viewModelScope.launch {
            walkRepository.endWalkMission(missionNo)
        }
    }
}
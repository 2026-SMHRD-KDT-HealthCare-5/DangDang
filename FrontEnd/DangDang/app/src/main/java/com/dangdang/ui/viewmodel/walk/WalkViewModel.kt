package com.dangdang.ui.viewmodel.walk

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dangdang.data.repository.WalkRepository
import com.dangdang.di.StepCounterManager
import com.dangdang.di.StepCounterService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalkViewModel @Inject constructor(
    private val walkRepository: WalkRepository
): ViewModel(){

    init {
        if(!StepCounterManager.isWalking.value){
            getWalkStatus()
        }
    }

    //현황 불러오기
    fun getWalkStatus(){
        viewModelScope.launch {
            val response = walkRepository.getWalkStatus()
            if(response.isSuccessful){
                val walkStatus = response.body()

                StepCounterManager.updateWalkTarget(walkStatus?.walkTargetDistance?:0f)
                StepCounterManager.updateStepCount(walkStatus?.currentWalkCount?:0)
            }
        }
    }


    fun startStepCounting(
        context: Context,
        currentStepCount: Int,
        missionNo: Int = 1
    ) {

        val intent =
            Intent(
                context,
                StepCounterService::class.java
            ).apply {
                action =
                    StepCounterService.ACTION_START
            }
        intent.putExtra("currentStep", currentStepCount)
        intent.putExtra("missionNo", missionNo)

        ContextCompat.startForegroundService(
            context,
            intent
        )
    }

    fun stopStepCounting(
        context: Context
    ) {

        val intent =
            Intent(
                context,
                StepCounterService::class.java
            ).apply {

                action =
                    StepCounterService.ACTION_STOP
            }

        context.startService(
            intent
        )
    }
}
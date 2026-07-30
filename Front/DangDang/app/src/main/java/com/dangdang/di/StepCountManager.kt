package com.dangdang.di

import com.dangdang.data.model.walk.WalkStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object StepCounterManager {

    private val _walkStatus = MutableStateFlow(
        WalkStatus(
            walkTargetDistance = 0f,
            currentWalkDistance = 0f,
            currentWalkCount = 0,
            currentWalkKcal = 0
        )
    )
    val walkStatus: StateFlow<WalkStatus> =
        _walkStatus.asStateFlow()

    //초 단위 걸은 시간
    private val _stepTime = MutableStateFlow(0)
    val stepTime: StateFlow<Int> =
        _stepTime.asStateFlow()

    private val _isWalking = MutableStateFlow(false)
    val isWalking: StateFlow<Boolean> =
        _isWalking.asStateFlow()

    fun updateWalkingState(isWalking: Boolean) {
        _isWalking.value = isWalking
    }

    fun updateWalkTarget(walkTargetDistance: Float){
        _walkStatus.value = _walkStatus.value.copy(
            walkTargetDistance = walkTargetDistance
        )
    }

    fun updateStepCount(stepCount: Int) {
        _walkStatus.value = _walkStatus.value.copy(
            currentWalkCount = stepCount,
            currentWalkDistance = stepCount * 0.0007f,
            currentWalkKcal = (stepCount * 0.04f).toInt(),
        )
    }

    fun updateStepTime(stepTime: Int){
        _stepTime.value = stepTime
    }

    fun resetStepTime() {
        _stepTime.value = 0
    }

    fun reset() {
        _walkStatus.value = _walkStatus.value.copy(
            currentWalkCount = 0,
            currentWalkDistance = 0f,
            currentWalkKcal = 0,
        )
    }
}
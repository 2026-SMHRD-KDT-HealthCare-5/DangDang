package com.dangdang.data.manager

import com.dangdang.common.utils.WalkStatusDefault
import com.dangdang.common.utils.getMeterToKm
import com.dangdang.common.utils.getWalkKcal
import com.dangdang.data.enums.WalkMissionStatus
import com.dangdang.data.model.walk.WalkStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

object StepCounterManager {

    private val _walkStatus = MutableStateFlow(
        WalkStatusDefault
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

    // 궤적 포인트 (Kakao Map LatLng 대신 Pair로 관리, UI에서 LatLng로 변환 가능)
    private val _routePoints = MutableStateFlow<List<Pair<Double, Double>>>(emptyList())
    val routePoints: StateFlow<List<Pair<Double, Double>>> = _routePoints.asStateFlow()

    private val _isEndWalk = MutableStateFlow(false)
    val isEndWalk: StateFlow<Boolean> = _isEndWalk.asStateFlow()

    private val _isWalkEndDialog = MutableStateFlow(false)
    val isWalkEndDialog: StateFlow<Boolean> = _isWalkEndDialog.asStateFlow()

    fun endWalkMission(missionNo: Int){
        _isEndWalk.value = true
        _isWalkEndDialog.value = true
    }

    fun closeWalkEndDialog(){
        _isWalkEndDialog.value = false
    }

    fun updateWalkingState(isWalking: Boolean) {
        _isWalking.value = isWalking
        if (!isWalking) {
            // 종료 시 궤적은 메모리에서 버림
            _routePoints.value = emptyList()
        }
    }

    fun updateWalkTarget(walkTargetDistance: Float){
        _walkStatus.value = _walkStatus.value.copy(
            targetDistance = walkTargetDistance
        )
    }

    fun updateStepCount(stepCount: Int) {
        _walkStatus.value = _walkStatus.value.copy(
            currentWalkCount = stepCount,
            currentWalkKcal = getWalkKcal(stepCount),
        )
    }

    fun updateWalkDistance(distance: Float) {
        _walkStatus.value = _walkStatus.value.copy(
            actualDistance = distance
        )
    }

    fun addRoutePoint(lat: Double, lng: Double) {
        _routePoints.value = _routePoints.value + Pair(lat, lng)
    }

    fun updateStepTime(stepTime: Int){
        _stepTime.value = stepTime
    }

    fun resetStepTime() {
        _stepTime.value = 0
    }

    fun reset() {
        _walkStatus.value = WalkStatusDefault
    }

    fun walkStateLoadingErrorProcess(){
        _walkStatus.value = _walkStatus.value.copy(
            status = WalkMissionStatus.LoadingError.name
        )
    }

    fun loadWalkState(loadedWalkStatus: WalkStatus?){
        _walkStatus.value = _walkStatus.value.copy(
            missionNo = loadedWalkStatus?.missionNo?:0,
            status = loadedWalkStatus?.status?:"",
            targetDistance = loadedWalkStatus?.targetDistance?:0f,
            currentWalkCount = loadedWalkStatus?.currentWalkCount?:0,
            currentWalkKcal = getWalkKcal(loadedWalkStatus?.currentWalkCount?:0),
            actualDistance = getMeterToKm(loadedWalkStatus?.actualDistance?.toDouble()?:0.0).toFloat(),
        )

        if(loadedWalkStatus?.status == WalkMissionStatus.IN_PROGRESS.name){
            val startTime = LocalDateTime.parse(loadedWalkStatus.startTime)
            _stepTime.value = ChronoUnit.SECONDS.between(
                startTime,
                LocalDateTime.now()
            ).toInt()
        }
    }
}
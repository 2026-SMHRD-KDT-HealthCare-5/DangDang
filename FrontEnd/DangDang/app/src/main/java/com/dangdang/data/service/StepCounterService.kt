package com.dangdang.data.service

import android.R
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.dangdang.common.utils.AppPrefs
import com.dangdang.common.utils.getMeterToKm
import com.dangdang.common.utils.getWalkKcal
import com.dangdang.data.enums.WalkMissionExpiredReason
import com.dangdang.data.enums.WalkMissionStatus
import com.dangdang.data.manager.StepCounterManager
import com.dangdang.data.model.walk.WalkExpireInputForm
import com.dangdang.data.model.walk.WalkMissionTrackingInputForm
import com.dangdang.data.repository.UserRepository
import com.dangdang.data.repository.WalkRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@AndroidEntryPoint
class StepCounterService : Service(), SensorEventListener {

    @Inject
    lateinit var appPrefs: AppPrefs

    @Inject
    lateinit var walkRepository: WalkRepository

    @Inject
    lateinit var userRepository: UserRepository

    private val serviceScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var missionNo: Int = -1
    private var startStepCount = -1f
    private var baseStepCount = 0
    private var totalDistance = 0f
    private var lastLocation: Location? = null

    private var timerJob: Job? = null
    private var elapsedSecond = 0
    private var lastMeaningfulMovementTime = 0L
    private var lastActiveStepCount = 0
    private var isGoalReachedNotified = false

    @SuppressLint("ForegroundServiceType")
    override fun onCreate() {
        super.onCreate()

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        createLocationCallback()
        createNotificationChannel()
        createWarningNotificationChannel()

        startForeground(
            NOTIFICATION_ID,
            createNotification(0, 0f)
        )
    }

    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            //좌표가 변할 경우의 콜백
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    processLocation(location)
                }
            }
        }
    }

    private fun processLocation(location: Location) {
        // 필터링: accuracy 50m 초과 무시
        if (location.accuracy > 50) return

        val lastLoc = lastLocation
        if (lastLoc != null) {
            val distance = lastLoc.distanceTo(location)
            val timeDelta = (location.time - lastLoc.time) / 1000.0 // seconds
            val speed = if (timeDelta > 0) (distance / timeDelta) * 3.6 else 0.0 // km/h

            // 활동 감지: 5m 이상 이동 시 타이머 리셋
            if (distance >= 5.0) {
                lastMeaningfulMovementTime = System.currentTimeMillis()
                lastActiveStepCount = StepCounterManager.walkStatus.value.currentWalkCount
            }

            // 필터링: 이동거리 3m 미만 무시(궤적용), 속도 범위 벗어남 무시
            if (distance < 3 || speed < 0.5 || speed > 15.0) return

            totalDistance += distance
            // m 단위를 km 단위로 변환하여 매니저에 업데이트
            updateDistance()
            StepCounterManager.addRoutePoint(location.latitude, location.longitude)

            // 마지막 유의미한 이동 시각 갱신
            lastMeaningfulMovementTime = System.currentTimeMillis()
        } else {
            // 첫 좌표
            lastMeaningfulMovementTime = System.currentTimeMillis()
            StepCounterManager.addRoutePoint(location.latitude, location.longitude)
        }
        lastLocation = location
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val currentStep = intent?.getIntExtra("currentStep", 0) ?: 0
        missionNo = intent?.getIntExtra("missionNo", -1) ?: -1

        when (intent?.action) {
            ACTION_START -> startStepCounting(currentStep)
            ACTION_STOP -> stopStepCounting(true)
        }

        return START_NOT_STICKY
    }

    private fun startStepCounting(currentStepCount: Int) {
        val sensor = stepSensor ?: run {
            stopSelf()
            return
        }

        serviceScope.launch {
            startWalkMission(
                no = missionNo,
                onSuccess = { sensorEventListener ->
                    baseStepCount = currentStepCount
                    startStepCount = -1f
                    totalDistance = 0f
                    lastLocation = null
                    lastMeaningfulMovementTime = System.currentTimeMillis()
                    lastActiveStepCount = currentStepCount
                    isGoalReachedNotified = false

                    sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
                    startLocationUpdates()

                    elapsedSecond = 0
                    StepCounterManager.resetStepTime()
                    StepCounterManager.updateWalkingState(true)
                    StepCounterManager.updateWalkDistance(0f)
                    StepCounterManager.updateWalkKcal(0)

                    startTimer()
                }
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(2000)
            .build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_STEP_COUNTER) return

        val currentStepCount = event.values[0]
        if (startStepCount == -1f) {
            startStepCount = currentStepCount
            StepCounterManager.updateStepCount(0)
        }

        val currentWalkingStepCount = (currentStepCount - startStepCount).toInt()
        val totalStepCount = baseStepCount + currentWalkingStepCount
        StepCounterManager.updateStepCount(totalStepCount)

        // 활동 감지: 10걸음 이상 증가 시 타이머 리셋
        if (totalStepCount - lastActiveStepCount >= 10) {
            lastMeaningfulMovementTime = System.currentTimeMillis()
            lastActiveStepCount = totalStepCount
        }

        updateNotification(totalStepCount, totalDistance)
    }

    private fun stopStepCounting(isEndWalkMission: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            if(isEndWalkMission){
                endWalkMission(
                    no = missionNo,
                    onSuccess = {
                        withContext(Dispatchers.Main) {
                            stopProcess()
                        }
                    }
                )
            }else{
                withContext(Dispatchers.Main) {
                    stopProcess()
                }
            }
        }
    }

    private fun stopProcess(){
        sensorManager.unregisterListener(this@StepCounterService)
        fusedLocationClient.removeLocationUpdates(locationCallback)
        timerJob?.cancel()
        StepCounterManager.updateWalkingState(false)
        StepCounterManager.updateWalkStatus(WalkMissionStatus.EXPIRED.name)
        stopSelf()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        timerJob?.cancel()
        serviceScope.cancel()
        sensorManager.unregisterListener(this)
        fusedLocationClient.removeLocationUpdates(locationCallback)
        StepCounterManager.updateWalkingState(false)
        super.onDestroy()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                delay(1000.milliseconds)
                elapsedSecond++
                StepCounterManager.updateStepTime(elapsedSecond)

                // 10초 주기 서버 폴링
                if (elapsedSecond % 10 == 0) {
                    trackWalkMission(missionNo, lastLocation)
                }

                // 미동작 감지 (자체 타이머)
                val inactiveMillis = System.currentTimeMillis() - lastMeaningfulMovementTime

                // 10분 경과 -> 로컬 알림
                if (inactiveMillis >= 10 * 60 * 1000L && inactiveMillis < 10 * 60 * 1000L + 1000L) {
                    if (appPrefs.isNotificationEnabled()) {
                        showInactivityNotification("계속 걷고 계신가요?")
                    }
                }

                // 30분 경과 -> 세션 만료 서버 호출
                if (inactiveMillis >= 30 * 60 * 1000L) {
                    expireWalkMission(
                        no = missionNo,
                        onSuccess = {
                            withContext(Dispatchers.Main) {
                                stopStepCounting(false)
                            }
                        }
                    )
                    break
                }
            }
        }
    }

    // --- API Calls ---

    private suspend fun startWalkMission(no: Int, onSuccess: (SensorEventListener)-> Unit){
        if(no == -1) return
        val response = walkRepository.startWalkMission(no)
        val userInfoResponse = userRepository.getUserInfoDetail()
        if(response.isSuccessful && userInfoResponse.isSuccessful){
            val userInfo = userInfoResponse.body()
            StepCounterManager.initUserInfo(userInfo)

            onSuccess(this)
        }else{
            Toast.makeText(this, "걷기 미션을 시작하는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateDistance(){
        val kmDistance = getMeterToKm(totalDistance)
        StepCounterManager.updateWalkDistance(kmDistance)
        StepCounterManager.updateWalkKcal(getWalkKcal(
            distance = kmDistance,
            seconds = elapsedSecond,
            userInfo = StepCounterManager.userInfo.value
        ))
    }

    //폴링 처리
    private suspend fun trackWalkMission(no: Int, location: Location?) {
        if (no == -1 || location == null) return

        var responseCount = 0

        while(true){
            val response = walkRepository.trackWalkMission(
                missionNo = no,
                walkMissionTrackingInputForm = WalkMissionTrackingInputForm(
                    latitude = location.latitude.toFloat(),
                    longitude = location.longitude.toFloat(),
                    currentDistance = totalDistance
                )
            )
            if(response.isSuccessful){
                val responseBody = response.body()

                //anomalyDetected가 true일 경우 현재 거리를 서버의 거리로 리셋한다.
                if(responseBody?.anomalyDetected == true){
                    val walkStatusResponse = walkRepository.getWalkStatus()
                    if(walkStatusResponse.isSuccessful){
                        val walkStatusResponseBody = walkStatusResponse.body()
                        totalDistance = walkStatusResponseBody?.actualDistance?:0f
                        updateDistance()
                        showTrackNotification(false)
                    }else{
                        stopStepCounting(false)
                        StepCounterManager.walkStateLoadingErrorProcess()
                    }
                }else{
                    showTrackNotification(true)
                    //목표 달성 시 자동 종료
                    val goalReached = responseBody?.goalReached
                    if (goalReached == true && !isGoalReachedNotified) {
                        isGoalReachedNotified = true
                        if (appPrefs.isNotificationEnabled()) {
                            showGoalReachedNotification()
                        }
                        stopStepCounting(true)
                    }
                }

                break
            }

            //실패 시 처리
            responseCount ++
            if(responseCount >= 3){
                stopStepCounting(false)
                StepCounterManager.walkStateLoadingErrorProcess()
                break
            }
        }
    }

    private suspend fun expireWalkMission(no: Int, onSuccess: suspend () -> Unit) {
        if (no == -1) return
        val response = walkRepository.expireWalkMission(
            missionNo = no,
            walkExpireInputForm = WalkExpireInputForm(
                expireReason = WalkMissionExpiredReason.INACTIVE.name,
                actualDistance = totalDistance
            )
        )
        if(response.isSuccessful){
            Toast.makeText(this, "걷기 미션이 자동으로 만료되었습니다.", Toast.LENGTH_SHORT).show()
            onSuccess()
        }else{
            stopStepCounting(false)
            StepCounterManager.walkStateLoadingErrorProcess()
        }
    }

    private suspend fun endWalkMission(no: Int, onSuccess: suspend () -> Unit) {
        if (no == -1) return
        val response = walkRepository.endWalkMission(no)
        if(response.isSuccessful){
            StepCounterManager.endWalkMission()
            onSuccess()
        }else{
            Toast.makeText(this, "걷기 미션을 종료하는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Notifications ---

    private fun createNotification(stepCount: Int, distance: Float): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("걷기 측정 중")
            .setContentText("발걸음: ${stepCount}보 | 거리: ${String.format(Locale.getDefault(), "%.2f", distance / 1000f)}km")
            .setSmallIcon(R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(stepCount: Int, distance: Float) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification(stepCount, distance))
    }

    private fun showInactivityNotification(message: String) {
        val notification = NotificationCompat.Builder(this, WARNING_CHANNEL_ID)
            .setContentTitle("걷기 안내")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_menu_mylocation)
            .setAutoCancel(true)
            .build()
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(MOVE_NOTIFICATION_ID, notification)
    }

    private fun showGoalReachedNotification() {
        val notification = NotificationCompat.Builder(this, WARNING_CHANNEL_ID)
            .setContentTitle("목표 달성!")
            .setContentText("축하합니다! 걷기 목표를 달성하셨습니다.")
            .setSmallIcon(R.drawable.ic_menu_mylocation)
            .setAutoCancel(true)
            .build()
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(GOAL_NOTIFICATION_ID, notification)
    }

    private fun showTrackNotification(isSaved: Boolean){
        val notification = NotificationCompat.Builder(this, WARNING_CHANNEL_ID)
            .setContentTitle("걷기 미션 저장 안내")
            .setContentText(
                if(isSaved) "걷기 미션이 성공적으로 저장되었어요!"
                else "이동 속도가 너무 빨라서 이번 구간은 기록에서 제외됐어요"
            )
            .setSmallIcon(R.drawable.ic_menu_mylocation)
            .setAutoCancel(true)
            .build()
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(SAVE_NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "걷기 측정", NotificationManager.IMPORTANCE_LOW)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun createWarningNotificationChannel() {
        val channel =
            NotificationChannel(WARNING_CHANNEL_ID, "걷기 측정", NotificationManager.IMPORTANCE_HIGH)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "ACTION_START_STEP_COUNTING"
        const val ACTION_STOP = "ACTION_STOP_STEP_COUNTING"
        private const val CHANNEL_ID = "step_counter_channel"
        private const val WARNING_CHANNEL_ID = "warning_channel"
        private const val NOTIFICATION_ID = 1001
        private const val MOVE_NOTIFICATION_ID = 1002
        private const val GOAL_NOTIFICATION_ID = 1003
        private const val SAVE_NOTIFICATION_ID = 1004
    }
}
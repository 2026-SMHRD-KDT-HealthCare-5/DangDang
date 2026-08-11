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
import androidx.core.app.NotificationCompat
import com.dangdang.data.manager.StepCounterManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

class StepCounterService : Service(), SensorEventListener {

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
            StepCounterManager.updateWalkDistance(totalDistance / 1000f)
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
            ACTION_STOP -> stopStepCounting()
        }

        return START_NOT_STICKY
    }

    private fun startStepCounting(currentStepCount: Int) {
        val sensor = stepSensor ?: run {
            stopSelf()
            return
        }

        baseStepCount = currentStepCount
        startStepCount = -1f
        totalDistance = 0f
        lastLocation = null
        lastMeaningfulMovementTime = System.currentTimeMillis()
        lastActiveStepCount = currentStepCount
        isGoalReachedNotified = false

        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        startLocationUpdates()

        elapsedSecond = 0
        StepCounterManager.resetStepTime()
        StepCounterManager.updateWalkingState(true)
        StepCounterManager.updateWalkDistance(0f)

        startTimer()
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

    private fun stopStepCounting() {
        CoroutineScope(Dispatchers.IO).launch {
            endWalkMission(missionNo)
            withContext(Dispatchers.Main) {
                sensorManager.unregisterListener(this@StepCounterService)
                fusedLocationClient.removeLocationUpdates(locationCallback)
                timerJob?.cancel()
                StepCounterManager.updateWalkingState(false)
                stopSelf()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        timerJob?.cancel()
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
                    showInactivityNotification("계속 걷고 계신가요?")
                }

                // 30분 경과 -> 세션 만료 서버 호출
                if (inactiveMillis >= 30 * 60 * 1000L) {
                    expireWalkMission(missionNo)
                    withContext(Dispatchers.Main) {
                        stopStepCounting()
                    }
                    break
                }
            }
        }
    }

    // --- Mock API Calls ---

    private fun trackWalkMission(no: Int, location: Location?) {
        if (no == -1 || location == null) return

        Log.d("WalkService", "Tracking mission $no at ${location.latitude}, ${location.longitude}")
        // POST /api/walk-missions/{no}/track
        // Mock Response handling
        val goalReached = false // from server response
        if (goalReached && !isGoalReachedNotified) {
            isGoalReachedNotified = true
            showGoalReachedNotification()
        }
    }

    private suspend fun expireWalkMission(no: Int) {
        if (no == -1) return
        Log.d("WalkService", "Expiring mission $no due to inactivity")
        // POST /walk-missions/{no}/expire (reason: inactive)
    }

    private suspend fun endWalkMission(no: Int) {
        if (no == -1) return
        StepCounterManager.endWalkMission(no)
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
    }
}
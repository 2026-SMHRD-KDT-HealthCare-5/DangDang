package com.dangdang.di

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class StepCounterService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager

    private var stepSensor: Sensor? = null

    /**
     * 걷기 시작 시점의 기기 누적 걸음 수
     */
    private var startStepCount = -1f
    private var baseStepCount = 0

    private var timerJob: Job? = null

    private var elapsedSecond = 0

    @SuppressLint("ForegroundServiceType")
    override fun onCreate() {
        super.onCreate()

        sensorManager =
            getSystemService(SENSOR_SERVICE) as SensorManager

        stepSensor =
            sensorManager.getDefaultSensor(
                Sensor.TYPE_STEP_COUNTER
            )

        createNotificationChannel()

        startForeground(
            NOTIFICATION_ID,
            createNotification(0)
        )
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        val currentStep = intent?.getIntExtra("currentStep", 0)

        when (intent?.action) {

            ACTION_START -> {
                startStepCounting(currentStep?: 0)
            }

            ACTION_STOP -> {
                stopStepCounting()
            }
        }

        return START_NOT_STICKY
    }

    private fun startStepCounting(currentStepCount: Int) {

        val sensor = stepSensor ?: run {
            stopSelf()
            return
        }

        baseStepCount = currentStepCount

        /**
         * 기존 값 초기화
         */
        startStepCount = -1f

        /**
         * 센서 등록
         */
        sensorManager.registerListener(
            this,
            sensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )

        elapsedSecond = 0

        StepCounterManager.resetStepTime()

        StepCounterManager.updateWalkingState(true)

        startTimer()
    }

    override fun onSensorChanged(
        event: SensorEvent?
    ) {
        if (event == null) return

        if (event.sensor.type != Sensor.TYPE_STEP_COUNTER) {
            return
        }

        val currentStepCount =
            event.values[0]

        /**
         * 첫 번째 센서 값
         *
         * 예:
         * 걷기 시작 당시 기기 누적 걸음 수 = 10,000
         */
        if (startStepCount == -1f) {

            startStepCount =
                currentStepCount

            StepCounterManager.updateStepCount(0)
        }

        /**
         * 이번 걷기에서 걸은 걸음 수
         *
         * 현재 누적값 - 시작 누적값
         */
        val currentWalkingStepCount =
            (currentStepCount - startStepCount).toInt()

        StepCounterManager.updateStepCount(
            baseStepCount + currentWalkingStepCount
        )

        updateNotification(
            currentWalkingStepCount
        )
    }

    private fun stopStepCounting() {
        sensorManager.unregisterListener(this)

        timerJob?.cancel()

        StepCounterManager.updateWalkingState(false)

        stopSelf()
    }

    override fun onAccuracyChanged(
        sensor: Sensor?,
        accuracy: Int
    ) {
        // 사용하지 않음
    }

    override fun onDestroy() {
        timerJob?.cancel()

        sensorManager.unregisterListener(this)

        StepCounterManager.updateWalkingState(false)

        super.onDestroy()
    }

    private fun startTimer() {
        timerJob?.cancel()

        timerJob = CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                delay(1000.milliseconds)

                elapsedSecond++

                StepCounterManager.updateStepTime(
                    elapsedSecond
                )
            }
        }
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? {
        return null
    }

    private fun createNotification(
        stepCount: Int
    ): Notification {

        return NotificationCompat.Builder(
            this,
            CHANNEL_ID
        )
            .setContentTitle("걷기 측정 중")
            .setContentText(
                "현재 걸음 수: ${stepCount}걸음"
            )
            .setSmallIcon(
                android.R.drawable.ic_menu_mylocation
            )
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(
        stepCount: Int
    ) {

        val notificationManager =
            getSystemService(
                NOTIFICATION_SERVICE
            ) as NotificationManager

        notificationManager.notify(
            NOTIFICATION_ID,
            createNotification(stepCount)
        )
    }

    private fun createNotificationChannel() {

        val channel =
            NotificationChannel(
                CHANNEL_ID,
                "걷기 측정",
                NotificationManager.IMPORTANCE_LOW
            )

        val notificationManager =
            getSystemService(
                NOTIFICATION_SERVICE
            ) as NotificationManager

        notificationManager.createNotificationChannel(
            channel
        )
    }

    companion object {

        const val ACTION_START =
            "ACTION_START_STEP_COUNTING"

        const val ACTION_STOP =
            "ACTION_STOP_STEP_COUNTING"

        private const val CHANNEL_ID =
            "step_counter_channel"

        private const val NOTIFICATION_ID =
            1001
    }
}
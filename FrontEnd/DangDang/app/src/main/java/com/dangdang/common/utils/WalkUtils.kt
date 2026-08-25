package com.dangdang.common.utils

import android.content.Context
import android.content.Intent
import com.dangdang.data.enums.WalkMissionStatus
import com.dangdang.data.model.user.SignUpForm
import com.dangdang.data.model.walk.WalkStatus
import com.dangdang.data.model.walk.WalkStatusItemTemplateModel
import com.dangdang.data.service.StepCounterService
import java.util.Locale

val WalkStatusDetailItemTemplates = listOf(
    WalkStatusItemTemplateModel(
        title = "시간",
        value = { _, stepTime ->
            val hours = stepTime / 3600
            val minutes = (stepTime % 3600) / 60
            val seconds = stepTime % 60
            String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
        },
        unit = null
    ),
    WalkStatusItemTemplateModel(
        title = "거리",
        value = { walkStatus, _ ->
            String.format(Locale.getDefault(), "%.2f", walkStatus.actualDistance)
        },
        unit = "km"
    ),
    WalkStatusItemTemplateModel(
        title = "걸음 수",
        value = { walkStatus, _ ->
            addComma(walkStatus.currentWalkCount)
        },
        unit = null
    ),
    WalkStatusItemTemplateModel(
        title = "칼로리",
        value = { walkStatus, _ ->
            addComma(walkStatus.currentWalkKcal)
        },
        unit = "kcal"
    )
)

val WalkStatusDefault = WalkStatus(
    missionNo = 0,
    targetDistance = 0f,
    actualDistance = 0f,
    currentWalkCount = 0,
    currentWalkKcal = 0,
    status = WalkMissionStatus.Loading.name,
    startTime = "",
    lastTrackedAt = "",
    createdAt = "",
)

fun StopStepCounting(
    context: Context,
    missionNo: Int
) {
    val intent =
        Intent(
            context,
            StepCounterService::class.java
        ).apply {

            action =
                StepCounterService.ACTION_STOP
        }
    intent.putExtra("missionNo", missionNo)

    context.startService(
        intent
    )
}

fun calculateSpeed(distance: Float, seconds: Int): Float {
    if (seconds <= 0) return 0.0f
    val hours = seconds / 3600.0f
    return distance / hours
}

fun getMet(speed: Float) : Float{
    return if(speed < 3.2f){
        2.0f
    }else if(speed in 3.2f..<4.8f){
        3.0f
    }else if(speed in 4.8f..<6.4f){
        4.3f
    }else{
        6.0f
    }
}

fun getWalkKcal(distance: Float, seconds: Int, userInfo: SignUpForm?): Int{
    val hours = seconds / 3600.0f
    val speed = calculateSpeed(distance, seconds)
    val met = getMet(speed)
    return (met * (userInfo?.weight?.toFloat()?:0f) * hours).toInt()
}
package com.dangdang.common.utils

import android.content.Context
import android.content.Intent
import com.dangdang.data.enums.WalkMissionStatus
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

fun getWalkKcal(stepCount: Int): Int{
    return (stepCount * 0.04f).toInt()
}
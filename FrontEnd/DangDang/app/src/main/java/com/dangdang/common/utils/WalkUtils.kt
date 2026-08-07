package com.dangdang.common.utils

import com.dangdang.data.model.walk.WalkStatusItemTemplateModel
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
            String.format(Locale.getDefault(), "%.2f", walkStatus.currentWalkDistance)
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
package com.dangdang.component.page.walk

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.common.utils.WalkStatusDetailItemTemplates
import com.dangdang.component.divider.Divider
import com.dangdang.component.map.KakaoMap
import com.dangdang.data.enums.DividerPosition
import com.dangdang.data.enums.WalkMissionStatus
import com.dangdang.data.model.walk.WalkStatus
import com.dangdang.ui.theme.Gray
import com.dangdang.ui.theme.MediumRoundShape
import com.dangdang.ui.theme.ThinLineDp

@Preview
@Composable
fun WalkInfoPreview(){
    WalkInfo(
        walkStatus = WalkStatus(
            missionNo = 1,
            targetDistance = 2.6f,
            actualDistance = 0.85f,
            currentWalkCount = 10,
            currentWalkKcal = 20,
            status = WalkMissionStatus.IN_PROGRESS.name,
            startTime = "2026-08-20T03:01:20.467Z",
            lastTrackedAt = "2026-08-20T03:01:20.467Z",
            createdAt = "2026-08-20T03:01:20.467Z"
        ),
        stepTime = 100
    )
}

@Composable
fun WalkInfo(
    walkStatus: WalkStatus,
    stepTime: Int,
    routePoints: List<Pair<Double, Double>> = emptyList()
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = ThinLineDp,
                color = Gray,
                shape = MediumRoundShape
            )
    ) {
        KakaoMap(
            routePoints = routePoints
        )
        WalkTargetBox(
            walkTarget = walkStatus.targetDistance,
            currentWalk = walkStatus.actualDistance
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 15.dp,
                    vertical = 10.dp
                )
        ) {
            WalkStatusDetailItemTemplates.forEachIndexed { index, item ->
                WalkDetailItem(
                    title = item.title,
                    current = item.value(
                        walkStatus,
                        stepTime
                    ).toString(),
                    unit = item.unit,
                )

                if (index < WalkStatusDetailItemTemplates.size - 1) {
                    Row(
                        modifier = Modifier
                            .weight(1f),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Divider(
                            position = DividerPosition.Vertical,
                            size = 34.dp
                        )
                    }
                }
            }
        }
    }
}
package com.dangdang.ui.screens.navigation

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.dangdang.common.utils.mainScreen
import com.dangdang.common.utils.regular
import com.dangdang.component.button.WalkButton
import com.dangdang.component.map.KakaoMap
import com.dangdang.component.navigation.topnavigation.TopNavigation
import com.dangdang.component.page.walk.WalkInfo
import com.dangdang.data.model.walk.WalkStatus
import com.dangdang.di.StepCounterManager
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.viewmodel.walk.WalkViewModel

@Preview
@Composable
fun WalkScreenPreview(

){
    WalkScreenContent(
        walkStatus = WalkStatus(
            walkTargetDistance = 2.6f,
            currentWalkDistance = 0.85f,
            currentWalkCount = 10,
            currentWalkKcal = 20
        ),
        stepTime = 100,
        isWalking = false,
        routePoints = emptyList(),
        onWalkButtonClick = {}
    )
}

@Composable
fun WalkScreen(
    walkViewModel: WalkViewModel = hiltViewModel()
){
    val context = LocalContext.current

    val walkStatus by
        StepCounterManager.walkStatus.collectAsState()

    val stepTime by StepCounterManager.stepTime.collectAsState()

    val isWalking by
        StepCounterManager.isWalking.collectAsState()

    val routePoints by
        StepCounterManager.routePoints.collectAsState()

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            if (it) {
                walkViewModel.startStepCounting(
                    context,
                    walkStatus.currentWalkCount
                )
            }
        }

    WalkScreenContent(
        walkStatus = walkStatus,
        stepTime = stepTime,
        isWalking = isWalking,
        routePoints = routePoints,
        onWalkButtonClick = {
            if(isWalking){
                walkViewModel.stopStepCounting(
                    context = context
                )
            }else{
                if (Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.Q
                ) {
                    permissionLauncher.launch(
                        Manifest.permission.ACTIVITY_RECOGNITION
                    )
                } else {
                    walkViewModel.startStepCounting(
                        context,
                        walkStatus.currentWalkCount
                    )
                }
            }
        }
    )
}

@Composable
fun WalkScreenContent(
    walkStatus: WalkStatus,
    stepTime: Int,
    isWalking: Boolean,
    routePoints: List<Pair<Double, Double>> = emptyList(),
    onWalkButtonClick: () -> Unit
){
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .mainScreen()
    ) {
        TopNavigation(
            title = "걷기",
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(
                    vertical = 20.dp,
                    horizontal = 30.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WalkInfo(
                walkStatus = walkStatus,
                stepTime = stepTime,
                routePoints = routePoints
            )

            WalkButton(
                isWalking = isWalking,
                onClick = onWalkButtonClick
            )
        }
    }
}
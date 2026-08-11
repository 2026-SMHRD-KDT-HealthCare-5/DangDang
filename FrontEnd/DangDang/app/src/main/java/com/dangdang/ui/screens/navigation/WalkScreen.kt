package com.dangdang.ui.screens.navigation

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.dangdang.common.utils.mainScreen
import com.dangdang.component.button.WalkButton
import com.dangdang.component.dialog.WalkMissionCompleteDialog
import com.dangdang.component.navigation.topnavigation.TopNavigation
import com.dangdang.component.page.walk.WalkInfo
import com.dangdang.data.model.walk.WalkStatus
import com.dangdang.data.manager.StepCounterManager
import com.dangdang.ui.viewmodel.walk.WalkViewModel

@Preview
@Composable
fun WalkScreenPreview(

){
    WalkScreenContent(
        walkStatus = WalkStatus(
            missionNo = 1,
            walkTargetDistance = 2.6f,
            currentWalkDistance = 0f,
            currentWalkCount = 0,
            currentWalkKcal = 0
        ),
        stepTime = 100,
        isWalking = false,
        routePoints = emptyList(),
        onWalkButtonClick = {},
        isWalkEndDialog = false,
        onSendGlucoseClick = {}
    )
}

@Composable
fun WalkScreen(
    walkViewModel: WalkViewModel = hiltViewModel(),
    isStart: Boolean,
    onSendGlucoseClick: () -> Unit
){
    val context = LocalContext.current

    val walkStatus by
        StepCounterManager.walkStatus.collectAsState()

    val stepTime by StepCounterManager.stepTime.collectAsState()

    val isWalking by
        StepCounterManager.isWalking.collectAsState()

    val routePoints by
        StepCounterManager.routePoints.collectAsState()

    val isEndWalk by
        StepCounterManager.isEndWalk.collectAsState()

    val isWalkEndDialog by
        StepCounterManager.isWalkEndDialog.collectAsState()

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

    fun startStepCounting(){
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

    LaunchedEffect(isStart) {
        if(isStart&&!isWalking){
            startStepCounting()
        }
    }

    LaunchedEffect(isEndWalk) {
        if(isEndWalk && walkStatus.walkTargetDistance > 0.0f){
            walkViewModel.endWalkMission(walkStatus.missionNo)
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
                startStepCounting()
            }
        },
        isWalkEndDialog = isWalkEndDialog,
        onSendGlucoseClick = onSendGlucoseClick
    )
}

@Composable
fun WalkScreenContent(
    walkStatus: WalkStatus,
    stepTime: Int,
    isWalking: Boolean,
    routePoints: List<Pair<Double, Double>> = emptyList(),
    onWalkButtonClick: () -> Unit,
    isWalkEndDialog: Boolean,
    onSendGlucoseClick: () -> Unit
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
    
    if(isWalkEndDialog){
        WalkMissionCompleteDialog(
            onDismiss = {
                StepCounterManager.closeWalkEndDialog()
            },
            onButtonClick = onSendGlucoseClick
        )
    }
}
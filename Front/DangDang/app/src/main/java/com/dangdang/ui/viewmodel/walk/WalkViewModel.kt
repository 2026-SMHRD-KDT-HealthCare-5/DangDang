package com.dangdang.ui.viewmodel.walk

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.dangdang.di.StepCounterService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WalkViewModel @Inject constructor(

): ViewModel(){

    fun startStepCounting(
        context: Context
    ) {

        val intent =
            Intent(
                context,
                StepCounterService::class.java
            ).apply {
                action =
                    StepCounterService.ACTION_START
            }

        ContextCompat.startForegroundService(
            context,
            intent
        )
    }

    fun stopStepCounting(
        context: Context
    ) {

        val intent =
            Intent(
                context,
                StepCounterService::class.java
            ).apply {

                action =
                    StepCounterService.ACTION_STOP
            }

        context.startService(
            intent
        )
    }
}
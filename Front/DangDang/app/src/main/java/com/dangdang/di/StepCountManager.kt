package com.dangdang.di

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object StepCounterManager {

    private val _stepCount = MutableStateFlow(0)

    val stepCount: StateFlow<Int> =
        _stepCount.asStateFlow()

    fun updateStepCount(stepCount: Int) {
        _stepCount.value = stepCount
    }

    fun reset() {
        _stepCount.value = 0
    }
}
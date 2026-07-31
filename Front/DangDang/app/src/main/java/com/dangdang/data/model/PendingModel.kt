package com.dangdang.data.model

import androidx.annotation.Keep
import com.dangdang.data.enums.LoadingState

@Keep
data class PendingModel<T>(
    val data: T?,
    val loadingState: LoadingState
)

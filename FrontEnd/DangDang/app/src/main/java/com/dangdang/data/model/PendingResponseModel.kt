package com.dangdang.data.model

import androidx.annotation.Keep
import retrofit2.Response

@Keep
data class PendingResponseModel<T, R> (
    val pendingModel: PendingModel<T>,
    val response: Response<R>
)
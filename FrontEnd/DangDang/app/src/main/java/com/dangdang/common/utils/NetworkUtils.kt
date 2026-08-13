package com.dangdang.common.utils

import android.util.Log
import com.dangdang.data.enums.LoadingState
import com.dangdang.data.model.PendingModel
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

//api 부를 시 공통 함수
suspend fun <T> safeApiCall(call: suspend () -> Response<T>): Response<T> {
    return try {
        call()
    } catch (e: HttpException) {
        Log.e("API", "HTTP ${e.code()}: ${e.response()?.errorBody()?.string()}", e)
        Response.error(e.code(), (e.response()?.errorBody()?.string() ?: "").toResponseBody(null))
    }
    catch (e: IOException) {
        Log.e("API", "Network error", e)
        Response.error(500, "".toResponseBody(null))
    }
}

fun <T> MutableStateFlow<PendingModel<T>>.applyResponse(response: Response<T>) {
    value = if (response.isSuccessful) {
        value.copy(data = response.body(), loadingState = LoadingState.Success)
    } else {
        value.copy(data = null, loadingState = LoadingState.Error)
    }
}
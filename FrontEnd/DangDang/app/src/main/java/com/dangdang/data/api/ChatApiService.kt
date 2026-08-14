package com.dangdang.data.api

import com.dangdang.Application.Companion.ChatPath
import com.dangdang.data.model.chat.PreGlucoseInputForm
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ChatApiService {
    //식전 혈당 입력
    @POST("${ChatPath}/preglucose")
    suspend fun preGlucose(
        @Body preGlucoseInputForm: PreGlucoseInputForm
    ): Response<PreGlucoseInputForm>
}
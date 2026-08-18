package com.dangdang.data.api

import com.dangdang.Application.Companion.AnalyzePath
import com.dangdang.Application.Companion.ChatPath
import com.dangdang.data.model.chat.ChatHistoryResponse
import com.dangdang.data.model.chat.ChatInputForm
import com.dangdang.data.model.chat.ChatResponse
import com.dangdang.data.model.chat.PreGlucoseInputForm
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ChatApiService {
    //식전 혈당 입력
    @POST("${AnalyzePath}/preglucose")
    suspend fun preGlucose(
        @Body preGlucoseInputForm: PreGlucoseInputForm
    ): Response<PreGlucoseInputForm>

    //채팅 보내기
    @POST(ChatPath)
    suspend fun sendChat(
        @Body chatInputForm: ChatInputForm
    ): Response<ChatResponse>

    //채팅 기록
    @GET("${ChatPath}/history")
    suspend fun getChatHistory(

    ): Response<ChatHistoryResponse>
}
package com.dangdang.data.api

import com.dangdang.Application.Companion.AnalyzePath
import com.dangdang.Application.Companion.ChatPath
import com.dangdang.data.model.chat.ChatHistoryResponse
import com.dangdang.data.model.chat.ChatInputForm
import com.dangdang.data.model.chat.ChatResponse
import com.dangdang.data.model.chat.FoodAnalysisResponse
import com.dangdang.data.model.chat.FoodConfirmInputForm
import com.dangdang.data.model.chat.FoodConfirmResponse
import com.dangdang.data.model.chat.FoodPredictInputForm
import com.dangdang.data.model.chat.FoodPredictResponse
import com.dangdang.data.model.chat.PreGlucoseInputForm
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ChatApiService {
    //식전 혈당 입력
    @POST("${AnalyzePath}/preglucose")
    suspend fun preGlucose(
        @Body preGlucoseInputForm: PreGlucoseInputForm
    ): Response<PreGlucoseInputForm>

    //음식 인식
    @Multipart
    @POST("${AnalyzePath}/recognize")
    suspend fun foodRecognize(
        @Part image: MultipartBody.Part?,
        @Part("message") message: RequestBody,
        @Part("baseline") baseline: RequestBody,
    ): Response<FoodAnalysisResponse>

    //음식 먹은 양 입력
    @POST("${AnalyzePath}/predict")
    suspend fun foodPredict(
        @Body foodPredictInputForm: FoodPredictInputForm
    ): Response<FoodPredictResponse>

    //음식 재인식
    @Multipart
    @POST("${AnalyzePath}/reanalyze")
    suspend fun foodReAnalyze(
        @Part image: MultipartBody.Part?,
        @Part("foodName") foodName: RequestBody,
        @Part("baseline") baseline: RequestBody,
    ): Response<FoodAnalysisResponse>

    //음식 확정하기
    @POST(AnalyzePath)
    suspend fun foodConfirm(
        @Body foodConfirmInputForm: FoodConfirmInputForm
    ): Response<FoodConfirmResponse>

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
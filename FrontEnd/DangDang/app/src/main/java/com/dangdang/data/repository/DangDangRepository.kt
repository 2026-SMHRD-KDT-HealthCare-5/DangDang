package com.dangdang.data.repository

import android.content.Context
import android.net.Uri
import com.dangdang.common.utils.AIFeedbackStage
import com.dangdang.common.utils.AfterWalkGlucoseInputStage
import com.dangdang.common.utils.AnalysisFoodStage
import com.dangdang.common.utils.AnalysisFoodType
import com.dangdang.common.utils.BeforeMealGlucoseInputStage
import com.dangdang.common.utils.BeforeMealTipType
import com.dangdang.common.utils.InputAteFoodStage
import com.dangdang.common.utils.InputAteWeightStage
import com.dangdang.common.utils.RecommendWalkDistanceStage
import com.dangdang.common.utils.TodayWalkTargetType
import com.dangdang.common.utils.deleteSafely
import com.dangdang.common.utils.safeApiCall
import com.dangdang.common.utils.toMultipart
import com.dangdang.common.utils.toRequestBody
import com.dangdang.common.utils.uriToFile
import com.dangdang.data.api.ChatApiService
import com.dangdang.data.enums.ChatUserType
import com.dangdang.data.model.chat.AIRecommendWalkModel
import com.dangdang.data.model.chat.AnalysisFoodModel
import com.dangdang.data.model.chat.AnalysisNutritionResponse
import com.dangdang.data.model.chat.ChatHistory
import com.dangdang.data.model.chat.ChatInputForm
import com.dangdang.data.model.chat.ChatModel
import com.dangdang.data.model.chat.ChatRecommendQuestionModel
import com.dangdang.data.model.chat.FoodAnalysisResponse
import com.dangdang.data.model.chat.FoodConfirmInputForm
import com.dangdang.data.model.chat.FoodInfoModel
import com.dangdang.data.model.chat.FoodInputDirectlyForm
import com.dangdang.data.model.chat.FoodNutritionModel
import com.dangdang.data.model.chat.FoodPredictInputForm
import com.dangdang.data.model.chat.FoodPredictResponse
import com.dangdang.data.model.chat.GlucoseFeedbackModel
import com.dangdang.data.model.chat.PreGlucoseInputForm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import retrofit2.Response
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

class DangDangRepository @Inject constructor(
    private val chatApiService: ChatApiService
){
    private val _analyzeChattingList = MutableStateFlow<List<ChatModel>>(emptyList())
    private val _analyzeFood = MutableStateFlow<FoodAnalysisResponse?>(null)
    private val _foodPredict = MutableStateFlow<FoodPredictResponse?>(null)
    private val _preGlucose = MutableStateFlow<Double?>(null)
    private val _portion = MutableStateFlow<Double?>(null)

    //채팅 리스트 호출하기
    suspend fun getChattingList(): Response<List<ChatModel>>{
        val chatResponse = safeApiCall {
            chatApiService.getChatHistory()
        }
        if(chatResponse.isSuccessful){
            val chatList = ArrayList<ChatModel>()
            chatResponse.body()?.messages?.forEach { chat ->
                chatList.addAll(
                    listOf(
                        ChatModel(
                            chatUserType = ChatUserType.User,
                            message = chat.userMessage?:"",
                            date = LocalDateTime.parse(chat.chattedAt),
                            chatType = "",
                            chatStageType = "",
                            isChatAble = true,
                            isInputComplete = false,
                            analysisFoodInfo = null,
                            recommendWalkInfo = null,
                            glucoseFeedbackInfo = null
                        ),
                        ChatModel(
                            chatUserType = ChatUserType.AI,
                            message = chat.aiMessage,
                            date = LocalDateTime.parse(chat.chattedAt),
                            chatType = if(chat.chatType == "MISSION_CARD"){
                                AnalysisFoodType
                            }else{
                                chat.chatType
                            },
                            chatStageType = if(chat.chatType == "MISSION_CARD"){
                                RecommendWalkDistanceStage
                            }else{
                                ""
                            },
                            isChatAble = true,
                            isInputComplete = false,
                            analysisFoodInfo = null,
                            recommendWalkInfo = if(chat.cardData!=null){
                                AIRecommendWalkModel(
                                    targetDistance = chat.cardData.targetDistance.toFloat(),
                                    minute = 30
                                )
                            }else{
                                null
                            },
                            glucoseFeedbackInfo = null
                        )
                    )
                )
            }
            return Response.success(chatList)
        }else{
            return Response.error(chatResponse.code(), chatResponse.errorBody())
        }
    }

    //추천 질문 리스트
    suspend fun getRecommendQuestion(): Response<List<ChatRecommendQuestionModel>> {
        val response = listOf(
            ChatRecommendQuestionModel(
                question = "음식 분석 & 걷기",
                type = AnalysisFoodType
            ),
            ChatRecommendQuestionModel(
                question = "식전 관리 팁",
                type = BeforeMealTipType
            ),
            ChatRecommendQuestionModel(
                question = "오늘 걷기 목표",
                type = TodayWalkTargetType
            )
        )
        return Response.success(response)
    }

    //채팅 전송
    suspend fun chatSend(message: String): Response<List<ChatModel>>{
        val sendChatResponse = safeApiCall {
            chatApiService.sendChat(ChatInputForm(message))
        }

        return if(sendChatResponse.isSuccessful){
            getChattingList()
        }else{
            Response.error(sendChatResponse.code(), sendChatResponse.errorBody())
        }
    }

    //음식 분석&걷기 시작
    suspend fun startAnalysisFood(): Response<List<ChatModel>>{
        _analyzeChattingList.value = listOf(
            ChatModel(
                chatUserType = ChatUserType.AI,
                message = "식전 혈당을 알고 계신가요?\n" +
                        "(음식을 먹기 전 혈당이에요)\n" +
                        "\n" +
                        "입력해주시면 예측이\n" +
                        "더 정확해져요!",
                date = LocalDateTime.now(),
                chatType = AnalysisFoodType,
                isChatAble = false,
                isInputComplete = false,
                chatStageType = BeforeMealGlucoseInputStage,
                analysisFoodInfo = null,
                recommendWalkInfo = null,
                glucoseFeedbackInfo = null
            )
        )
        return Response.success(_analyzeChattingList.value)
    }

    //식전 혈당 전송
    suspend fun sendBeforeMealGlucose(glucoseValue: String?): Response<List<ChatModel>>{
        val chatResponse = safeApiCall {
            chatApiService.preGlucose(
                PreGlucoseInputForm(
                    preGlucose = glucoseValue?.toIntOrNull()
                )
            )
        }
        if(chatResponse.isSuccessful){
            val chatResponseBody = chatResponse.body()
            val response = listOf(
                ChatModel(
                    chatUserType = ChatUserType.User,
                    message = if(glucoseValue == null) "모르겠어요" else "$glucoseValue mg/dL",
                    date = LocalDateTime.now(),
                    chatType = AnalysisFoodType,
                    isChatAble = false,
                    isInputComplete = true,
                    chatStageType = BeforeMealGlucoseInputStage,
                    analysisFoodInfo = null,
                    recommendWalkInfo = null,
                    glucoseFeedbackInfo = null
                ),
                ChatModel(
                    chatUserType = ChatUserType.AI,
                    message = "알겠어요!\n" +
                            "회원님의 상태(당뇨)에 맞는\n" +
                            "${chatResponseBody?.preGlucose?:""}mg/dL 적용할게요.",
                    date = LocalDateTime.now(),
                    chatType = AnalysisFoodType,
                    isChatAble = false,
                    isInputComplete = true,
                    chatStageType = BeforeMealGlucoseInputStage,
                    analysisFoodInfo = null,
                    recommendWalkInfo = null,
                    glucoseFeedbackInfo = null
                ),
                ChatModel(
                    chatUserType = ChatUserType.AI,
                    message = "먹은 음식을 알려주세요!\n" +
                            "음식의 영양성분과 예상 혈당 상승량을 알려드릴게요.\n" +
                            "그리고 혈당 스파이크를 막기 위한 걷기 미션을 생성해드릴게요.",
                    date = LocalDateTime.now(),
                    chatType = AnalysisFoodType,
                    isChatAble = false,
                    isInputComplete = false,
                    chatStageType = InputAteFoodStage,
                    analysisFoodInfo = null,
                    recommendWalkInfo = null,
                    glucoseFeedbackInfo = null
                )
            )

            _analyzeChattingList.update { currentList ->
                currentList.map {
                    it.copy(
                        isInputComplete = true
                    )
                } + response
            }

            _preGlucose.value = chatResponseBody?.preGlucose?.toDouble()

            return Response.success(_analyzeChattingList.value)
        }else{
            return Response.error(chatResponse.code(), chatResponse.errorBody())
        }
    }

    //음식 입력 전송
    suspend fun ateFoodSend(context: Context, ateFoodValue: String, ateFoodImageUri: Uri?): Response<List<ChatModel>>{
        var uploadFile: File? = null
        try{
            val ateFoodImagePart = ateFoodImageUri?.let { uri->
                uploadFile = context.uriToFile(uri)

                uploadFile.toMultipart()
            }

            val chatResponse = safeApiCall {
                chatApiService.foodRecognize(
                    image = ateFoodImagePart,
                    message = ateFoodValue.toRequestBody(),
                    baseline = _preGlucose.value.toString().toRequestBody()
                )
            }

            if(chatResponse.isSuccessful){
                val response = listOf(
                    ChatModel(
                        chatUserType = ChatUserType.User,
                        message = ateFoodValue,
                        messageImageUri = ateFoodImageUri,
                        date = LocalDateTime.now(),
                        chatType = AnalysisFoodType,
                        isChatAble = false,
                        isInputComplete = true,
                        chatStageType = InputAteFoodStage,
                        analysisFoodInfo = null,
                        recommendWalkInfo = null,
                        glucoseFeedbackInfo = null
                    ),
                    ChatModel(
                        chatUserType = ChatUserType.AI,
                        message = "드신 양을 인분 기준으로 알려주세요!\n" +
                                "(예: 반 인분이면 0.5, 한 그릇 다 드셨으면 1)",
                        date = LocalDateTime.now(),
                        chatType = AnalysisFoodType,
                        isChatAble = false,
                        isInputComplete = false,
                        chatStageType = InputAteWeightStage,
                        analysisFoodInfo = null,
                        recommendWalkInfo = null,
                        glucoseFeedbackInfo = null
                    )
                )

                _analyzeChattingList.update { currentList ->
                    currentList.map {
                        it.copy(
                            isInputComplete = true
                        )
                    } + response
                }

                _analyzeFood.value = chatResponse.body()

                return Response.success(_analyzeChattingList.value)
            }else{
                return Response.error(chatResponse.code(), chatResponse.errorBody())
            }
        }finally {
            uploadFile?.deleteSafely()
        }
    }

    fun analyzeFoodToFoodInfoModel(
        foodName: String,
        nutrition: AnalysisNutritionResponse?,
        servingSize: Int,
        calorie: Double
    ): FoodInfoModel{
        return FoodInfoModel(
            name = foodName,
            nutritionInfo = "총 내용량 ${servingSize}g 1인분(1개) / " +
                    "${calorie}kcal",
            nutritionList = listOf(
                FoodNutritionModel(
                    name = "탄수화물",
                    unit = "g",
                    value = nutrition?.carb?:0.0
                ),
                FoodNutritionModel(
                    name = "식이섬유",
                    unit = "g",
                    value = nutrition?.fiber?:0.0
                ),
                FoodNutritionModel(
                    name = "단백질",
                    unit = "g",
                    value = nutrition?.protein?:0.0
                ),
                FoodNutritionModel(
                    name = "지방",
                    unit = "g",
                    value = nutrition?.fat?:0.0
                ),
                FoodNutritionModel(
                    name = "칼로리",
                    unit = "kcal",
                    value = nutrition?.calorie?:0.0
                )
            )
        )
    }

    //음식 먹은 양 전송
    suspend fun ateWeightSend(
        weightValue: String,
    ): Response<List<ChatModel>>{
        val analyzeFood = _analyzeFood.value
        val chatResponse = safeApiCall {
            chatApiService.foodPredict(
                FoodPredictInputForm(
                    carb = analyzeFood?.nutrition?.carb?:0.0,
                    sugar = analyzeFood?.nutrition?.sugar?:0.0,
                    protein = analyzeFood?.nutrition?.protein?:0.0,
                    fat = analyzeFood?.nutrition?.fat?:0.0,
                    fiber = analyzeFood?.nutrition?.fiber?:0.0,
                    calorie = analyzeFood?.nutrition?.calorie?:0.0,
                    portion = weightValue.toDouble(),
                    baseline = _preGlucose.value?:0.0
                )
            )
        }


        if(chatResponse.isSuccessful){
            val foodPredict = chatResponse.body()

            val response = listOf(
                ChatModel(
                    chatUserType = ChatUserType.User,
                    message = weightValue+"인분",
                    date = LocalDateTime.now(),
                    chatType = AnalysisFoodType,
                    isChatAble = false,
                    isInputComplete = true,
                    chatStageType = InputAteFoodStage,
                    analysisFoodInfo = null,
                    recommendWalkInfo = null,
                    glucoseFeedbackInfo = null
                ),
                ChatModel(
                    chatUserType = ChatUserType.AI,
                    message = analyzeFood?.chatbotMessage?:"",
                    date = LocalDateTime.now(),
                    chatType = AnalysisFoodType,
                    isChatAble = false,
                    isInputComplete = false,
                    chatStageType = AnalysisFoodStage,
                    analysisFoodInfo = AnalysisFoodModel(
                        predictedGlucoseRise = foodPredict?.predictedGlucoseRise?:0.0,
                        beginGlucose = _preGlucose.value?:0.0,
                        foodInfo = analyzeFoodToFoodInfoModel(
                            foodName = analyzeFood?.foodName?:"",
                            nutrition = foodPredict?.nutritionUsed,
                            servingSize = analyzeFood?.serving_size?:0,
                            calorie = analyzeFood?.nutrition?.calorie?:0.0
                        )
                    ),
                    recommendWalkInfo = null,
                    glucoseFeedbackInfo = null
                )
            )

            _analyzeChattingList.update { currentList ->
                currentList.map {
                    it.copy(
                        isInputComplete = true
                    )
                } + response
            }
            _portion.value = weightValue.toDouble()
            _foodPredict.value = foodPredict

            return Response.success(_analyzeChattingList.value)
        }else{
            return Response.error(chatResponse.code(), chatResponse.errorBody())
        }
    }

    //ai로 다시 분석하기
    suspend fun reAnalyzeFood(
        context: Context,
        ateFoodValue: String,
        ateFoodImageUri: Uri?,
        weightValue: String,
    ): Response<List<ChatModel>>{
        var uploadFile: File? = null
        try{
            val ateFoodImagePart = ateFoodImageUri?.let { uri->
                uploadFile = context.uriToFile(uri)

                uploadFile.toMultipart()
            }

            val chatResponse = safeApiCall {
                chatApiService.foodReAnalyze(
                    image = ateFoodImagePart,
                    foodName = ateFoodValue.toRequestBody(),
                    baseline = _preGlucose.value.toString().toRequestBody()
                )
            }

            if(chatResponse.isSuccessful){
                val analyzeFood = chatResponse.body()
                _analyzeFood.value = analyzeFood

                val predictResponse = safeApiCall{
                    chatApiService.foodPredict(
                        FoodPredictInputForm(
                            carb = analyzeFood?.nutrition?.carb?:0.0,
                            sugar = analyzeFood?.nutrition?.sugar?:0.0,
                            protein = analyzeFood?.nutrition?.protein?:0.0,
                            fat = analyzeFood?.nutrition?.fat?:0.0,
                            fiber = analyzeFood?.nutrition?.fiber?:0.0,
                            calorie = analyzeFood?.nutrition?.calorie?:0.0,
                            portion = weightValue.toDouble(),
                            baseline = _preGlucose.value?:0.0
                        )
                    )
                }

                if(predictResponse.isSuccessful){
                    val foodPredict = predictResponse.body()

                    val response = listOf(
                        ChatModel(
                            chatUserType = ChatUserType.User,
                            message = "틀린 거 같아. 다시 분석해줘.",
                            date = LocalDateTime.now(),
                            chatType = AnalysisFoodType,
                            isChatAble = false,
                            isInputComplete = false,
                            chatStageType = InputAteFoodStage,
                            analysisFoodInfo = null,
                            recommendWalkInfo = null,
                            glucoseFeedbackInfo = null
                        ),
                        ChatModel(
                            chatUserType = ChatUserType.AI,
                            message = analyzeFood?.chatbotMessage?:"",
                            date = LocalDateTime.now(),
                            chatType = AnalysisFoodType,
                            isChatAble = false,
                            isInputComplete = false,
                            chatStageType = AnalysisFoodStage,
                            analysisFoodInfo = AnalysisFoodModel(
                                predictedGlucoseRise = foodPredict?.predictedGlucoseRise?:0.0,
                                beginGlucose = _preGlucose.value?:0.0,
                                foodInfo = analyzeFoodToFoodInfoModel(
                                    foodName = analyzeFood?.foodName?:"",
                                    nutrition = foodPredict?.nutritionUsed,
                                    servingSize = analyzeFood?.serving_size?:0,
                                    calorie = analyzeFood?.nutrition?.calorie?:0.0
                                )
                            ),
                            recommendWalkInfo = null,
                            glucoseFeedbackInfo = null
                        )
                    )

                    _analyzeChattingList.update { currentList ->
                        currentList.map {
                            it.copy(
                                isInputComplete = true
                            )
                        } + response
                    }
                    _portion.value = weightValue.toDouble()
                    _foodPredict.value = foodPredict

                    return Response.success(_analyzeChattingList.value)
                }else{
                    return Response.error(predictResponse.code(), predictResponse.errorBody())
                }
            }else{
                return Response.error(chatResponse.code(), chatResponse.errorBody())
            }
        }finally {
            uploadFile?.deleteSafely()
        }
    }

    //음식 직접 입력 전송
    suspend fun sendFoodInputDirectly(foodInputDirectlyForm: FoodInputDirectlyForm): Response<List<ChatModel>>{
        val chatResponse = safeApiCall {
            chatApiService.foodConfirm(
                FoodConfirmInputForm(
                    foodNo = null,
                    customFood = foodInputDirectlyForm,
                    preGlucose = _preGlucose.value,
                    portion = null
                )
            )
        }

        if(chatResponse.isSuccessful){
            val checkFood = chatResponse.body()

            _analyzeChattingList.value = listOf(
                ChatModel(
                    chatUserType = ChatUserType.AI,
                    message = checkFood?.chatbotMessage?:"",
                    date = LocalDateTime.now(),
                    chatType = AnalysisFoodType,
                    isChatAble = true,
                    isInputComplete = false,
                    chatStageType = RecommendWalkDistanceStage,
                    analysisFoodInfo = AnalysisFoodModel(
                        predictedGlucoseRise = checkFood?.predictedGlucoseRise?:0.0,
                        beginGlucose = _preGlucose.value?:0.0,
                        foodInfo = analyzeFoodToFoodInfoModel(
                            foodName = foodInputDirectlyForm.foodName,
                            nutrition = AnalysisNutritionResponse(
                                carb = foodInputDirectlyForm.carb.toDouble(),
                                sugar = foodInputDirectlyForm.sugar.toDouble(),
                                protein = foodInputDirectlyForm.protein.toDouble(),
                                fat = foodInputDirectlyForm.fat.toDouble(),
                                fiber = foodInputDirectlyForm.fiber.toDouble(),
                                calorie = foodInputDirectlyForm.calorie.toDouble()
                            ),
                            servingSize = foodInputDirectlyForm.servingSize.toInt(),
                            calorie = foodInputDirectlyForm.calorie.toDouble()
                        )
                    ),
                    recommendWalkInfo = AIRecommendWalkModel(
                        targetDistance = checkFood?.targetDistance?.toFloat()?:0.0f,
                        minute = 30
                    ),
                    glucoseFeedbackInfo = null
                )
            )

            return Response.success(_analyzeChattingList.value)
        }else{
            return Response.error(chatResponse.code(), chatResponse.errorBody())
        }
    }

    //검색어 다시 입력 선택 시
    suspend fun ateFoodReSearch(): Response<List<ChatModel>>{
        _analyzeChattingList.value = listOf(
            ChatModel(
                chatUserType = ChatUserType.AI,
                message = "어떤 음식인가요?\n" +
                        "자세히 입력해주시면 더 정확하게 찾아볼게요.",
                date = LocalDateTime.now(),
                chatType = AnalysisFoodType,
                isChatAble = false,
                isInputComplete = false,
                chatStageType = InputAteFoodStage,
                analysisFoodInfo = null,
                recommendWalkInfo = null,
                glucoseFeedbackInfo = null
            )
        )
        return Response.success(_analyzeChattingList.value)
    }

    //음식 확정 선택 시
    suspend fun foodCheck(): Response<List<ChatModel>>{
        val chatResponse = safeApiCall {
            chatApiService.foodConfirm(
                FoodConfirmInputForm(
                    foodNo = _analyzeFood.value?.foodNo,
                    customFood = null,
                    preGlucose = _preGlucose.value,
                    portion = _portion.value
                )
            )
        }


        if(chatResponse.isSuccessful){
            val checkFood = chatResponse.body()

            _analyzeChattingList.value = listOf(
                ChatModel(
                    chatUserType = ChatUserType.AI,
                    message = checkFood?.chatbotMessage?:"",
                    date = LocalDateTime.now(),
                    chatType = AnalysisFoodType,
                    isChatAble = true,
                    isInputComplete = false,
                    chatStageType = RecommendWalkDistanceStage,
                    analysisFoodInfo = AnalysisFoodModel(
                        predictedGlucoseRise = checkFood?.predictedGlucoseRise?:0.0,
                        beginGlucose = _preGlucose.value?:0.0,
                        foodInfo = analyzeFoodToFoodInfoModel(
                            foodName = _analyzeFood.value?.foodName?:"",
                            nutrition = _foodPredict.value?.nutritionUsed,
                            servingSize = _analyzeFood.value?.serving_size?:0,
                            calorie = _analyzeFood.value?.nutrition?.calorie?:0.0
                        )
                    ),
                    recommendWalkInfo = AIRecommendWalkModel(
                        targetDistance = checkFood?.targetDistance?.toFloat()?:0.0f,
                        minute = 30
                    ),
                    glucoseFeedbackInfo = null
                )
            )

            return Response.success(_analyzeChattingList.value)
        }else{
            return Response.error(chatResponse.code(), chatResponse.errorBody())
        }
    }


    //오늘 걷기 목표 불러오기 선택 시
    suspend fun getRecommendWalkChallenge(): Response<List<ChatModel>>{
        val response = listOf(
            ChatModel(
                chatUserType = ChatUserType.AI,
                message = "오늘 걷기 미션이에요!",
                date = LocalDateTime.of(
                    LocalDate.now(),
                    LocalTime.of(8, 30)
                ),
                chatType = TodayWalkTargetType,
                isChatAble = true,
                isInputComplete = false,
                chatStageType = "",
                analysisFoodInfo = null,
                recommendWalkInfo = AIRecommendWalkModel(
                    targetDistance = 2.6f,
                    minute = 30
                ),
                glucoseFeedbackInfo = null
            )
        )
        return Response.success(response)
    }

    //걷기 완료 미션 전송
    suspend fun completeWalkMission(): Response<List<ChatModel>>{
        val response = listOf(
            ChatModel(
                chatUserType = ChatUserType.AI,
                message = "걷기 완료! \uD83C\uDF89 수고했어요!\n" +
                        "이제 혈당을 입력해주세요.",
                date = LocalDateTime.of(
                    LocalDate.now(),
                    LocalTime.of(8, 30)
                ),
                chatType = AnalysisFoodType,
                isChatAble = false,
                isInputComplete = false,
                chatStageType = AfterWalkGlucoseInputStage,
                analysisFoodInfo = null,
                recommendWalkInfo = null,
                glucoseFeedbackInfo = null
            )
        )
        return Response.success(response)
    }

    //식후 혈당 전송
    suspend fun afterWalkGlucoseSend(glucose: Int): Response<List<ChatModel>>{
        val response = listOf(
            ChatModel(
                chatUserType = ChatUserType.AI,
                message = "정말 잘했어요!",
                date = LocalDateTime.of(
                    LocalDate.now(),
                    LocalTime.of(8, 30)
                ),
                chatType = AnalysisFoodType,
                isChatAble = true,
                isInputComplete = false,
                chatStageType = AIFeedbackStage,
                analysisFoodInfo = null,
                recommendWalkInfo = null,
                glucoseFeedbackInfo = GlucoseFeedbackModel(
                    beginGlucose = 140,
                    aiPredictAfterGlucose = 175,
                    realAfterGlucose = 170,
                    targetDistance = 2.6f,
                    walkDistance = 2.6f
                )
            )
        )
        return Response.success(response)
    }
}
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
import com.dangdang.common.utils.getMeterToKm
import com.dangdang.common.utils.safeApiCall
import com.dangdang.common.utils.toMultipart
import com.dangdang.common.utils.toRequestBody
import com.dangdang.common.utils.uriToFile
import com.dangdang.common.utils.uriToResizedFile
import com.dangdang.data.api.ChatApiService
import com.dangdang.data.api.WalkApiService
import com.dangdang.data.enums.ChatCardType
import com.dangdang.data.enums.ChatUserType
import com.dangdang.data.enums.LoadingState
import com.dangdang.data.model.PendingModel
import com.dangdang.data.model.PendingResponseModel
import com.dangdang.data.model.chat.AIRecommendWalkModel
import com.dangdang.data.model.chat.AnalysisFoodModel
import com.dangdang.data.model.chat.AnalysisNutritionResponse
import com.dangdang.data.model.chat.ChatHistory
import com.dangdang.data.model.chat.ChatHistoryResponse
import com.dangdang.data.model.chat.ChatInputForm
import com.dangdang.data.model.chat.ChatModel
import com.dangdang.data.model.chat.ChatRecommendQuestionModel
import com.dangdang.data.model.chat.FoodAnalysisResponse
import com.dangdang.data.model.chat.FoodConfirmInputForm
import com.dangdang.data.model.chat.FoodConfirmResponse
import com.dangdang.data.model.chat.FoodInfoModel
import com.dangdang.data.model.chat.FoodInputDirectlyForm
import com.dangdang.data.model.chat.FoodNutritionModel
import com.dangdang.data.model.chat.FoodPredictInputForm
import com.dangdang.data.model.chat.FoodPredictResponse
import com.dangdang.data.model.chat.GlucoseFeedbackModel
import com.dangdang.data.model.chat.PreGlucoseInputForm
import com.dangdang.data.model.walk.PostWalkGlucoseInputForm
import com.dangdang.data.model.walk.PostWalkGlucoseResponse
import com.dangdang.data.model.walk.WalkStatusCardData
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import retrofit2.Response
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject
import kotlin.collections.lastIndex

class DangDangRepository @Inject constructor(
    private val chatApiService: ChatApiService,
    private val walkApiService: WalkApiService
){
    private val gson = Gson()
    private val _analyzeChattingList = MutableStateFlow<List<ChatModel>>(emptyList())

    private val _currentChattingList = MutableStateFlow<List<ChatModel>>(emptyList())
    val currentChattingList = _currentChattingList.asStateFlow()

    private val _analyzeFood = MutableStateFlow<FoodAnalysisResponse?>(null)
    private val _foodPredict = MutableStateFlow<FoodPredictResponse?>(null)
    private val _preGlucose = MutableStateFlow<Float?>(null)
    private val _portion = MutableStateFlow<Float?>(null)

    private val _targetTimeMinutes = MutableStateFlow<Int?>(null)

    private val _missionNo = MutableStateFlow<Int?>(null)

    suspend fun getChatListParsing(): PendingResponseModel<List<ChatModel>, ChatHistoryResponse>{
        val chatResponse = safeApiCall {
            chatApiService.getChatHistory()
        }
        if(chatResponse.isSuccessful){
            val chatList = ArrayList<ChatModel>()
            val chatResponseList = chatResponse.body()?.messages
            chatResponseList?.forEachIndexed { index, chat ->
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
                            chatType = if(chat.chatType == ChatCardType.MISSION_CARD.name ||
                                chat.chatType == ChatCardType.FOOD_CARD.name ||
                                chat.chatType == ChatCardType.POST_GLUCOSE.name ||
                                chat.chatType == ChatCardType.RESULT_CARD_FAIL.name ||
                                chat.chatType == ChatCardType.RESULT_CARD_SUCCESS.name){
                                AnalysisFoodType
                            }else{
                                chat.chatType
                            },
                            chatStageType = if(chat.chatType == ChatCardType.MISSION_CARD.name ||
                                chat.chatType == ChatCardType.FOOD_CARD.name){
                                RecommendWalkDistanceStage
                            }else if(chat.chatType == ChatCardType.POST_GLUCOSE.name){
                                chat.cardData?.takeIf { it.isJsonObject }?.let { cardDataJson ->
                                    val cardData = gson.fromJson(cardDataJson, WalkStatusCardData::class.java)
                                    _missionNo.value = cardData.missionNo
                                }
                                AfterWalkGlucoseInputStage
                            }else if(chat.chatType == ChatCardType.RESULT_CARD_FAIL.name ||
                                chat.chatType == ChatCardType.RESULT_CARD_SUCCESS.name){
                                AIFeedbackStage
                            }else{
                                ""
                            },
                            isChatAble = true,
                            isInputComplete = if(chat.chatType == ChatCardType.POST_GLUCOSE.name){
                                index < chatResponseList.lastIndex
                            }else{
                                false
                            },
                            analysisFoodInfo = if(chat.chatType == ChatCardType.FOOD_CARD.name){
                                chat.cardData?.takeIf { it.isJsonObject }?.let { cardDataJson ->
                                    val cardData = gson.fromJson(cardDataJson, FoodAnalysisResponse::class.java)
                                    AnalysisFoodModel(
                                        predictedGlucoseRise = 0f,
                                        beginGlucose = 0f,
                                        foodInfo = analyzeFoodToFoodInfoModel(
                                            isMatched = true,
                                            foodName = cardData.foodName,
                                            nutrition = cardData.nutrition,
                                            servingSize = cardData.servingSize,
                                            calorie = cardData.nutrition.calorie
                                        )
                                    )
                                }
                            }else{
                                null
                            },
                            recommendWalkInfo = if(chat.chatType == ChatCardType.MISSION_CARD.name){
                                chat.cardData?.takeIf { it.isJsonObject }?.let { cardDataJson ->
                                    val cardData = gson.fromJson(cardDataJson, FoodConfirmResponse::class.java)
                                    _targetTimeMinutes.value = cardData.targetTimeMinutes
                                    AIRecommendWalkModel(
                                        targetDistance = getMeterToKm(cardData.targetDistance).toFloat(),
                                        minute = cardData.targetTimeMinutes
                                    )
                                }
                            }else{
                                null
                            },
                            glucoseFeedbackInfo = if(chat.chatType == ChatCardType.RESULT_CARD_FAIL.name ||
                                chat.chatType == ChatCardType.RESULT_CARD_SUCCESS.name){
                                chat.cardData?.takeIf { it.isJsonObject }?.let { cardDataJson ->
                                    val cardData = gson.fromJson(cardDataJson,
                                        PostWalkGlucoseResponse::class.java)
                                    GlucoseFeedbackModel(
                                        beginGlucose = cardData?.preGlucose?:0,
                                        aiPredictAfterGlucose = cardData?.postGlucoseEst?:0,
                                        realAfterGlucose = cardData?.postWalkGlucose?:0,
                                        targetDistance = getMeterToKm(cardData?.targetDistance?:0f),
                                        walkDistance = getMeterToKm(cardData?.actualDistance?:0f)
                                    )
                                }
                            }else{
                                null
                            }
                        )
                    )
                )
            }

            _currentChattingList.value = chatList

            return PendingResponseModel(
                pendingModel = PendingModel(
                    data = chatList,
                    loadingState = LoadingState.Success
                ),
                response = chatResponse
            )
        }else{
            return PendingResponseModel(
                pendingModel = PendingModel(
                    data = null,
                    loadingState = LoadingState.Error
                ),
                response = chatResponse
            )
        }
    }

    //채팅 리스트 호출하기
    suspend fun getChattingList(): Response<List<ChatModel>>{
        val chatListPending = getChatListParsing()

        return if(chatListPending.pendingModel.loadingState == LoadingState.Success){
            Response.success(chatListPending.pendingModel.data)
        }else{
            Response.error(chatListPending.response.code(), chatListPending.response.errorBody())
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

            _preGlucose.value = chatResponseBody?.preGlucose?.toFloat()

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
                uploadFile = context.uriToResizedFile(uri, maxSize = 512)

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
        isMatched: Boolean,
        foodName: String,
        nutrition: AnalysisNutritionResponse?,
        servingSize: Int,
        calorie: Float
    ): FoodInfoModel{
        return FoodInfoModel(
            isMatched = isMatched,
            name = foodName,
            nutritionInfo = "총 내용량 ${servingSize}g 1인분(1개) / " +
                    "${calorie}kcal",
            nutritionList = listOf(
                FoodNutritionModel(
                    name = "탄수화물",
                    unit = "g",
                    value = nutrition?.carb?:0f
                ),
                FoodNutritionModel(
                    name = "식이섬유",
                    unit = "g",
                    value = nutrition?.fiber?:0f
                ),
                FoodNutritionModel(
                    name = "단백질",
                    unit = "g",
                    value = nutrition?.protein?:0f
                ),
                FoodNutritionModel(
                    name = "지방",
                    unit = "g",
                    value = nutrition?.fat?:0f
                ),
                FoodNutritionModel(
                    name = "칼로리",
                    unit = "kcal",
                    value = nutrition?.calorie?:0f
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
                    carb = analyzeFood?.nutrition?.carb?:0f,
                    sugar = analyzeFood?.nutrition?.sugar?:0f,
                    protein = analyzeFood?.nutrition?.protein?:0f,
                    fat = analyzeFood?.nutrition?.fat?:0f,
                    fiber = analyzeFood?.nutrition?.fiber?:0f,
                    calorie = analyzeFood?.nutrition?.calorie?:0f,
                    portion = weightValue.toFloat(),
                    baseline = _preGlucose.value?:0f
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
                        predictedGlucoseRise = foodPredict?.predictedGlucoseRise?:0f,
                        beginGlucose = _preGlucose.value?:0f,
                        foodInfo = analyzeFoodToFoodInfoModel(
                            isMatched = (analyzeFood?.foodNo?:0) > 0,
                            foodName = analyzeFood?.foodName?:"",
                            nutrition = foodPredict?.nutritionUsed,
                            servingSize = analyzeFood?.serving_size?:0,
                            calorie = analyzeFood?.nutrition?.calorie?:0f
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
            _portion.value = weightValue.toFloat()
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
                uploadFile = context.uriToResizedFile(uri, maxSize = 512)

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
                            carb = analyzeFood?.nutrition?.carb?:0f,
                            sugar = analyzeFood?.nutrition?.sugar?:0f,
                            protein = analyzeFood?.nutrition?.protein?:0f,
                            fat = analyzeFood?.nutrition?.fat?:0f,
                            fiber = analyzeFood?.nutrition?.fiber?:0f,
                            calorie = analyzeFood?.nutrition?.calorie?:0f,
                            portion = weightValue.toFloat(),
                            baseline = _preGlucose.value?:0f
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
                                predictedGlucoseRise = foodPredict?.predictedGlucoseRise?:0f,
                                beginGlucose = _preGlucose.value?:0f,
                                foodInfo = analyzeFoodToFoodInfoModel(
                                    isMatched = (analyzeFood?.foodName?.isNotEmpty() == true &&
                                            !analyzeFood.foodName.contains("없음")),
                                    foodName = analyzeFood?.foodName?:"",
                                    nutrition = foodPredict?.nutritionUsed,
                                    servingSize = analyzeFood?.serving_size?:0,
                                    calorie = analyzeFood?.nutrition?.calorie?:0f
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
                    _portion.value = weightValue.toFloat()
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
                    customFood = foodInputDirectlyForm.copy(
                        servingSize = foodInputDirectlyForm.servingSize.ifEmpty {
                            "0"
                        },
                        calorie = foodInputDirectlyForm.calorie.ifEmpty {
                            "0"
                        },
                        carb = foodInputDirectlyForm.carb.ifEmpty {
                            "0"
                        },
                        sugar = foodInputDirectlyForm.sugar.ifEmpty {
                            "0"
                        },
                        fiber = foodInputDirectlyForm.fiber.ifEmpty {
                            "0"
                        },
                        protein = foodInputDirectlyForm.protein.ifEmpty {
                            "0"
                        },
                        fat = foodInputDirectlyForm.fat.ifEmpty {
                            "0"
                        }
                    ),
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
                        predictedGlucoseRise = checkFood?.predictedGlucoseRise?:0f,
                        beginGlucose = _preGlucose.value?:0f,
                        foodInfo = analyzeFoodToFoodInfoModel(
                            isMatched = true,
                            foodName = foodInputDirectlyForm.foodName,
                            nutrition = AnalysisNutritionResponse(
                                carb = foodInputDirectlyForm.carb.toFloatOrNull()?:0f,
                                sugar = foodInputDirectlyForm.sugar.toFloatOrNull()?:0f,
                                protein = foodInputDirectlyForm.protein.toFloatOrNull()?:0f,
                                fat = foodInputDirectlyForm.fat.toFloatOrNull()?:0f,
                                fiber = foodInputDirectlyForm.fiber.toFloatOrNull()?:0f,
                                calorie = foodInputDirectlyForm.calorie.toFloatOrNull()?:0f
                            ),
                            servingSize = foodInputDirectlyForm.servingSize.toIntOrNull()?:0,
                            calorie = foodInputDirectlyForm.calorie.toFloatOrNull()?:0f
                        )
                    ),
                    recommendWalkInfo = AIRecommendWalkModel(
                        targetDistance = getMeterToKm(checkFood?.targetDistance?:0f),
                        minute = checkFood?.targetTimeMinutes?:0
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
            if(_analyzeFood.value?.source?.contains("AI") == true){
                val nutrition = _foodPredict.value?.nutritionUsed
                //틀려요 ai 분석
                chatApiService.foodConfirm(
                    FoodConfirmInputForm(
                        foodNo = null,
                        customFood = FoodInputDirectlyForm(
                            foodName = _analyzeFood.value?.foodName?:"",
                            servingSize = _analyzeFood.value?.serving_size.toString(),
                            calorie = nutrition?.calorie.toString(),
                            carb = nutrition?.carb.toString(),
                            sugar = nutrition?.sugar.toString(),
                            fiber = nutrition?.fiber.toString(),
                            protein = nutrition?.protein.toString(),
                            fat = nutrition?.fat.toString(),
                            source = _analyzeFood.value?.source?:""
                        ),
                        preGlucose = _preGlucose.value,
                        portion = _portion.value
                    )
                )
            }else{
                //한번에 맞아요 했을 시
                chatApiService.foodConfirm(
                    FoodConfirmInputForm(
                        foodNo = _analyzeFood.value?.foodNo,
                        customFood = null,
                        preGlucose = _preGlucose.value,
                        portion = _portion.value
                    )
                )
            }
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
                        predictedGlucoseRise = checkFood?.predictedGlucoseRise?:0f,
                        beginGlucose = _preGlucose.value?:0f,
                        foodInfo = analyzeFoodToFoodInfoModel(
                            isMatched = true,
                            foodName = _analyzeFood.value?.foodName?:"",
                            nutrition = _foodPredict.value?.nutritionUsed,
                            servingSize = _analyzeFood.value?.serving_size?:0,
                            calorie = _analyzeFood.value?.nutrition?.calorie?:0f
                        )
                    ),
                    recommendWalkInfo = AIRecommendWalkModel(
                        targetDistance = getMeterToKm(checkFood?.targetDistance?:0f).toFloat(),
                        minute = checkFood?.targetTimeMinutes?:0
                    ),
                    glucoseFeedbackInfo = null
                )
            )

            _targetTimeMinutes.value = checkFood?.targetTimeMinutes

            return Response.success(_analyzeChattingList.value)
        }else{
            return Response.error(chatResponse.code(), chatResponse.errorBody())
        }
    }


    //오늘 걷기 목표 불러오기 선택 시
    suspend fun getRecommendWalkChallenge(): Response<List<ChatModel>>{
        val walkStatusResponse = safeApiCall {
            walkApiService.getWalkStatus()
        }
        if(walkStatusResponse.isSuccessful){
            val chatListPending = getChatListParsing()

            if(chatListPending.pendingModel.loadingState == LoadingState.Success){
                val chatList = ArrayList<ChatModel>()

                val walkStatus = walkStatusResponse.body()
                val isMissionHave = walkStatus?.targetDistance != null

                val walkChatList = listOf(
                    ChatModel(
                        chatUserType = ChatUserType.AI,
                        message = if(isMissionHave){
                            "오늘 걷기 미션이에요!"
                        } else {
                            "아직 생성된 걷기 미션이 없어요!\n" +
                                    "[음식분석&걷기]를 누르시고 먹은 음식을 입력한 다음\n" +
                                    "걷기 미션을 생성해보세요"
                        },
                        date = LocalDateTime.now(),
                        chatType = TodayWalkTargetType,
                        isChatAble = true,
                        isInputComplete = false,
                        chatStageType = "",
                        analysisFoodInfo = null,
                        recommendWalkInfo = if(isMissionHave){
                            AIRecommendWalkModel(
                                targetDistance = getMeterToKm(walkStatus.targetDistance),
                                minute = _targetTimeMinutes.value?:0
                            )
                        }else{
                            null
                        },
                        glucoseFeedbackInfo = null
                    )
                )

                if(chatListPending.pendingModel.data != null){
                    chatList.addAll(chatListPending.pendingModel.data)
                    chatList.addAll(walkChatList)

                    return Response.success(chatList)
                }else{
                    return Response.success(walkChatList)
                }
            }else{
                return Response.error(chatListPending.response.code(), chatListPending.response.errorBody())
            }
        }else{
            return Response.error(walkStatusResponse.code(), walkStatusResponse.errorBody())
        }
    }

    //걷기 완료 미션 전송
    suspend fun completeWalkMission(): Response<List<ChatModel>>{
        _analyzeChattingList.value = listOf(
            ChatModel(
                chatUserType = ChatUserType.AI,
                message = "걷기 완료! \uD83C\uDF89 수고했어요!\n" +
                        "이제 혈당을 입력해주세요.",
                date = LocalDateTime.now(),
                chatType = AnalysisFoodType,
                isChatAble = false,
                isInputComplete = false,
                chatStageType = AfterWalkGlucoseInputStage,
                analysisFoodInfo = null,
                recommendWalkInfo = null,
                glucoseFeedbackInfo = null
            )
        )
        return Response.success(_analyzeChattingList.value)
    }

    //식후 혈당 전송
    suspend fun afterWalkGlucoseSend(missionNo: Int?, glucose: Int): Response<List<ChatModel>>{
        val postWalkGlucoseResponse = safeApiCall {
            walkApiService.postWalkGlucose(
                missionNo = if((missionNo?:-1) > 0){
                    missionNo?:-1
                }else if((_missionNo.value?:-1) > 0){
                    _missionNo.value?:-1
                }else{
                    -1
                },
                postWalkGlucoseInputForm = PostWalkGlucoseInputForm(
                    postWalkGlucose = glucose
                )
            )
        }
        if(postWalkGlucoseResponse.isSuccessful){
            val postWalkGlucoseResponseBody = postWalkGlucoseResponse.body()

            val response = listOf(
                ChatModel(
                    chatUserType = ChatUserType.AI,
                    message = "정말 잘했어요!",
                    date = LocalDateTime.now(),
                    chatType = AnalysisFoodType,
                    isChatAble = true,
                    isInputComplete = false,
                    chatStageType = AIFeedbackStage,
                    analysisFoodInfo = null,
                    recommendWalkInfo = null,
                    glucoseFeedbackInfo = GlucoseFeedbackModel(
                        beginGlucose = postWalkGlucoseResponseBody?.preGlucose?:0,
                        aiPredictAfterGlucose = postWalkGlucoseResponseBody?.postGlucoseEst?:0,
                        realAfterGlucose = postWalkGlucoseResponseBody?.postWalkGlucose?:0,
                        targetDistance = getMeterToKm(postWalkGlucoseResponseBody?.targetDistance?:0f),
                        walkDistance = getMeterToKm(postWalkGlucoseResponseBody?.actualDistance?:0f)
                    )
                )
            )
            return Response.success(response)
        }else{
            return Response.error(postWalkGlucoseResponse.code(), postWalkGlucoseResponse.errorBody())
        }
    }
}

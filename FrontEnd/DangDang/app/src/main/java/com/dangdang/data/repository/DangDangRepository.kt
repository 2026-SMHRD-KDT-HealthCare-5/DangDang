package com.dangdang.data.repository

import com.dangdang.common.utils.AIFeedbackStage
import com.dangdang.common.utils.AfterWalkGlucoseInputStage
import com.dangdang.common.utils.AnalysisFoodStage
import com.dangdang.common.utils.AnalysisFoodType
import com.dangdang.common.utils.BeforeMealGlucoseInputStage
import com.dangdang.common.utils.BeforeMealTipType
import com.dangdang.common.utils.InputAteFoodStage
import com.dangdang.common.utils.RecommendWalkDistanceStage
import com.dangdang.common.utils.TodayWalkTargetType
import com.dangdang.data.enums.ChatUserType
import com.dangdang.data.model.chat.AIRecommendWalkModel
import com.dangdang.data.model.chat.AnalysisFoodModel
import com.dangdang.data.model.chat.ChatModel
import com.dangdang.data.model.chat.ChatRecommendQuestionModel
import com.dangdang.data.model.chat.FoodInfoModel
import com.dangdang.data.model.chat.FoodInputDirectlyForm
import com.dangdang.data.model.chat.FoodNutritionModel
import com.dangdang.data.model.chat.GlucoseFeedbackModel
import retrofit2.Response
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

class DangDangRepository @Inject constructor(

){
    //채팅 리스트 호출하기
    suspend fun getChattingList(): Response<List<ChatModel>>{
        val response = listOf(
            ChatModel(
                chatUserType = ChatUserType.AI,
                message = "안녕하세요!",
                date = LocalDateTime.of(
                    LocalDate.now(),
                    LocalTime.of(8, 30)
                ),
                chatType = "",
                isChatAble = true,
                isInputComplete = false,
                chatStageType = "",
                analysisFoodInfo = null,
                recommendWalkInfo = null,
                glucoseFeedbackInfo = null
            )
        )
        return Response.success(response)
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
        val response = listOf(
            ChatModel(
                chatUserType = ChatUserType.AI,
                message = "안녕하세요!",
                date = LocalDateTime.of(
                    LocalDate.now(),
                    LocalTime.of(8, 30)
                ),
                chatType = "",
                isChatAble = true,
                isInputComplete = false,
                chatStageType = "",
                analysisFoodInfo = null,
                recommendWalkInfo = null,
                glucoseFeedbackInfo = null
            ),
            ChatModel(
                chatUserType = ChatUserType.User,
                message = message,
                date = LocalDateTime.of(
                    LocalDate.now(),
                    LocalTime.of(8, 30)
                ),
                chatType = "",
                isChatAble = true,
                isInputComplete = false,
                chatStageType = "",
                analysisFoodInfo = null,
                recommendWalkInfo = null,
                glucoseFeedbackInfo = null
            ),
            ChatModel(
                chatUserType = ChatUserType.AI,
                message = "안녕하세요!",
                date = LocalDateTime.of(
                    LocalDate.now(),
                    LocalTime.of(8, 30)
                ),
                chatType = "",
                isChatAble = true,
                isInputComplete = false,
                chatStageType = "",
                analysisFoodInfo = null,
                recommendWalkInfo = null,
                glucoseFeedbackInfo = null
            )
        )
        return Response.success(response)
    }

    //음식 분석&걷기 시작
    suspend fun startAnalysisFood(): Response<List<ChatModel>>{
        val response = listOf(
            ChatModel(
                chatUserType = ChatUserType.AI,
                message = "식전 혈당을 알고 계신가요?\n" +
                        "(음식을 먹기 전 혈당이에요)\n" +
                        "\n" +
                        "입력해주시면 예측이\n" +
                        "더 정확해져요!",
                date = LocalDateTime.of(
                    LocalDate.now(),
                    LocalTime.of(8, 30)
                ),
                chatType = AnalysisFoodType,
                isChatAble = false,
                isInputComplete = false,
                chatStageType = BeforeMealGlucoseInputStage,
                analysisFoodInfo = null,
                recommendWalkInfo = null,
                glucoseFeedbackInfo = null
            )
        )
        return Response.success(response)
    }

    //식전 혈당 전송
    suspend fun sendBeforeMealGlucose(glucoseValue: String): Response<List<ChatModel>>{
        val response = listOf(
            ChatModel(
                chatUserType = ChatUserType.AI,
                message = "식전 혈당을 알고 계신가요?\n" +
                        "(음식을 먹기 전 혈당이에요)\n" +
                        "\n" +
                        "입력해주시면 예측이\n" +
                        "더 정확해져요!",
                date = LocalDateTime.of(
                    LocalDate.now(),
                    LocalTime.of(8, 30)
                ),
                chatType = AnalysisFoodType,
                isChatAble = false,
                isInputComplete = true,
                chatStageType = BeforeMealGlucoseInputStage,
                analysisFoodInfo = null,
                recommendWalkInfo = null,
                glucoseFeedbackInfo = null
            ),
            ChatModel(
                chatUserType = ChatUserType.User,
                message = glucoseValue,
                date = LocalDateTime.of(
                    LocalDate.now(),
                    LocalTime.of(8, 30)
                ),
                chatType = AnalysisFoodType,
                isChatAble = false,
                isInputComplete = false,
                chatStageType = BeforeMealGlucoseInputStage,
                analysisFoodInfo = null,
                recommendWalkInfo = null,
                glucoseFeedbackInfo = null
            ),
            ChatModel(
                chatUserType = ChatUserType.AI,
                message = "알겠어요!\n" +
                        "회원님의 상태(당뇨)에 맞는\n" +
                        "140mg/dL 적용할게요.",
                date = LocalDateTime.of(
                    LocalDate.now(),
                    LocalTime.of(8, 30)
                ),
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
                date = LocalDateTime.of(
                    LocalDate.now(),
                    LocalTime.of(8, 30)
                ),
                chatType = AnalysisFoodType,
                isChatAble = false,
                isInputComplete = false,
                chatStageType = InputAteFoodStage,
                analysisFoodInfo = null,
                recommendWalkInfo = null,
                glucoseFeedbackInfo = null
            )
        )
        return Response.success(response)
    }

    //음식 입력 전송
    suspend fun ateFoodSend(ateFoodValue: String): Response<List<ChatModel>>{
        val response = listOf(
            ChatModel(
                chatUserType = ChatUserType.AI,
                message = "식전 혈당을 알고 계신가요?\n" +
                        "(음식을 먹기 전 혈당이에요)\n" +
                        "\n" +
                        "입력해주시면 예측이\n" +
                        "더 정확해져요!",
                date = LocalDateTime.of(
                    LocalDate.now(),
                    LocalTime.of(8, 30)
                ),
                chatType = AnalysisFoodType,
                isChatAble = false,
                isInputComplete = true,
                chatStageType = BeforeMealGlucoseInputStage,
                analysisFoodInfo = null,
                recommendWalkInfo = null,
                glucoseFeedbackInfo = null
            ),
            ChatModel(
                chatUserType = ChatUserType.User,
                message = "모르겠어요",
                date = LocalDateTime.of(
                    LocalDate.now(),
                    LocalTime.of(8, 30)
                ),
                chatType = AnalysisFoodType,
                isChatAble = false,
                isInputComplete = false,
                chatStageType = BeforeMealGlucoseInputStage,
                analysisFoodInfo = null,
                recommendWalkInfo = null,
                glucoseFeedbackInfo = null
            ),
            ChatModel(
                chatUserType = ChatUserType.AI,
                message = "알겠어요!\n" +
                        "회원님의 상태(당뇨)에 맞는\n" +
                        "140mg/dL 적용할게요.",
                date = LocalDateTime.of(
                    LocalDate.now(),
                    LocalTime.of(8, 30)
                ),
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
                date = LocalDateTime.of(
                    LocalDate.now(),
                    LocalTime.of(8, 30)
                ),
                chatType = AnalysisFoodType,
                isChatAble = false,
                isInputComplete = true,
                chatStageType = InputAteFoodStage,
                analysisFoodInfo = null,
                recommendWalkInfo = null,
                glucoseFeedbackInfo = null
            ),
            ChatModel(
                chatUserType = ChatUserType.User,
                message = ateFoodValue,
                date = LocalDateTime.of(
                    LocalDate.now(),
                    LocalTime.of(8, 30)
                ),
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
                message = "식약처 데이터에서 찾았어요!\n" +
                        "이 음식이 맞나요?",
                date = LocalDateTime.of(
                    LocalDate.now(),
                    LocalTime.of(8, 30)
                ),
                chatType = AnalysisFoodType,
                isChatAble = false,
                isInputComplete = false,
                chatStageType = AnalysisFoodStage,
                analysisFoodInfo = AnalysisFoodModel(
                    predictedGlucoseRise = 35,
                    beginGlucose = 140,
                    foodInfo = FoodInfoModel(
                        name = "비빔밥",
                        nutritionInfo = "총 내용량 550g 1인분(1개)  / 650kcal",
                        nutritionList = listOf(
                            FoodNutritionModel(
                                name = "탄수화물",
                                unit = "g",
                                value = 15
                            ),
                            FoodNutritionModel(
                                name = "식이섬유",
                                unit = "g",
                                value = 2
                            ),
                            FoodNutritionModel(
                                name = "단백질",
                                unit = "g",
                                value = 20
                            ),
                            FoodNutritionModel(
                                name = "지방",
                                unit = "g",
                                value = 10
                            ),
                            FoodNutritionModel(
                                name = "칼로리",
                                unit = "kcal",
                                value = 250
                            )
                        )
                    )
                ),
                recommendWalkInfo = null,
                glucoseFeedbackInfo = null
            )
        )
        return Response.success(response)
    }

    //음식 직접 입력 전송
    suspend fun sendFoodInputDirectly(foodInputDirectlyForm: FoodInputDirectlyForm): Response<List<ChatModel>>{
        val response = listOf(
            ChatModel(
                chatUserType = ChatUserType.AI,
                message = "식전 혈당을 알고 계신가요?\n" +
                        "(음식을 먹기 전 혈당이에요)\n" +
                        "\n" +
                        "입력해주시면 예측이\n" +
                        "더 정확해져요!",
                date = LocalDateTime.of(
                    LocalDate.now(),
                    LocalTime.of(8, 30)
                ),
                chatType = AnalysisFoodType,
                isChatAble = false,
                isInputComplete = true,
                chatStageType = BeforeMealGlucoseInputStage,
                analysisFoodInfo = null,
                recommendWalkInfo = null,
                glucoseFeedbackInfo = null
            ),
            ChatModel(
                chatUserType = ChatUserType.User,
                message = "모르겠어요",
                date = LocalDateTime.of(
                    LocalDate.now(),
                    LocalTime.of(8, 30)
                ),
                chatType = AnalysisFoodType,
                isChatAble = false,
                isInputComplete = false,
                chatStageType = BeforeMealGlucoseInputStage,
                analysisFoodInfo = null,
                recommendWalkInfo = null,
                glucoseFeedbackInfo = null
            ),
            ChatModel(
                chatUserType = ChatUserType.AI,
                message = "알겠어요!\n" +
                        "회원님의 상태(당뇨)에 맞는\n" +
                        "140mg/dL 적용할게요.",
                date = LocalDateTime.of(
                    LocalDate.now(),
                    LocalTime.of(8, 30)
                ),
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
                date = LocalDateTime.of(
                    LocalDate.now(),
                    LocalTime.of(8, 30)
                ),
                chatType = AnalysisFoodType,
                isChatAble = false,
                isInputComplete = true,
                chatStageType = InputAteFoodStage,
                analysisFoodInfo = null,
                recommendWalkInfo = null,
                glucoseFeedbackInfo = null
            ),
            ChatModel(
                chatUserType = ChatUserType.User,
                message = "비빔밥",
                date = LocalDateTime.of(
                    LocalDate.now(),
                    LocalTime.of(8, 30)
                ),
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
                message = "식약처 데이터에서 찾았어요!\n" +
                        "이 음식이 맞나요?",
                date = LocalDateTime.of(
                    LocalDate.now(),
                    LocalTime.of(8, 30)
                ),
                chatType = AnalysisFoodType,
                isChatAble = false,
                isInputComplete = true,
                chatStageType = AnalysisFoodStage,
                analysisFoodInfo = AnalysisFoodModel(
                    predictedGlucoseRise = 35,
                    beginGlucose = 140,
                    foodInfo = FoodInfoModel(
                        name = "비빔밥",
                        nutritionInfo = "총 내용량 550g 1인분(1개)  / 650kcal",
                        nutritionList = listOf(
                            FoodNutritionModel(
                                name = "탄수화물",
                                unit = "g",
                                value = 15
                            ),
                            FoodNutritionModel(
                                name = "식이섬유",
                                unit = "g",
                                value = 2
                            ),
                            FoodNutritionModel(
                                name = "단백질",
                                unit = "g",
                                value = 20
                            ),
                            FoodNutritionModel(
                                name = "지방",
                                unit = "g",
                                value = 10
                            ),
                            FoodNutritionModel(
                                name = "칼로리",
                                unit = "kcal",
                                value = 250
                            )
                        )
                    )
                ),
                recommendWalkInfo = null,
                glucoseFeedbackInfo = null
            ),
            ChatModel(
                chatUserType = ChatUserType.User,
                message = "이 영양성분 정보야.",
                date = LocalDateTime.of(
                    LocalDate.now(),
                    LocalTime.of(8, 30)
                ),
                chatType = AnalysisFoodType,
                isChatAble = false,
                isInputComplete = false,
                chatStageType = AnalysisFoodStage,
                analysisFoodInfo = null,
                recommendWalkInfo = null,
                glucoseFeedbackInfo = null
            ),
            ChatModel(
                chatUserType = ChatUserType.AI,
                message = "기록할 음식이 확정되었어요!\n" +
                        "이제 식후 30분 이후\n" +
                        "걷기를 시작해볼까요?",
                date = LocalDateTime.of(
                    LocalDate.now(),
                    LocalTime.of(8, 30)
                ),
                chatType = AnalysisFoodType,
                isChatAble = true,
                isInputComplete = false,
                chatStageType = RecommendWalkDistanceStage,
                analysisFoodInfo = AnalysisFoodModel(
                    predictedGlucoseRise = 35,
                    beginGlucose = 140,
                    foodInfo = FoodInfoModel(
                        name = "비빔밥",
                        nutritionInfo = "총 내용량 550g 1인분(1개)  / 650kcal",
                        nutritionList = listOf(
                            FoodNutritionModel(
                                name = "탄수화물",
                                unit = "g",
                                value = 15
                            ),
                            FoodNutritionModel(
                                name = "식이섬유",
                                unit = "g",
                                value = 2
                            ),
                            FoodNutritionModel(
                                name = "단백질",
                                unit = "g",
                                value = 20
                            ),
                            FoodNutritionModel(
                                name = "지방",
                                unit = "g",
                                value = 10
                            ),
                            FoodNutritionModel(
                                name = "칼로리",
                                unit = "kcal",
                                value = 250
                            )
                        )
                    )
                ),
                recommendWalkInfo = AIRecommendWalkModel(
                    targetDistance = 2.6f,
                    minute = 30
                ),
                glucoseFeedbackInfo = null
            )
        )
        return Response.success(response)
    }

    //검색어 다시 입력 선택 시
    suspend fun ateFoodReSearch(): Response<List<ChatModel>>{
        val response = listOf(
            ChatModel(
                chatUserType = ChatUserType.AI,
                message = "식전 혈당을 알고 계신가요?\n" +
                        "(음식을 먹기 전 혈당이에요)\n" +
                        "\n" +
                        "입력해주시면 예측이\n" +
                        "더 정확해져요!",
                date = LocalDateTime.of(
                    LocalDate.now(),
                    LocalTime.of(8, 30)
                ),
                chatType = AnalysisFoodType,
                isChatAble = false,
                isInputComplete = true,
                chatStageType = BeforeMealGlucoseInputStage,
                analysisFoodInfo = null,
                recommendWalkInfo = null,
                glucoseFeedbackInfo = null
            ),
            ChatModel(
                chatUserType = ChatUserType.User,
                message = "모르겠어요",
                date = LocalDateTime.of(
                    LocalDate.now(),
                    LocalTime.of(8, 30)
                ),
                chatType = AnalysisFoodType,
                isChatAble = false,
                isInputComplete = false,
                chatStageType = BeforeMealGlucoseInputStage,
                analysisFoodInfo = null,
                recommendWalkInfo = null,
                glucoseFeedbackInfo = null
            ),
            ChatModel(
                chatUserType = ChatUserType.AI,
                message = "알겠어요!\n" +
                        "회원님의 상태(당뇨)에 맞는\n" +
                        "140mg/dL 적용할게요.",
                date = LocalDateTime.of(
                    LocalDate.now(),
                    LocalTime.of(8, 30)
                ),
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
                date = LocalDateTime.of(
                    LocalDate.now(),
                    LocalTime.of(8, 30)
                ),
                chatType = AnalysisFoodType,
                isChatAble = false,
                isInputComplete = false,
                chatStageType = InputAteFoodStage,
                analysisFoodInfo = null,
                recommendWalkInfo = null,
                glucoseFeedbackInfo = null
            )
        )
        return Response.success(response)
    }

    //음식 확정 선택 시
    suspend fun foodCheck(): Response<List<ChatModel>>{
        val response = listOf(
            ChatModel(
                chatUserType = ChatUserType.AI,
                message = "식전 혈당을 알고 계신가요?\n" +
                        "(음식을 먹기 전 혈당이에요)\n" +
                        "\n" +
                        "입력해주시면 예측이\n" +
                        "더 정확해져요!",
                date = LocalDateTime.of(
                    LocalDate.now(),
                    LocalTime.of(8, 30)
                ),
                chatType = AnalysisFoodType,
                isChatAble = false,
                isInputComplete = true,
                chatStageType = BeforeMealGlucoseInputStage,
                analysisFoodInfo = null,
                recommendWalkInfo = null,
                glucoseFeedbackInfo = null
            ),
            ChatModel(
                chatUserType = ChatUserType.User,
                message = "모르겠어요",
                date = LocalDateTime.of(
                    LocalDate.now(),
                    LocalTime.of(8, 30)
                ),
                chatType = AnalysisFoodType,
                isChatAble = false,
                isInputComplete = false,
                chatStageType = BeforeMealGlucoseInputStage,
                analysisFoodInfo = null,
                recommendWalkInfo = null,
                glucoseFeedbackInfo = null
            ),
            ChatModel(
                chatUserType = ChatUserType.AI,
                message = "알겠어요!\n" +
                        "회원님의 상태(당뇨)에 맞는\n" +
                        "140mg/dL 적용할게요.",
                date = LocalDateTime.of(
                    LocalDate.now(),
                    LocalTime.of(8, 30)
                ),
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
                date = LocalDateTime.of(
                    LocalDate.now(),
                    LocalTime.of(8, 30)
                ),
                chatType = AnalysisFoodType,
                isChatAble = false,
                isInputComplete = true,
                chatStageType = InputAteFoodStage,
                analysisFoodInfo = null,
                recommendWalkInfo = null,
                glucoseFeedbackInfo = null
            ),
            ChatModel(
                chatUserType = ChatUserType.User,
                message = "비빔밥",
                date = LocalDateTime.of(
                    LocalDate.now(),
                    LocalTime.of(8, 30)
                ),
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
                message = "식약처 데이터에서 찾았어요!\n" +
                        "이 음식이 맞나요?",
                date = LocalDateTime.of(
                    LocalDate.now(),
                    LocalTime.of(8, 30)
                ),
                chatType = AnalysisFoodType,
                isChatAble = false,
                isInputComplete = true,
                chatStageType = AnalysisFoodStage,
                analysisFoodInfo = AnalysisFoodModel(
                    predictedGlucoseRise = 35,
                    beginGlucose = 140,
                    foodInfo = FoodInfoModel(
                        name = "비빔밥",
                        nutritionInfo = "총 내용량 550g 1인분(1개)  / 650kcal",
                        nutritionList = listOf(
                            FoodNutritionModel(
                                name = "탄수화물",
                                unit = "g",
                                value = 15
                            ),
                            FoodNutritionModel(
                                name = "식이섬유",
                                unit = "g",
                                value = 2
                            ),
                            FoodNutritionModel(
                                name = "단백질",
                                unit = "g",
                                value = 20
                            ),
                            FoodNutritionModel(
                                name = "지방",
                                unit = "g",
                                value = 10
                            ),
                            FoodNutritionModel(
                                name = "칼로리",
                                unit = "kcal",
                                value = 250
                            )
                        )
                    )
                ),
                recommendWalkInfo = null,
                glucoseFeedbackInfo = null
            ),
            ChatModel(
                chatUserType = ChatUserType.AI,
                message = "기록할 음식이 확정되었어요!\n" +
                        "이제 식후 30분 이후\n" +
                        "걷기를 시작해볼까요?",
                date = LocalDateTime.of(
                    LocalDate.now(),
                    LocalTime.of(8, 30)
                ),
                chatType = AnalysisFoodType,
                isChatAble = true,
                isInputComplete = false,
                chatStageType = RecommendWalkDistanceStage,
                analysisFoodInfo = AnalysisFoodModel(
                    predictedGlucoseRise = 35,
                    beginGlucose = 140,
                    foodInfo = FoodInfoModel(
                        name = "비빔밥",
                        nutritionInfo = "총 내용량 550g 1인분(1개)  / 650kcal",
                        nutritionList = listOf(
                            FoodNutritionModel(
                                name = "탄수화물",
                                unit = "g",
                                value = 15
                            ),
                            FoodNutritionModel(
                                name = "식이섬유",
                                unit = "g",
                                value = 2
                            ),
                            FoodNutritionModel(
                                name = "단백질",
                                unit = "g",
                                value = 20
                            ),
                            FoodNutritionModel(
                                name = "지방",
                                unit = "g",
                                value = 10
                            ),
                            FoodNutritionModel(
                                name = "칼로리",
                                unit = "kcal",
                                value = 250
                            )
                        )
                    )
                ),
                recommendWalkInfo = AIRecommendWalkModel(
                    targetDistance = 2.6f,
                    minute = 30
                ),
                glucoseFeedbackInfo = null
            )
        )
        return Response.success(response)
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
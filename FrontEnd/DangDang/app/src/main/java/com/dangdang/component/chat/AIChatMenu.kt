package com.dangdang.component.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.common.utils.AIFeedbackStage
import com.dangdang.common.utils.AfterWalkGlucoseInputStage
import com.dangdang.common.utils.AnalysisFoodStage
import com.dangdang.common.utils.AnalysisFoodType
import com.dangdang.common.utils.BeforeMealGlucoseInputStage
import com.dangdang.common.utils.InputAteFoodStage
import com.dangdang.common.utils.InputAteWeightStage
import com.dangdang.common.utils.RecommendWalkDistanceStage
import com.dangdang.common.utils.TodayWalkTargetType
import com.dangdang.common.utils.regular
import com.dangdang.component.button.PrimaryButton
import com.dangdang.component.text.textfield.TextField
import com.dangdang.data.enums.ChatUserType
import com.dangdang.data.enums.LayoutSize
import com.dangdang.data.model.chat.AIRecommendWalkModel
import com.dangdang.data.model.chat.AnalysisFoodModel
import com.dangdang.data.model.chat.ChatModel
import com.dangdang.data.model.chat.FoodInfoModel
import com.dangdang.data.model.chat.FoodNutritionModel
import com.dangdang.data.model.chat.GlucoseFeedbackModel
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Gray
import com.dangdang.ui.theme.White
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Preview
@Composable
fun AIChatMenuPreview(){
    Column(
        modifier = Modifier
            .background(
                color = White
            ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        //일반 채팅
        AIChatMenu(
            chatModel = ChatModel(
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

        //오늘 걷기 목표 타입
        AIChatMenu(
            chatModel = ChatModel(
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

        //음식 분석-식전 혈당 체크
        AIChatMenu(
            chatModel = ChatModel(
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

        //음식 분석-먹은 음식 입력
        AIChatMenu(
            chatModel = ChatModel(
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
    }
}

@Preview
@Composable
fun AIChatMenuPreview2(){
    Column(
        modifier = Modifier
            .background(
                color = White
            ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        //음식 분석-먹은 음식 확인
        AIChatMenu(
            chatModel = ChatModel(
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
    }
}

@Preview
@Composable
fun AIChatMenuPreview3(){
    Column(
        modifier = Modifier
            .background(
                color = White
            ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        //음식 분석 - 걷기 챌린지 추천 단계
        AIChatMenu(
            chatModel = ChatModel(
                chatUserType = ChatUserType.AI,
                message = "기록할 음식이 확정되었어요!\n" +
                        "이제 식후 30분 이후\n" +
                        "걷기를 시작해볼까요?",
                date = LocalDateTime.of(
                    LocalDate.now(),
                    LocalTime.of(8, 30)
                ),
                chatType = AnalysisFoodType,
                isChatAble = false,
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
    }
}

@Preview
@Composable
fun AIChatMenuPreview4(){
    Column(
        modifier = Modifier
            .background(
                color = White
            ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        //음식 분석 - 식후 혈당 입력
        AIChatMenu(
            chatModel = ChatModel(
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

        //음식 분석 - AI 피드백
        AIChatMenu(
            chatModel = ChatModel(
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
                    walkDistance = 2.5f
                )
            )
        )
    }
}

@Composable
fun AIChatMenu(
    chatModel: ChatModel,
    glucoseValue: String = "",
    onGlucoseValueChange: (String) -> Unit = {},
    ateFoodValue: String = "",
    onAteFoodValueChange: (String) -> Unit = {},
    onAteFoodSendClick: () -> Unit = {},
    ateWeightValue: String = "",
    onAteWeightValueChange: (String) -> Unit = {},
    onAteWeightSendClick: () -> Unit = {},
    afterWalkGlucoseValue: String = "",
    onAfterWalkGlucoseValueChange: (String) -> Unit = {},
    onAfterWalkGlucoseInputCompleteClick: () -> Unit = {},
    onChallengeClick: () -> Unit = {},
    onGlucoseInputCompleteClick: () -> Unit = {},
    onGlucoseInputCancelClick: () -> Unit = {},
    onFoodCheckClick: () -> Unit = {},
    onFoodAIAnalysisClick: () -> Unit = {},
    onFoodKeywordInputClick: () -> Unit = {},
    onFoodInputDirectlyClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        if(chatModel.chatUserType == ChatUserType.User)
            UserChatView(
                message = chatModel.message,
                sendTime = chatModel.date
            )
        else{
            AIChatView(
                message = chatModel.message,
                sendTime = chatModel.date
            )

            if(chatModel.chatType.isNotEmpty()){
                //오늘 걷기 목표 타입일 경우
                if(chatModel.chatType == TodayWalkTargetType){
                    AIRecommendWalkChallengeBox(
                        targetDistance =
                            chatModel.recommendWalkInfo?.targetDistance?:0f,
                        minute = chatModel.recommendWalkInfo?.minute?:0,
                        onChallengeClick = onChallengeClick
                    )
                }else if(chatModel.chatType == AnalysisFoodType){
                    //음식 분석 타입인 경우
                    when(chatModel.chatStageType){
                        BeforeMealGlucoseInputStage -> {
                            if(!chatModel.isInputComplete){
                                BeginGlucoseInputMenuBox(
                                    glucoseValue = glucoseValue,
                                    onGlucoseValueChange = onGlucoseValueChange,
                                    onGlucoseInputCompleteClick = onGlucoseInputCompleteClick,
                                    onGlucoseInputCancelClick = onGlucoseInputCancelClick
                                )
                            }
                        }
                        InputAteFoodStage -> {
                            if(!chatModel.isInputComplete){
                                TextField(
                                    isMaxLengthView = false,
                                    value = ateFoodValue,
                                    onValueChange = onAteFoodValueChange,
                                    placeholderText = "예) 김치찌개, 닭가슴살 샐러드",
                                    maxLength = 100,
                                    sizeType = LayoutSize.FillMaxSize
                                )
                                PrimaryButton(
                                    text = "확인",
                                    enabled = ateFoodValue.isNotEmpty(),
                                    sizeType = LayoutSize.FillMaxSize,
                                    onClick = onAteFoodSendClick
                                )
                            }
                        }
                        InputAteWeightStage ->{
                            if(!chatModel.isInputComplete){
                                TextField(
                                    isMaxLengthView = false,
                                    value = ateWeightValue,
                                    onValueChange = onAteWeightValueChange,
                                    placeholderText = "예)150",
                                    maxLength = 20,
                                    sizeType = LayoutSize.FillMaxSize,
                                    keyboardType = KeyboardType.Number,
                                    rightIcon = {
                                        Text(
                                            text = "g",
                                            style = AppTypography.labelLarge.regular,
                                            color = Gray,
                                        )
                                    }
                                )
                                PrimaryButton(
                                    text = "입력 완료",
                                    enabled = ateWeightValue.isNotEmpty(),
                                    sizeType = LayoutSize.FillMaxSize,
                                    onClick = onAteWeightSendClick
                                )
                            }
                        }
                        AnalysisFoodStage -> {
                            FoodDetailBox(
                                predictedGlucoseRise =
                                    chatModel
                                        .analysisFoodInfo
                                        ?.predictedGlucoseRise?:0,
                                beginGlucose = chatModel
                                    .analysisFoodInfo
                                    ?.beginGlucose?:0,
                                foodInfo = chatModel.analysisFoodInfo?.foodInfo
                                    ?: FoodInfoModel(
                                        name = "",
                                        nutritionInfo = "",
                                        nutritionList = emptyList()
                                    ),
                                isMenuShow = !chatModel.isInputComplete,
                                onCheckClick = onFoodCheckClick,
                                onAIAnalysisClick = onFoodAIAnalysisClick,
                                onKeywordInputClick = onFoodKeywordInputClick,
                                onInputDirectlyClick = onFoodInputDirectlyClick
                            )
                        }
                        RecommendWalkDistanceStage -> {
                            FoodDetailBox(
                                predictedGlucoseRise =
                                    chatModel
                                        .analysisFoodInfo
                                        ?.predictedGlucoseRise?:0,
                                beginGlucose = chatModel
                                    .analysisFoodInfo
                                    ?.beginGlucose?:0,
                                foodInfo = chatModel.analysisFoodInfo?.foodInfo
                                    ?: FoodInfoModel(
                                        name = "",
                                        nutritionInfo = "",
                                        nutritionList = emptyList()
                                    ),
                                isMenuShow = false,
                            )
                            AIWalkTip()
                            AIWarning()
                            AIRecommendWalkChallengeBox(
                                targetDistance =
                                    chatModel.recommendWalkInfo?.targetDistance?:0f,
                                minute = chatModel.recommendWalkInfo?.minute?:0,
                                onChallengeClick = onChallengeClick
                            )
                        }
                        AfterWalkGlucoseInputStage -> {
                            AfterWalkGlucoseInputMenuBox(
                                afterWalkGlucoseValue = afterWalkGlucoseValue,
                                onAfterWalkGlucoseValueChange = onAfterWalkGlucoseValueChange,
                                onGlucoseInputCompleteClick = onAfterWalkGlucoseInputCompleteClick
                            )
                        }
                        AIFeedbackStage -> {
                            chatModel.glucoseFeedbackInfo?.let {
                                AIGlucoseFeedbackBox(
                                    glucoseFeedbackModel = it
                                )
                                AICelebrationBox(
                                    isSuccess =
                                        (it.targetDistance) <= (it.walkDistance)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
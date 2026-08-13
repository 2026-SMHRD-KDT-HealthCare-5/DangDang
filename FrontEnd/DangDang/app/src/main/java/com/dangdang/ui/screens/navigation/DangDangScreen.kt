package com.dangdang.ui.screens.navigation

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.dangdang.common.utils.AnalysisFoodType
import com.dangdang.common.utils.BeforeMealTipType
import com.dangdang.common.utils.mainScreen
import com.dangdang.common.utils.TodayWalkTargetType
import com.dangdang.component.button.outlined.SecondaryOutlinedButton
import com.dangdang.component.chat.AIChatListView
import com.dangdang.component.errorview.ErrorView
import com.dangdang.component.navigation.topnavigation.TopNavigation
import com.dangdang.component.text.textbox.ChatSendBox
import com.dangdang.data.enums.ChatUserType
import com.dangdang.data.enums.LoadingState
import com.dangdang.data.model.chat.ChatModel
import com.dangdang.data.model.chat.ChatRecommendQuestionModel
import com.dangdang.data.model.chat.FoodInputDirectlyForm
import com.dangdang.ui.viewmodel.dangdang.DangDangViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Preview
@Composable
fun DangDangScreenPreview(

){
    DangDangScreenContent(
        chatMessageValue = "",
        onChatMessageValueChange = {},
        isChatAble = true,
        recommendQuestionList = listOf(
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
        ),
        chattingList = listOf(
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
        ),
        onRecommendQuestionClick = {},
        glucoseValue = "",
        onGlucoseValueChange = {},
        ateFoodValue = "",
        onAteFoodValueChange = {},
        onAteFoodSendClick = {},
        ateWeightValue = "",
        onAteWeightValueChange = {},
        onAteWeightSendClick = {},
        afterWalkGlucoseValue = "",
        onAfterWalkGlucoseValueChange = {},
        onAfterWalkGlucoseInputCompleteClick = {},
        onChallengeClick = {},
        onGlucoseInputCompleteClick = {},
        onChatSendClick = {},
        onGlucoseInputCancelClick = {},
        onFoodCheckClick = {},
        onFoodAIAnalysisClick = {},
        onFoodKeywordInputClick = {},
        onFoodInputDirectlyClick = {}
    )
}

@Composable
fun DangDangScreen(
    dangDangViewModel: DangDangViewModel = hiltViewModel(),
    onWalkChallengeMove: () -> Unit,
    isWalkComplete: Boolean,
    onFoodInputDirectlyClick: () -> Unit,
    navController: NavController,
){
    val savedStateHandle =
        navController.currentBackStackEntry?.savedStateHandle

    var chatMessageValue by remember { mutableStateOf("") }

    val recommendQuestionList by
        dangDangViewModel.recommendQuestionList.collectAsState()

    val chattingList by
        dangDangViewModel.chattingList.collectAsState()

    val isChatAble by remember(chattingList) {
        derivedStateOf{
            if(chattingList.loadingState == LoadingState.Success){
                if(chattingList.data?.isEmpty() == true){
                    true
                }else{
                    chattingList.data?.last()?.isChatAble == true
                }
            }else{
                false
            }
        }
    }

    var glucoseValue by remember {
        mutableStateOf("")
    }

    var ateFoodValue by remember {
        mutableStateOf("")
    }

    var ateWeightValue by remember {
        mutableStateOf("")
    }

    var afterWalkGlucoseValue by remember {
        mutableStateOf("")
    }

    //걷기 완료하고 왔을 경우
    val isWalkCompleteByHandle by savedStateHandle
        ?.getStateFlow("isWalkComplete", false)
        ?.collectAsState()
        ?: remember { mutableStateOf(false) }

    //음식 입력 직접하고 왔을 경우
    val isFoodInputDirectlySend by savedStateHandle
        ?.getStateFlow("isFoodInputDirectlySend", false)
        ?.collectAsState()
        ?: remember { mutableStateOf(false) }
    val foodInputDirectlyForm by savedStateHandle
        ?.getStateFlow("foodInputDirectlyForm", null)
        ?.collectAsState()
        ?: remember { mutableStateOf<FoodInputDirectlyForm?>(null) }

    var isInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(isWalkComplete, isWalkCompleteByHandle, isFoodInputDirectlySend) {
        if(isWalkComplete || isWalkCompleteByHandle){
            dangDangViewModel.completeWalkMission()
            savedStateHandle?.remove<Boolean>("isWalkComplete")
            isInitialized = true
        }else if(isFoodInputDirectlySend){
            foodInputDirectlyForm?.let{
                dangDangViewModel.sendFoodInputDirectly(
                    it
                )
            }

            savedStateHandle?.remove<FoodInputDirectlyForm>("foodInputDirectlyForm")
            savedStateHandle?.remove<Boolean>("isFoodInputDirectlySend")
            isInitialized = true
        }else if(!isInitialized){
            dangDangViewModel.getChattingList()
            isInitialized = true
        }
    }

    if(chattingList.loadingState == LoadingState.Success
        && recommendQuestionList.loadingState == LoadingState.Success){
        DangDangScreenContent(
            chatMessageValue = chatMessageValue,
            onChatMessageValueChange = {
                chatMessageValue = it
            },
            isChatAble = isChatAble,
            recommendQuestionList = recommendQuestionList.data?:emptyList(),
            chattingList = chattingList.data?:emptyList(),
            onRecommendQuestionClick = {
                dangDangViewModel.onRecommendQuestionClick(
                    it
                )
            },
            glucoseValue = glucoseValue,
            onGlucoseValueChange = {
                glucoseValue = it
            },
            ateFoodValue = ateFoodValue,
            onAteFoodValueChange = {
                ateFoodValue = it
            },
            onAteFoodSendClick = {
                dangDangViewModel.ateFoodSend(
                    ateFoodValue
                )
            },
            ateWeightValue = ateWeightValue,
            onAteWeightValueChange = {
                ateWeightValue = it
            },
            onAteWeightSendClick = {
                dangDangViewModel.ateWeightSend(
                    ateWeightValue
                )
            },
            afterWalkGlucoseValue = afterWalkGlucoseValue,
            onAfterWalkGlucoseValueChange = {
                afterWalkGlucoseValue = it
            },
            onAfterWalkGlucoseInputCompleteClick = {
                dangDangViewModel.afterWalkGlucoseSend(
                    afterWalkGlucoseValue.toInt()
                )
            },
            onChallengeClick = {
                onWalkChallengeMove()
            },
            onGlucoseInputCompleteClick = {
                dangDangViewModel.sendBeforeMealGlucose(
                    glucoseValue
                )
            },
            onGlucoseInputCancelClick = {
                dangDangViewModel.sendBeforeMealGlucose(
                    "모르겠어요"
                )
            },
            onFoodCheckClick = {
                dangDangViewModel.foodCheck()
            },
            onFoodAIAnalysisClick = {
                dangDangViewModel.ateFoodSend(
                    ateFoodValue
                )
            },
            onFoodKeywordInputClick = {
                dangDangViewModel.ateFoodReSearch()
            },
            onFoodInputDirectlyClick = {
                onFoodInputDirectlyClick()
            },
            onChatSendClick = {
                dangDangViewModel.chatSend(
                    chatMessageValue
                )
                chatMessageValue = ""
            }
        )
    }else{
        ErrorView(
            loadingState = if(
                (chattingList.loadingState == LoadingState.Error
                    || recommendQuestionList.loadingState == LoadingState.Error)
            ){
                LoadingState.Error
            }else{
                LoadingState.Loading
            },
            message = "채팅 정보 또는 추천 질문 불러오기를 실패했습니다."
        )
    }
}

@Composable
fun DangDangScreenContent(
    chatMessageValue: String,
    onChatMessageValueChange: (String) -> Unit,
    isChatAble: Boolean,
    recommendQuestionList: List<ChatRecommendQuestionModel>,
    chattingList: List<ChatModel>,
    onRecommendQuestionClick: (ChatRecommendQuestionModel) -> Unit,
    glucoseValue: String,
    onGlucoseValueChange: (String) -> Unit,
    ateFoodValue: String,
    onAteFoodValueChange: (String) -> Unit,
    onAteFoodSendClick: () -> Unit,
    ateWeightValue: String,
    onAteWeightValueChange: (String) -> Unit,
    onAteWeightSendClick: () -> Unit,
    afterWalkGlucoseValue: String,
    onAfterWalkGlucoseValueChange: (String) -> Unit,
    onAfterWalkGlucoseInputCompleteClick: () -> Unit,
    onChallengeClick: () -> Unit,
    onGlucoseInputCompleteClick: () -> Unit,
    onGlucoseInputCancelClick: () -> Unit,
    onFoodCheckClick: () -> Unit,
    onFoodAIAnalysisClick: () -> Unit,
    onFoodKeywordInputClick: () -> Unit,
    onFoodInputDirectlyClick: () -> Unit,
    onChatSendClick: () -> Unit
){

    Column(
        modifier = Modifier
            .mainScreen()
            .imePadding()
    ) {
        TopNavigation(
            title = "AI 건강 비서 당당이"
        )

        AIChatListView(
            modifier = Modifier
                .weight(1f),
            chattingList = chattingList,
            glucoseValue = glucoseValue,
            onGlucoseValueChange = onGlucoseValueChange,
            ateFoodValue = ateFoodValue,
            onAteFoodValueChange = onAteFoodValueChange,
            onAteFoodSendClick = onAteFoodSendClick,
            ateWeightValue = ateWeightValue,
            onAteWeightValueChange = onAteWeightValueChange,
            onAteWeightSendClick = onAteWeightSendClick,
            afterWalkGlucoseValue = afterWalkGlucoseValue,
            onAfterWalkGlucoseValueChange = onAfterWalkGlucoseValueChange,
            onAfterWalkGlucoseInputCompleteClick = onAfterWalkGlucoseInputCompleteClick,
            onChallengeClick = onChallengeClick,
            onGlucoseInputCompleteClick = onGlucoseInputCompleteClick,
            onGlucoseInputCancelClick = onGlucoseInputCancelClick,
            onFoodCheckClick = onFoodCheckClick,
            onFoodAIAnalysisClick = onFoodAIAnalysisClick,
            onFoodKeywordInputClick = onFoodKeywordInputClick,
            onFoodInputDirectlyClick = onFoodInputDirectlyClick
        )

        if(isChatAble){
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 10.dp
                    ),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ){
                items(recommendQuestionList) { item ->
                    SecondaryOutlinedButton(
                        text = item.question,
                        onClick = {
                            onRecommendQuestionClick(item)
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        ChatSendBox(
            enabled = isChatAble,
            value = chatMessageValue,
            onValueChange = onChatMessageValueChange,
            onSendClick = onChatSendClick
        )

        Spacer(Modifier.height(4.dp))
    }
}
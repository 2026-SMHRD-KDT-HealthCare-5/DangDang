package com.dangdang.component.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.data.enums.ChatUserType
import com.dangdang.data.model.chat.ChatModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Preview
@Composable
fun AIChatListViewPreview(){
    AIChatListView(
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
                goalGlucose = 180,
                weeklyMissionCompleteCount = 6,
                chatStageType = "",
                analysisFoodInfo = null,
                recommendWalkInfo = null,
                glucoseFeedbackInfo = null
            )
        )
    )
}

@Composable
fun AIChatListView(
    modifier: Modifier = Modifier,
    chattingList: List<ChatModel>,
    glucoseValue: String = "",
    onGlucoseValueChange: (String) -> Unit = {},
    ateFoodValue: String = "",
    onAteFoodValueChange: (String) -> Unit = {},
    onAteFoodSendClick: () -> Unit = {},
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
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(
                horizontal = 8.dp,
                vertical = 15.dp
            ),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        chattingList.forEach { chatting ->
            AIChatMenu(
                chatModel = chatting,
                glucoseValue = glucoseValue,
                onGlucoseValueChange = onGlucoseValueChange,
                ateFoodValue = ateFoodValue,
                onAteFoodValueChange = onAteFoodValueChange,
                onAteFoodSendClick = onAteFoodSendClick,
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
        }
    }
}
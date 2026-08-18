package com.dangdang.component.chat

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
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
    ateFoodImageUri: Uri? = null,
    ateFoodValue: String = "",
    onAteFoodValueChange: (String) -> Unit = {},
    onAteFoodSendClick: () -> Unit = {},
    onAteFoodImageSelectClick: () -> Unit = {},
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
    val scrollState = rememberScrollState()

    // AIChatListView가 최초로 화면에 구성될 때만, 목록 레이아웃이 끝난 뒤 최하단으로 이동한다.
    // chattingList를 key로 사용하지 않아 이후 메시지 추가/상태 변경으로는 다시 실행되지 않는다.
    LaunchedEffect(Unit) {
        withFrameNanos { }
        scrollState.scrollTo(scrollState.maxValue)
    }

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
                ateFoodImageUri = ateFoodImageUri,
                onAteFoodImageSelectClick = onAteFoodImageSelectClick,
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
        }
    }
}

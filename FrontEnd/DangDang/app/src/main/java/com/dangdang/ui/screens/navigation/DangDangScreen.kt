package com.dangdang.ui.screens.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.common.utils.mainScreen
import com.dangdang.common.utils.regular
import com.dangdang.component.navigation.topnavigation.TopNavigation
import com.dangdang.component.text.textbox.ChatSendBox
import com.dangdang.ui.theme.AppTypography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Preview
@Composable
fun DangDangScreenPreview(

){
    DangDangScreenContent(
        chatMessageValue = "",
        onChatMessageValueChange = {}
    )
}

@Composable
fun DangDangScreen(

){
    var chatMessageValue by remember { mutableStateOf("") }

    DangDangScreenContent(
        chatMessageValue = chatMessageValue,
        onChatMessageValueChange = {
            chatMessageValue = it
        }
    )
}

@Composable
fun DangDangScreenContent(
    chatMessageValue: String,
    onChatMessageValueChange: (String) -> Unit
){
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .mainScreen()
            .imePadding()
    ) {
        TopNavigation(
            title = "AI 건강 비서 당당이"
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(
                    horizontal = 8.dp,
                    vertical = 15.dp
                ),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {

        }

        ChatSendBox(
            value = chatMessageValue,
            onValueChange = onChatMessageValueChange,
            onSendClick = {

            }
        )

        Spacer(Modifier.height(4.dp))
    }
}
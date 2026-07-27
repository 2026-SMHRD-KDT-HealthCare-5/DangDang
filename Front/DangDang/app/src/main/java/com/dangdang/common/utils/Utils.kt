package com.dangdang.common.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

//화면마다 공통으로 사용하는 modifier
fun Modifier.screen() = this
    .fillMaxSize()
    .background(Color.White)
    .systemBarsPadding()

val TextStyle.regular: TextStyle
    get() = copy(fontWeight = FontWeight.Normal)

val TextStyle.medium: TextStyle
    get() = copy(fontWeight = FontWeight.Medium)

val TextStyle.bold: TextStyle
    get() = copy(fontWeight = FontWeight.Bold)
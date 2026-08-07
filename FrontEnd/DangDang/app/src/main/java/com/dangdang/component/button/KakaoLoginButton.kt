package com.dangdang.component.button

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dangdang.R
import com.dangdang.common.utils.bold
import com.dangdang.common.utils.componentWidthModifier
import com.dangdang.data.enums.LayoutSize
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.KakaoYellow
import com.dangdang.ui.theme.LightGray
import com.dangdang.ui.theme.White

@Preview
@Composable
fun KakaoLoginButtonPreview(

){
    KakaoLoginButton(
        sizeType = LayoutSize.FillMaxSize,
        onClick = {}
    )
}

@Composable
fun KakaoLoginButton(
    fixWidth: Dp? = null,
    sizeType: LayoutSize = LayoutSize.DefaultSize,
    onClick: () -> Unit
){
    Row(
        modifier = Modifier
            .background(
                color = KakaoYellow,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
            .componentWidthModifier(
                fixWidth = fixWidth,
                sizeType = sizeType
            )
            .clickable(
                onClick = onClick,
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.mipmap.kakao_login),
            contentDescription = "로그인 아이콘",
            modifier = Modifier.size(24.dp)
        )

        Text(
            text = "카카오로 로그인",
            style = AppTypography.bodyLarge.bold,
            color = Black,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
    }
}
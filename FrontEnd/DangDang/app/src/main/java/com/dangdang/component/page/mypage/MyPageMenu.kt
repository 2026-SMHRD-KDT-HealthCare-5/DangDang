package com.dangdang.component.page.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.R
import com.dangdang.common.utils.regular
import com.dangdang.component.button.ListButton
import com.dangdang.component.divider.Divider
import com.dangdang.component.toggle.Switch
import com.dangdang.data.enums.DividerPosition
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.LightGray
import com.dangdang.ui.theme.MediumRoundShape
import com.dangdang.ui.theme.ThinLineDp
import com.dangdang.ui.theme.White

@Preview
@Composable
fun MyPageMenuPreview(

){
    MyPageMenu(
        isSwitchChecked = true,
        onSwitchCheckChange = {},
        onFaqClick = {},
        onInquiryClick = {},
        onLogoutClick = {}
    )
}

@Composable
fun MyPageMenu(
    isSwitchChecked: Boolean,
    onSwitchCheckChange: () -> Unit,
    onFaqClick: () -> Unit,
    onInquiryClick: () -> Unit,
    onLogoutClick: () -> Unit
){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = White
            ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "환경설정",
            style = AppTypography.labelLarge.regular,
            color = Black,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = White,
                    shape = MediumRoundShape
                )
                .border(
                    width = ThinLineDp,
                    color = LightGray,
                    shape = MediumRoundShape
                )
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ListButton(
                leftIcon = {
                    Icon(
                        painter = painterResource(R.drawable.bell),
                        contentDescription = "left icon",
                        modifier = Modifier
                            .size(24.dp)
                    )
                },
                rightIcon = {
                    Switch(
                        isCheck = isSwitchChecked,
                        onCheckChange = onSwitchCheckChange
                    )
                },
                title = "알림설정",
                onClick = {}
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
            ){
                Divider(position = DividerPosition.Horizontal)
            }

            ListButton(
                leftIcon = {
                    Icon(
                        painter = painterResource(R.drawable.faq_black),
                        contentDescription = "left icon",
                        modifier = Modifier
                            .size(24.dp)
                    )
                },
                rightIcon = {
                    Icon(
                        painter = painterResource(R.drawable.right),
                        contentDescription = "right icon",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable(
                                onClick = onFaqClick
                            )
                    )
                },
                title = "자주묻는질문",
                onClick = onFaqClick
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
            ){
                Divider(position = DividerPosition.Horizontal)
            }

            ListButton(
                leftIcon = {
                    Icon(
                        painter = painterResource(R.drawable.chat_black),
                        contentDescription = "left icon",
                        modifier = Modifier
                            .size(24.dp)
                    )
                },
                rightIcon = {
                    Icon(
                        painter = painterResource(R.drawable.right),
                        contentDescription = "right icon",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable(
                                onClick = onInquiryClick
                            )
                    )
                },
                title = "문의하기",
                onClick = onInquiryClick
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
            ){
                Divider(position = DividerPosition.Horizontal)
            }

            ListButton(
                leftIcon = {
                    Icon(
                        painter = painterResource(R.drawable.logout),
                        contentDescription = "left icon",
                        modifier = Modifier
                            .size(24.dp)
                    )
                },
                rightIcon = {
                    Icon(
                        painter = painterResource(R.drawable.right),
                        contentDescription = "right icon",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable(
                                onClick = onLogoutClick
                            )
                    )
                },
                title = "로그아웃",
                onClick = onLogoutClick
            )
        }
    }
}
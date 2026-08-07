package com.dangdang.component.image

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.Application.Companion.ExamplePictureUrl
import com.dangdang.R
import com.dangdang.common.utils.bold
import com.dangdang.common.utils.medium
import com.dangdang.common.utils.regular
import com.dangdang.data.enums.AvatarSize
import com.dangdang.data.enums.BackgroundType
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.LightGray
import com.dangdang.ui.theme.White

@Preview
@Composable
fun ProfilePreview(

){
    Profile(
        profileImageUrl = ExamplePictureUrl,
        nickname = "닉네임",
        sinceDays = 120,
        onNextClick = {}
    )
}

@Composable
fun Profile(
    profileImageUrl: String? = null,
    nickname: String,
    sinceDays: Int,
    onNextClick: ()-> Unit
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = White,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = LightGray,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(
            avatarSize = AvatarSize.Large,
            imageUrl = profileImageUrl
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = nickname,
                style = AppTypography.titleLarge.bold,
                color = Black
            )

            Text(
                text = "당당이와 함께한 지 ${sinceDays}일",
                style = AppTypography.labelMedium.regular,
                color = Black
            )
        }

        Icon(
            painter = painterResource(R.mipmap.right),
            contentDescription = "next icon",
            modifier = Modifier
                .size(24.dp)
                .clickable(
                    onClick = onNextClick
                ),
        )
    }
}
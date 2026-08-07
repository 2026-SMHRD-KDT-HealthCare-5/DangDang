package com.dangdang.component.image

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.dangdang.Application.Companion.ExamplePictureUrl
import com.dangdang.R
import com.dangdang.data.enums.AvatarSize
import com.dangdang.ui.theme.SkyBlue

@Preview
@Composable
fun AvatarPreview(

) {
    Column(
       verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Avatar(
            avatarSize = AvatarSize.Large,
            imageUrl = ExamplePictureUrl
        )
        Avatar(
            avatarSize = AvatarSize.Small,
            imageSrc = R.mipmap.lock_icon
        )
        Avatar(
            avatarSize = AvatarSize.XSmall,
            imageUrl = ExamplePictureUrl
        )
    }
}

@Composable
fun Avatar(
    avatarSize: AvatarSize,
    imageUrl: String? = null,
    imageSrc: Int? = null
) {
    Box(
        modifier = Modifier
            .size(avatarSize.backgroundSize)
            .background(
                color = SkyBlue,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ){
        if(imageUrl != null){
            AsyncImage(
                model = imageUrl,
                error = painterResource(id = R.mipmap.mypage_black),
                placeholder = painterResource(id = R.mipmap.mypage_black),
                contentDescription = "아바타",
                modifier = Modifier
                    .size(avatarSize.avatarSize)
                    .clip(CircleShape),
            )
        }

        if(imageSrc != null){
            Image(
                painter = painterResource(imageSrc),
                contentDescription = "아바타",
                modifier = Modifier
                    .size(avatarSize.avatarSize)
                    .clip(CircleShape),
            )
        }

        if(imageUrl == null && imageSrc == null){
            Image(
                painter = painterResource(id = R.mipmap.mypage_black),
                contentDescription = "아바타",
                modifier = Modifier
                    .size(avatarSize.avatarSize)
                    .clip(CircleShape),
            )
        }
    }
}
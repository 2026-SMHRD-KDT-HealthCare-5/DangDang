package com.dangdang.component.button

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.ui.theme.Navy
import coil.compose.AsyncImage
import com.dangdang.R
import com.dangdang.ui.theme.Gray
import com.dangdang.ui.theme.ThinLineDp
import com.dangdang.ui.theme.White

@Preview
@Composable
fun ImageSelectButtonPreview() {
    ImageSelectButton(
        imageUri = null,
        onGalleryClick = {},
        onCameraClick = {}
    )
}

@Composable
fun ImageSelectButton(
    imageUri: Uri?,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(
                color = Navy,
                shape = CircleShape
            )
            .clickable(
                onClick = onGalleryClick
            ),
        contentAlignment = Alignment.Center
    ){
        AsyncImage(
            model = imageUri,
            contentDescription = null,
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape),
            error = painterResource(id = R.drawable.community_black),
            placeholder = painterResource(id = R.drawable.community_black),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ){
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = White,
                        shape = CircleShape
                    )
                    .border(
                        width = ThinLineDp,
                        color = Gray,
                        shape = CircleShape
                    )
                    .clickable(
                        onClick = onCameraClick
                    ),
                contentAlignment = Alignment.Center
            ){
                Icon(
                    painter = painterResource(id = R.drawable.camera),
                    contentDescription = "카메라",
                )
            }
        }
    }
}
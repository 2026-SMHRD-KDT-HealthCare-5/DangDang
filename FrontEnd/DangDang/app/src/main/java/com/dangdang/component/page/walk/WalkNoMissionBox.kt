package com.dangdang.component.page.walk

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.R
import com.dangdang.common.utils.medium
import com.dangdang.common.utils.regular
import com.dangdang.component.button.outlined.PrimaryOutlinedButton
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.DarkGray
import com.dangdang.ui.theme.LightBlack
import com.dangdang.ui.theme.LightWhiteSmoke
import com.dangdang.ui.theme.MediumRoundShape

@Preview
@Composable
fun WalkNoMissionBoxPreview(
){
    WalkNoMissionBox(
        onMissionButtonClick = {}
    )
}

@Composable
fun WalkNoMissionBox(
    onMissionButtonClick: ()->Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(590.dp)
            .padding(
                horizontal = 35.dp
            )
            .background(
                color = LightWhiteSmoke,
                shape = MediumRoundShape
            )
    ) {
        Image(
            painter = painterResource(R.drawable.no_walk_mission),
            contentDescription = "no walk mission",
            modifier = Modifier
                .fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.map_icon),
                contentDescription = "map icon",
                modifier = Modifier
                    .width(105.dp)
                    .height(96.dp)
            )

            Text(
                text = buildAnnotatedString {
                    withStyle(
                        AppTypography.titleLarge.medium
                            .toSpanStyle()
                            .copy(
                                color = Black
                            )
                    ) {
                        append("진행 중인 걷기 미션이 없어요")
                    }
                    withStyle(
                        AppTypography.bodyLarge.medium
                            .toSpanStyle()
                            .copy(
                                color = DarkGray
                            )
                    ) {
                        append("\n\n" +
                                "새로운 걷기 미션에 참여하고\n" +
                                "건강한 습관을 만들어보세요!\n")
                    }
                },
                textAlign = TextAlign.Center
            )

            PrimaryOutlinedButton(
                text = "걷기 미션 참여하기",
                onClick = onMissionButtonClick
            )
        }
    }
}
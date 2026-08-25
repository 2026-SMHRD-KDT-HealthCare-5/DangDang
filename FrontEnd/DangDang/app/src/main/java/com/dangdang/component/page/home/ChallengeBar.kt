package com.dangdang.component.page.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.Application.Companion.ExamplePictureUrl
import com.dangdang.common.utils.medium
import com.dangdang.component.guage.GuageBar
import com.dangdang.component.image.Avatar
import com.dangdang.data.enums.AvatarSize
import com.dangdang.data.enums.GuageBarSize
import com.dangdang.data.model.community.TeamMemberChallengeStatusModel
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.PrimaryBlue
import com.dangdang.ui.theme.White

@Preview
@Composable
fun ChallengeBarPreview(){
    ChallengeBar(
        targetDistance = 60f,
        teamMemberChallengeStatusModel = TeamMemberChallengeStatusModel(
            nickname = "닉네임3",
            totalDistance = 10.56f,
        )
    )
}

@Composable
fun ChallengeBar(
    targetDistance: Float,
    teamMemberChallengeStatusModel: TeamMemberChallengeStatusModel
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = White
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Avatar(
            imageUrl = "",
            avatarSize = AvatarSize.XSmall,
        )

        Text(
            text = teamMemberChallengeStatusModel.nickname,
            style = AppTypography.labelLarge.medium,
            color = Black,
        )

        Box(
            modifier = Modifier
                .weight(1f)
        ){
            GuageBar(
                size = GuageBarSize.Small,
                guageColor = PrimaryBlue,
                current = teamMemberChallengeStatusModel.totalDistance,
                target = targetDistance
            )
        }

        Text(
            text = "${String.format(
                LocalLocale.current.platformLocale,
                "%.2f",
                teamMemberChallengeStatusModel.totalDistance
            )} km",
            style = AppTypography.labelMedium.medium,
            color = Black,
        )
    }
}
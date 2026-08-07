package com.dangdang.component.page.community.teamchallenge

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.Application.Companion.ExamplePictureUrl
import com.dangdang.R
import com.dangdang.common.utils.bold
import com.dangdang.common.utils.medium
import com.dangdang.common.utils.regular
import com.dangdang.component.guage.GuageBar
import com.dangdang.component.image.Avatar
import com.dangdang.data.enums.AvatarSize
import com.dangdang.data.enums.GuageBarSize
import com.dangdang.data.model.community.TeamMemberChallengeStatusModel
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.DarkGray
import com.dangdang.ui.theme.DarkGreen
import com.dangdang.ui.theme.PrimaryBlue
import com.dangdang.ui.theme.White

@Preview
@Composable
fun TeamMemberStatusItemPreview(

){
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TeamMemberStatusItem(
            isGraph = true,
            guageColor = DarkGreen,
            teamMemberChallengeStatus = TeamMemberChallengeStatusModel(
                rank = 1,
                profileImageUrl = ExamplePictureUrl,
                nickname = "닉네임",
                currentDistance = 32.56f,
                targetDistance = 150f
            )
        )

        TeamMemberStatusItem(
            isGraph = false,
            teamMemberChallengeStatus = TeamMemberChallengeStatusModel(
                rank = 3,
                profileImageUrl = ExamplePictureUrl,
                nickname = "닉네임",
                currentDistance = 20.56f,
                targetDistance = 150f
            )
        )
    }
}

@Composable
fun TeamMemberStatusItem(
    isGraph: Boolean,
    guageColor: Color = PrimaryBlue,
    teamMemberChallengeStatus: TeamMemberChallengeStatusModel
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = White
            ),
        verticalAlignment = if(isGraph){
            Alignment.Top
        }else {
            Alignment.CenterVertically
        },
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 10.dp)
        ) {
            Text(
                text = teamMemberChallengeStatus.rank.toString(),
                style = AppTypography.bodyLarge.medium,
                color = Black,
            )
        }

        Avatar(
            avatarSize = if(isGraph) AvatarSize.Small else AvatarSize.XSmall,
            imageUrl = teamMemberChallengeStatus.profileImageUrl,
        )
        
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = teamMemberChallengeStatus.nickname,
                    style = AppTypography.labelLarge.medium,
                    color = Black,
                    modifier = Modifier
                        .weight(1f)
                )

                if(teamMemberChallengeStatus.rank == 1 && isGraph){
                    Image(
                        painter = painterResource(R.mipmap.crown),
                        contentDescription = "왕관 이미지",
                        modifier = Modifier
                            .size(20.dp)
                    )
                }

                Text(
                    text = String.format(
                        LocalLocale.current.platformLocale,
                        "%.2f",
                        teamMemberChallengeStatus.currentDistance),
                    style = AppTypography.labelLarge.medium,
                    color = Black,
                )

                Text(
                    text = "km",
                    style = AppTypography.labelMedium.regular,
                    color = DarkGray,
                )
            }

            if(isGraph){
                GuageBar(
                    size = GuageBarSize.Small,
                    guageColor = guageColor,
                    current = teamMemberChallengeStatus.currentDistance,
                    target = teamMemberChallengeStatus.targetDistance
                )
            }
        }
    }
}
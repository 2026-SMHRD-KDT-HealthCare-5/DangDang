package com.dangdang.component.page.community.ranking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.Application.Companion.ExamplePictureUrl
import com.dangdang.common.utils.medium
import com.dangdang.common.utils.regular
import com.dangdang.component.image.Avatar
import com.dangdang.data.enums.AvatarSize
import com.dangdang.data.model.community.TeamRankingStatusModel
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.DarkGray
import com.dangdang.ui.theme.White

@Preview
@Composable
fun TeamRankingStatusItemPreview(){
    TeamRankingStatusItem(
        teamRankingStatus = TeamRankingStatusModel(
            rank = 2,
            profileImageUrl = ExamplePictureUrl,
            teamName = "팀명2",
            monthlyDistance = 20.56f,
        )
    )
}

@Composable
fun TeamRankingStatusItem(
    teamRankingStatus: TeamRankingStatusModel
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
        Column(
            modifier = Modifier
                .padding(vertical = 10.dp)
        ) {
            Text(
                text = teamRankingStatus.rank.toString(),
                style = AppTypography.bodyLarge.medium,
                color = Black,
            )
        }

        Avatar(
            avatarSize = AvatarSize.XSmall,
            imageUrl = teamRankingStatus.profileImageUrl,
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
                    text = teamRankingStatus.teamName,
                    style = AppTypography.labelLarge.medium,
                    color = Black,
                    modifier = Modifier
                        .weight(1f)
                )

                Text(
                    text = String.format(
                        LocalLocale.current.platformLocale,
                        "%.2f",
                        teamRankingStatus.monthlyDistance),
                    style = AppTypography.labelLarge.medium,
                    color = Black,
                )

                Text(
                    text = "km",
                    style = AppTypography.labelMedium.regular,
                    color = DarkGray,
                )
            }
        }
    }
}
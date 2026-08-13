package com.dangdang.component.page.community.ranking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.dangdang.ui.theme.KakaoYellowOpacity50
import com.dangdang.ui.theme.LightRedOpacity30
import com.dangdang.ui.theme.Navy
import com.dangdang.ui.theme.Orange
import com.dangdang.ui.theme.PrimaryBlueOpacity30
import com.dangdang.ui.theme.Red
import com.dangdang.ui.theme.SmallRoundShape
import com.dangdang.ui.theme.White

@Preview
@Composable
fun RankingIn3ItemPreview(){
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        RankingIn3Item(
            TeamRankingStatusModel(
                rank = 1,
                profileImageUrl = ExamplePictureUrl,
                teamName = "팀명",
                monthlyDistance = 32.56f,
            )
        )

        RankingIn3Item(
            TeamRankingStatusModel(
                rank = 2,
                profileImageUrl = ExamplePictureUrl,
                teamName = "팀명2",
                monthlyDistance = 20.56f,
            )
        )

        RankingIn3Item(
            TeamRankingStatusModel(
                rank = 3,
                profileImageUrl = ExamplePictureUrl,
                teamName = "팀명3",
                monthlyDistance = 10.56f,
            )
        )
    }
}

@Composable
fun RankingIn3Item(
    teamRankingStatus: TeamRankingStatusModel
){
    Column(
        modifier = Modifier
            .background(
                color = White
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Avatar(
            avatarSize = AvatarSize.Small,
            imageUrl = teamRankingStatus.profileImageUrl,
        )

        Box(
            modifier = Modifier
                .width(40.dp)
                .height(44.dp)
                .background(
                    color =
                        when (teamRankingStatus.rank) {
                            1 -> {
                                KakaoYellowOpacity50
                            }
                            2 -> {
                                PrimaryBlueOpacity30
                            }
                            else -> {
                                LightRedOpacity30
                            }
                        },
                    shape = SmallRoundShape
                )
        ){
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ){
                Text(
                    text = teamRankingStatus.rank.toString(),
                    style = AppTypography.titleLarge.regular,
                    color = when (teamRankingStatus.rank) {
                        1 -> {
                            Orange
                        }
                        2 -> {
                            Navy
                        }
                        else -> {
                            Red
                        }
                    },
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 55.dp)
            ){
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .background(
                            color = White,
                            shape = SmallRoundShape
                        )
                )
            }
        }

        Text(
            text = teamRankingStatus.teamName,
            style = AppTypography.labelMedium.medium,
            color = Black,
        )

        Text(
            text = "${String.format(
                LocalLocale.current.platformLocale,
                "%.2f",
                teamRankingStatus.monthlyDistance
            )} km",
            style = AppTypography.labelMedium.medium,
            color = Black,
        )
    }
}
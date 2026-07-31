package com.dangdang.component.page.community.teamchallenge

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.common.utils.bold
import com.dangdang.common.utils.regular
import com.dangdang.data.model.community.TeamInfoModel
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.Navy
import com.dangdang.ui.theme.White
import java.util.Locale
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import com.dangdang.R
import com.dangdang.common.utils.medium
import com.dangdang.component.guage.ThreeGuageBar

@Preview
@Composable
fun TeamChallengeGraphBoxPreview(){
    TeamChallengeGraphBox(
        teamInfo = TeamInfoModel(
            isLeader = false,
            name = "우리팀 5월 걷기 챌린지",
            targetDistance = 150f,
            currentDistance = 20f,
            currentTeamDistance = 30f
        )
    )
}

@Composable
fun TeamChallengeGraphBox(
    teamInfo: TeamInfoModel
){
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Navy,
                shape = RoundedCornerShape(12.dp)
            )
    ){
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 10.dp,
                    vertical = 20.dp
                ),
            contentAlignment = Alignment.CenterEnd
        ){
            Image(
                painter = painterResource(R.mipmap.hi_five),
                contentDescription = "챌린지 이미지",
                modifier = Modifier
                    .height(120.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = teamInfo.name,
                style = AppTypography.bodyLarge.bold,
                color = White,
            )

            Text(
                text = "목표 " +
                        String.format(
                            LocalLocale.current.platformLocale,
                            "%.2f",
                            teamInfo.targetDistance) +
                        " km",
                style = AppTypography.labelLarge.regular,
                color = White,
            )

            Spacer(Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = String.format(
                        LocalLocale.current.platformLocale,
                        "%.2f",
                        teamInfo.currentTeamDistance
                    ),
                    style = AppTypography.bodyLarge.medium,
                    color = White,
                )

                Text(
                    text = "/",
                    style = AppTypography.labelMedium.regular,
                    color = White,
                )

                Text(
                    text = String.format(
                                LocalLocale.current.platformLocale,
                                "%.2f",
                                teamInfo.targetDistance) +
                            " km",
                    style = AppTypography.labelMedium.regular,
                    color = White,
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                ){
                    ThreeGuageBar(
                        current = teamInfo.currentDistance,
                        target = teamInfo.targetDistance,
                        teamCurrent = teamInfo.currentTeamDistance
                    )
                }

                Text(
                    text =
                        "${((teamInfo.currentTeamDistance/teamInfo.targetDistance)
                                *100).toInt()}%",
                    style = AppTypography.labelLarge.medium,
                    color = White,
                )
            }
        }
    }
}
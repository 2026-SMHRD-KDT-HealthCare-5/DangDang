package com.dangdang.component.page.community.teamchallenge

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.dangdang.Application.Companion.ExamplePictureUrl
import com.dangdang.R
import com.dangdang.common.utils.medium
import com.dangdang.component.divider.Divider
import com.dangdang.data.enums.DividerPosition
import com.dangdang.data.model.community.TeamInfoModel
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.Navy
import com.dangdang.ui.theme.White

@Preview
@Composable
fun TeamChallengeInfoBoxPreview(){
    TeamChallengeInfoBox(
        teamInfo = TeamInfoModel(
            isLeader = false,
            currentMemberCount = 4,
            maxMemberCount = 5,
            name = "우리팀 5월 걷기 챌린지",
            targetDistance = 150f,
            currentDistance = 20f,
            currentTeamDistance = 30f,
            profileImageUrl = ExamplePictureUrl,
            introduction = "하루 7천보 이상 함께 걸어요!"
        )
    )
}

@Composable
fun TeamChallengeInfoBox(
    teamInfo: TeamInfoModel
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = White
            )
            .padding(
                start = 5.dp,
                top = 15.dp,
                bottom = 3.dp
            ),
        horizontalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Box(
            modifier = Modifier
                .size(85.dp)
                .background(
                    color = Navy,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = teamInfo.profileImageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape),
                error = painterResource(id = R.mipmap.community_black),
                placeholder = painterResource(id = R.mipmap.community_black),
                contentScale = ContentScale.Crop
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = teamInfo.name,
                style = AppTypography.bodyLarge.medium,
                color = Black,
            )

            Row(
                modifier = Modifier
                    .padding(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(
                    painter = painterResource(R.mipmap.community_blue_outline),
                    contentDescription = null,
                    modifier = Modifier
                        .size(15.dp)
                )

                Text(
                    text = "${teamInfo.currentMemberCount}" +
                            "/${teamInfo.maxMemberCount}명",
                    style = AppTypography.labelSmall.medium,
                    color = Black,
                )

                Divider(
                    position = DividerPosition.Vertical,
                    size = 11.dp
                )

                Text(
                    text = "목표 ${teamInfo.targetDistance}km",
                    style = AppTypography.labelSmall.medium,
                    color = Black,
                )
            }

            Text(
                text = teamInfo.introduction,
                style = AppTypography.labelSmall.medium,
                color = Black,
                modifier = Modifier
                    .padding(start = 14.dp)
            )
        }
    }
}
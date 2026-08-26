package com.dangdang.component.page.community.ranking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.Application.Companion.ExamplePictureUrl
import com.dangdang.component.divider.Divider
import com.dangdang.data.enums.DividerPosition
import com.dangdang.data.model.community.TeamRankingStatusModel
import com.dangdang.ui.theme.Gray
import com.dangdang.ui.theme.MediumRoundShape
import com.dangdang.ui.theme.ThinLineDp
import com.dangdang.ui.theme.White

@Preview
@Composable
fun TeamRankingStatusBoxPreview(){
    TeamRankingStatusBox(
        teamRankingStatusList = listOf(
            TeamRankingStatusModel(
                rank = 1,
                profileImageUrl = ExamplePictureUrl,
                teamName = "닉네임",
                monthlyDistance = 32.56f,
            ),
            TeamRankingStatusModel(
                rank = 2,
                profileImageUrl = ExamplePictureUrl,
                teamName = "닉네임2",
                monthlyDistance = 20.56f,
            ),
            TeamRankingStatusModel(
                rank = 3,
                profileImageUrl = ExamplePictureUrl,
                teamName = "닉네임3",
                monthlyDistance = 10.56f,
            ),
            TeamRankingStatusModel(
                rank = 4,
                profileImageUrl = ExamplePictureUrl,
                teamName = "닉네임4",
                monthlyDistance = 5.56f,
            ),
            TeamRankingStatusModel(
                rank = 5,
                profileImageUrl = ExamplePictureUrl,
                teamName = "닉네임5",
                monthlyDistance = 3.56f,
            )
        )
    )
}

@Composable
fun TeamRankingStatusBox(
    teamRankingStatusList: List<TeamRankingStatusModel>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = White,
                shape = MediumRoundShape
            )
            .border(
                width = ThinLineDp,
                color = Gray,
                shape = MediumRoundShape
            )
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        teamRankingStatusList.forEachIndexed { index, teamRankingStatus ->
            TeamRankingStatusItem(
                teamRankingStatus = teamRankingStatus
            )

            if(index < teamRankingStatusList.size - 1){
                Divider(position = DividerPosition.Horizontal)
            }
        }
    }
}
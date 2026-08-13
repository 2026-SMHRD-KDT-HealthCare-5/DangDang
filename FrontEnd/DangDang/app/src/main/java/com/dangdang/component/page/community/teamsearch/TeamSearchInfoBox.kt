package com.dangdang.component.page.community.teamsearch

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import com.dangdang.Application.Companion.ExamplePictureUrl
import com.dangdang.common.utils.regular
import com.dangdang.component.button.outlined.PrimaryOutlinedButton
import com.dangdang.component.guage.GuageBar
import com.dangdang.component.image.Avatar
import com.dangdang.data.enums.AvatarSize
import com.dangdang.data.enums.GuageBarSize
import com.dangdang.data.model.community.TeamSearchInfoModel
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.Gray
import com.dangdang.ui.theme.MediumRoundShape
import com.dangdang.ui.theme.PrimaryBlue
import com.dangdang.ui.theme.ThinLineDp
import com.dangdang.ui.theme.White

@Preview
@Composable
fun TeamSearchInfoBoxPreview(){
    TeamSearchInfoBox(
        teamSearchInfoModel = TeamSearchInfoModel(
            teamNo = 1,
            profileImageUrl = ExamplePictureUrl,
            teamName = "건강한 습관 만들기",
            memberCount = 3,
            capacity = 5,
            currentDistance = 32.56f,
            targetDistance = 150f,
            teamIntro = "하루 7천보 이상 함께 걸어요!"
        ),
        onJoinClick = {}
    )
}

@Composable
fun TeamSearchInfoBox(
    teamSearchInfoModel: TeamSearchInfoModel,
    onJoinClick: (TeamSearchInfoModel)-> Unit
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(
                color = White,
                shape = MediumRoundShape
            )
            .border(
                width = ThinLineDp,
                color = Gray,
                shape = MediumRoundShape
            )
            .padding(
                vertical = 14.dp,
                horizontal = 10.dp
            ),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Avatar(
            avatarSize = AvatarSize.Small,
            imageUrl = teamSearchInfoModel.profileImageUrl,
        )

        Column(
            modifier = Modifier
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = teamSearchInfoModel.teamName,
                style = AppTypography.labelMedium.regular,
                color = Black,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "${teamSearchInfoModel.memberCount}" +
                            "/" +
                            "${teamSearchInfoModel.capacity}명",
                    style = AppTypography.caption.regular,
                    color = Black,
                )

                Text(
                    text = "|",
                    style = AppTypography.caption.regular,
                    color = Black,
                )

                Text(
                    text = "목표 ${(teamSearchInfoModel.targetDistance).toInt()}km",
                    style = AppTypography.caption.regular,
                    color = Black,
                )
            }

            Text(
                text = teamSearchInfoModel.teamIntro,
                style = AppTypography.caption.regular,
                color = Black,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                ){
                    GuageBar(
                        size = GuageBarSize.Small,
                        guageColor = PrimaryBlue,
                        current = teamSearchInfoModel.currentDistance,
                        target = teamSearchInfoModel.targetDistance
                    )
                }

                Text(
                    text = "${((teamSearchInfoModel.currentDistance
                            /teamSearchInfoModel.targetDistance
                            )*100).toInt()}%",
                    style = AppTypography.caption.regular,
                    color = Black,
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Bottom
        ) {
            PrimaryOutlinedButton(
                text = "가입하기",
                onClick = {
                    onJoinClick(teamSearchInfoModel)
                }
            )
        }
    }
}
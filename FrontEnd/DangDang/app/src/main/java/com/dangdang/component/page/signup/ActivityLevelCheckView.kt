package com.dangdang.component.page.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.common.utils.activityLevelList
import com.dangdang.common.utils.medium
import com.dangdang.component.toggle.RadioSelectBox
import com.dangdang.data.model.user.UserActivityLevelModel
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.White

@Preview
@Composable
fun ActivityLevelCheckViewPreview() {
    ActivityLevelCheckView(
        checkedActivityLevel = activityLevelList[0],
        onCheckedActivityLevelChange = {}
    )
}

@Composable
fun ActivityLevelCheckView(
    checkedActivityLevel: UserActivityLevelModel,
    onCheckedActivityLevelChange: (UserActivityLevelModel) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = White
            )
    ) {
        Box(
            modifier = Modifier
                .padding(vertical = 9.dp)
        ){
            Text(
                text = "평소 활동량을 선택해주세요",
                style = AppTypography.bodyLarge.medium,
                color = Black,
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            activityLevelList.forEach { activityLevel ->
                RadioSelectBox(
                    checked = checkedActivityLevel == activityLevel,
                    onCheckedChange = {
                        onCheckedActivityLevelChange(activityLevel)
                    },
                    titleIconResourceId = activityLevel.titleIconResourceId,
                    title = activityLevel.title,
                    description = activityLevel.description
                )
            }
        }
    }
}
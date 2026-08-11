package com.dangdang.component.dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.dangdang.R
import com.dangdang.common.utils.bold
import com.dangdang.common.utils.medium
import com.dangdang.component.button.outlined.SecondaryOutlinedButton
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.ForestGreen
import com.dangdang.ui.theme.Gray
import com.dangdang.ui.theme.White

@Preview
@Composable
fun WalkMissionCompleteDialogPreview() {
    WalkMissionCompleteDialogContent(
        onButtonClick = {}
    )
}

@Composable
fun WalkMissionCompleteDialog(
    onDismiss: () -> Unit,
    onButtonClick: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        WalkMissionCompleteDialogContent(
            onButtonClick = {
                onDismiss()
                onButtonClick()
            }
        )
    }
}

@Composable
private fun WalkMissionCompleteDialogContent(
    onButtonClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = White,
                    shape = RoundedCornerShape(12.dp)
                )
                .border(
                    width = 1.dp,
                    color = Gray,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(10.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.dangdang_signupcomplete),
                contentDescription = "당당이",
                modifier = Modifier.size(120.dp)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "\uD83C\uDF89 걷기 미션 완료!",
                    style = AppTypography.bodyLarge.medium,
                    color = Black,
                )

                Text(
                    text = "걷기 완료! 수고했어요!\n" +
                            "이제 걷기 후 혈당을\n" +
                            "당당이에게 알려주세요!",
                    style = AppTypography.bodyLarge.medium,
                    color = Black,
                )

                SecondaryOutlinedButton(
                    text = "걷기 후 혈당 알려주기",
                    onClick = onButtonClick
                )
            }
        }
    }
}
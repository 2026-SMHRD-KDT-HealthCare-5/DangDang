package com.dangdang.ui.screens.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.dangdang.Application.Companion.ExamplePictureUrl
import com.dangdang.common.utils.AppPrefs
import com.dangdang.common.utils.mainScreen
import com.dangdang.common.utils.regular
import com.dangdang.common.utils.sendMail
import com.dangdang.component.image.Profile
import com.dangdang.component.navigation.topnavigation.TopNavigation
import com.dangdang.component.page.mypage.MyPageMenu
import com.dangdang.data.model.user.User
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.viewmodel.navigation.MyPageViewModel

@Preview
@Composable
fun MyPageScreenPreview(

){
    MyPageScreenContent(
        user = User(
            id = "1",
            isSignUp = true,
            nickname = "닉네임",
            profileImageUrl = ExamplePictureUrl,
            email = "email@gmail.com",
            sinceDays = 120,
            createdDt = "2026-07-28",
            updatedDt = "2026-07-28",
        ),
        onMyInfoUpdateMove = {},
        isSwitchChecked = true,
        onSwitchCheckChange = {},
        onFaqClick = {},
        onInquiryClick = {},
        onLogoutClick = {}
    )
}

@Composable
fun MyPageScreen(
    myPageViewModel: MyPageViewModel = hiltViewModel(),
    appPrefs: AppPrefs,
    onMyInfoUpdateMove: ()-> Unit,
    onFaqClick: () -> Unit,
){
    val context = LocalContext.current
    val userInfo by myPageViewModel.userInfo.collectAsState()

    val isNotification by appPrefs.notificationFlow.collectAsState()

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                // 권한 허용됨
                myPageViewModel.setNotification(!isNotification)
            }
        }

    MyPageScreenContent(
        user = userInfo,
        onMyInfoUpdateMove = onMyInfoUpdateMove,
        isSwitchChecked = isNotification,
        onSwitchCheckChange = {
            if(!isNotification && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
                //권한 요청
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }else{
                myPageViewModel.setNotification(!isNotification)
            }
        },
        onFaqClick = onFaqClick,
        onInquiryClick = {
            sendMail(context)
        },
        onLogoutClick = {
            myPageViewModel.logout()
        }
    )
}

@Composable
fun MyPageScreenContent(
    user: User?,
    onMyInfoUpdateMove: ()-> Unit,
    isSwitchChecked: Boolean,
    onSwitchCheckChange: () -> Unit,
    onFaqClick: () -> Unit,
    onInquiryClick: () -> Unit,
    onLogoutClick: () -> Unit
){
    Column(
        modifier = Modifier
            .mainScreen()
    ) {
        TopNavigation(
            title = "마이페이지",
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 10.dp,
                    horizontal = 20.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Profile(
                profileImageUrl = user?.profileImageUrl,
                nickname = user?.nickname?: "",
                sinceDays = user?.sinceDays?: 0,
                onNextClick = onMyInfoUpdateMove
            )

            MyPageMenu(
                isSwitchChecked = isSwitchChecked,
                onSwitchCheckChange = onSwitchCheckChange,
                onFaqClick = onFaqClick,
                onInquiryClick = onInquiryClick,
                onLogoutClick = onLogoutClick
            )
        }
    }
}
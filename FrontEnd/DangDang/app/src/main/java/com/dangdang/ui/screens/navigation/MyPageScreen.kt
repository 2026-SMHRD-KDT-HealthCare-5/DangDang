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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.dangdang.Application.Companion.ExamplePictureUrl
import com.dangdang.common.utils.diagnosisGroupList
import com.dangdang.common.utils.mainScreen
import com.dangdang.common.utils.sendMail
import com.dangdang.component.errorview.ErrorView
import com.dangdang.component.image.Profile
import com.dangdang.component.navigation.topnavigation.TopNavigation
import com.dangdang.component.page.mypage.MyPageMenu
import com.dangdang.data.enums.Gender
import com.dangdang.data.enums.LoadingState
import com.dangdang.data.model.user.SignUpForm
import com.dangdang.ui.viewmodel.navigation.MyPageViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit


@Preview
@Composable
fun MyPageScreenPreview(

){
    MyPageScreenContent(
        user = SignUpForm(
            isSocial = true,
            nickname = "닉네임8",
            email = "email@gmail.com",
            password = "",
            passwordCheck = "",
            gender = Gender.male.name,
            birthDate = "1997.05.16",
            height = "170",
            weight = "70",
            hba1c = "12",
            isHemoglobinRecentResultUnknown = false,
            targetGlucose = "180",
            activityLevel = "주 1 ~2회",
            joinedAt = "2026-07-28",
            profileImageUrl = ExamplePictureUrl,
            notificationEnabled = true,
            diagnosisGroup = diagnosisGroupList[0]
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
    onMyInfoUpdateMove: ()-> Unit,
    onFaqClick: () -> Unit,
){
    val context = LocalContext.current

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        myPageViewModel.getUserInfo()
    }

    val userInfo by myPageViewModel.userInfo.collectAsState()
    var isGrantedPermission by remember {
        mutableStateOf(
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            }else{
                true
            }
        )
    }

    val isNotification by remember(
        userInfo,
        isGrantedPermission
    ) {
        derivedStateOf {
            (userInfo.data?.notificationEnabled ?: false)
            && isGrantedPermission
        }
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                isGrantedPermission = true
                // 권한 허용됨
                myPageViewModel.setNotification(
                    context = context,
                    isNotification = true,
                )
            }
        }

    if(userInfo.loadingState == LoadingState.Success){
        MyPageScreenContent(
            user = userInfo.data,
            onMyInfoUpdateMove = onMyInfoUpdateMove,
            isSwitchChecked = isNotification,
            onSwitchCheckChange = {
                if(!isNotification && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
                    //권한 요청
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }else{
                    myPageViewModel.setNotification(
                        context = context,
                        isNotification = !isNotification
                    )
                }
            },
            onFaqClick = onFaqClick,
            onInquiryClick = {
                sendMail(context)
            },
            onLogoutClick = {
                myPageViewModel.logout(context)
            }
        )
    }else{
        ErrorView(
            loadingState = userInfo.loadingState,
            message = "유저 정보 불러오기를 실패했습니다."
        )
    }
}

@Composable
fun MyPageScreenContent(
    user: SignUpForm?,
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
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
            val sinceDays = ChronoUnit.DAYS.between(
                LocalDate.parse(user?.joinedAt, formatter),
                LocalDate.now()
            )
            Profile(
                profileImageUrl = user?.profileImageUrl,
                nickname = user?.nickname?: "",
                sinceDays = sinceDays.toInt(),
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
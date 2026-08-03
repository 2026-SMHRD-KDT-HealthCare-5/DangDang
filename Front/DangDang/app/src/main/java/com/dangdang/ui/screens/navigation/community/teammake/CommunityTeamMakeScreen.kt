package com.dangdang.ui.screens.navigation.community.teammake

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.dangdang.common.utils.createImageUri
import com.dangdang.common.utils.mainScreen
import com.dangdang.common.utils.medium
import com.dangdang.common.utils.regular
import com.dangdang.component.button.ImageSelectButton
import com.dangdang.component.button.PrimaryButton
import com.dangdang.component.navigation.topnavigation.TopNavigation
import com.dangdang.component.text.textbox.TextBox
import com.dangdang.component.text.textfield.TextField
import com.dangdang.data.enums.LayoutSize
import com.dangdang.data.model.community.TeamMakeForm
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.viewmodel.community.CommunityTeamMakeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Preview
@Composable
fun CommunityTeamMakeScreenPreview(){
    CommunityTeamMakeScreenContent(
        onBackClick = {},
        onGalleryClick = {},
        onCameraClick = {},
        teamMakeForm = TeamMakeForm(
            uri = null,
            name = "",
            introduction = "",
            targetDistance = ""
        ),
        onFormChange = {},
        onDoneClick = {}
    )
}

@Composable
fun CommunityTeamMakeScreen(
    communityTeamMakeViewModel: CommunityTeamMakeViewModel = hiltViewModel(),
    navController: NavController,
){
    val context = LocalContext.current
    val activity = context as Activity

    var photoUri by remember {
        mutableStateOf(context.createImageUri())
    }

    var teamMakeForm by remember {
        mutableStateOf(
    TeamMakeForm(
                uri = null,
                name = "",
                introduction = "",
                targetDistance = ""
            )
        )
    }

    val galleryLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            teamMakeForm = teamMakeForm.copy(
                uri = uri
            )
        }

    val cameraLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->
            if (success) {
                teamMakeForm = teamMakeForm.copy(
                    uri = photoUri
                )
            }
        }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                photoUri = context.createImageUri()
                cameraLauncher.launch(photoUri)
            } else {
                Toast
                    .makeText(
                        context,
                        "카메라 권한이 필요합니다.",
                        Toast.LENGTH_SHORT
                    )
                    .show()
            }
        }

    CommunityTeamMakeScreenContent(
        onBackClick = {
            navController.popBackStack()
        },
        onGalleryClick = {
            galleryLauncher.launch("image/*")
        },
        onCameraClick = {
            when {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED -> {
                    photoUri = context.createImageUri()
                    cameraLauncher.launch(photoUri)
                }

                ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.CAMERA
                ) -> {
                    Toast.makeText(
                        context,
                        "카메라 권한을 허용해주세요.",
                        Toast.LENGTH_SHORT
                    ).show()

                    permissionLauncher.launch(
                        Manifest.permission.CAMERA
                    )
                }

                else -> {
                    permissionLauncher.launch(
                        Manifest.permission.CAMERA
                    )
                }
            }
        },
        teamMakeForm = teamMakeForm,
        onFormChange = {
            teamMakeForm = it
        },
        onDoneClick = {
            communityTeamMakeViewModel.makeTeam(
                context = context,
                teamMakeForm = teamMakeForm,
                onMakeSuccess = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("isMakeTeamSuccess", true)
                    navController.popBackStack()
                }
            )
        }
    )
}

@Composable
fun CommunityTeamMakeScreenContent(
    onBackClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit,
    teamMakeForm: TeamMakeForm,
    onFormChange: (TeamMakeForm) -> Unit,
    onDoneClick: () -> Unit,
){
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    val nameRequester = remember { BringIntoViewRequester() }
    val introductionRequester = remember { BringIntoViewRequester() }
    val targetDistanceRequester = remember { BringIntoViewRequester() }

    Column(
        modifier = Modifier
            .mainScreen(),
    ) {
        TopNavigation(
            isBackButton = true,
            onBackClick = onBackClick,
            title = "팀 만들기",
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .imePadding()
                .verticalScroll(scrollState)
                .padding(
                    vertical = 8.dp,
                    horizontal = 20.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ImageSelectButton(
                imageUri = teamMakeForm.uri,
                onGalleryClick = onGalleryClick,
                onCameraClick = onCameraClick
            )

            Text(
                text = "팀 이미지를 설정해주세요",
                style = AppTypography.labelMedium.regular,
                color = Black,
            )

            TextField(
                title = "팀 이름",
                isRequired = true,
                value = teamMakeForm.name,
                onValueChange = {
                    onFormChange(
                        teamMakeForm.copy(
                            name = it
                        )
                    )
                },
                placeholderText = "팀 이름을 입력해주세요 (최대 20자)",
                maxLength = 20,
                sizeType = LayoutSize.FillMaxSize,
                modifier = Modifier
                    .bringIntoViewRequester(nameRequester)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            coroutineScope.launch {
                                delay(300.milliseconds)
                                nameRequester.bringIntoView()
                            }
                        }
                    }
            )

            TextBox(
                title = "팀 소개",
                isRequired = true,
                value = teamMakeForm.introduction,
                onValueChange = {
                    onFormChange(
                        teamMakeForm.copy(
                            introduction = it
                        )
                    )
                },
                placeholderText = "팀을 소개해주세요 (최대 100자)",
                maxLength = 100,
                modifier = Modifier
                    .bringIntoViewRequester(introductionRequester)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            coroutineScope.launch {
                                delay(300.milliseconds)
                                introductionRequester.bringIntoView()
                            }
                        }
                    }
            )

            TextField(
                title = "목표 거리",
                isRequired = true,
                isMaxLengthView = false,
                value = teamMakeForm.targetDistance,
                onValueChange = {
                    onFormChange(
                        teamMakeForm.copy(
                            targetDistance = it
                        )
                    )
                },
                placeholderText = "km 단위로 숫자만 입력해주세요",
                maxLength = 20,
                sizeType = LayoutSize.FillMaxSize,
                keyboardType = KeyboardType.Number,
                modifier = Modifier
                    .bringIntoViewRequester(targetDistanceRequester)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            coroutineScope.launch {
                                delay(300.milliseconds)
                                targetDistanceRequester.bringIntoView()
                            }
                        }
                    }
            )

            Spacer(modifier = Modifier.height(12.dp))

            PrimaryButton(
                text = "팀 만들기",
                enabled =
                    teamMakeForm.name.isNotEmpty()
                            && teamMakeForm.introduction.isNotEmpty()
                            && teamMakeForm.targetDistance.isNotEmpty(),
                onClick = onDoneClick,
                sizeType = LayoutSize.FillMaxSize
            )
        }
    }
}

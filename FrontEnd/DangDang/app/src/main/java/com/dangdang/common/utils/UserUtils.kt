package com.dangdang.common.utils

import com.dangdang.R
import com.dangdang.data.enums.Gender
import com.dangdang.data.model.user.SignUpForm
import com.dangdang.data.model.user.UserActivityLevelModel

//평소 활동량 리스트
val activityLevelList = listOf(
    UserActivityLevelModel(
        titleIconResourceId = R.drawable.no_entry,
        title = "거의 안함",
        description = "운동을 거의 하지 않아요"
    ),
    UserActivityLevelModel(
        titleIconResourceId = R.drawable.exercise_low,
        title = "주 1~2회",
        description = "가벼운 운동을 일주일에 1~2회 해요"
    ),
    UserActivityLevelModel(
        titleIconResourceId = R.drawable.exercise_medium,
        title = "주 3~5회",
        description = "보통 강도의 운동을 일주일에 3~5회 해요"
    ),
    UserActivityLevelModel(
        titleIconResourceId = R.drawable.exercise_high,
        title = "매일",
        description = "거의 매일 꾸준히 운동해요"
    )
)

val diagnosisGroupList = listOf(
    "정상",
    "전당뇨",
    "제2형당뇨"
)

val SignUpDefault = SignUpForm(
    isSocial = false,
    nickname = "",
    email = "",
    password = "",
    passwordCheck = "",
    gender = Gender.male.name,
    birthDate = "",
    height = "",
    weight = "",
    diagnosisGroup = diagnosisGroupList[0],
    hba1c = "",
    isHemoglobinRecentResultUnknown = false,
    targetGlucose = "",
    activityLevel = activityLevelList[0].title,
    profileImageUrl = "",
    joinedAt = "",
    notificationEnabled = false
)
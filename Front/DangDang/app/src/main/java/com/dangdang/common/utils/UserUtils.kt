package com.dangdang.common.utils

import com.dangdang.R
import com.dangdang.data.enums.Gender
import com.dangdang.data.model.user.SignUpForm
import com.dangdang.data.model.user.UserActivityLevelModel

//당뇨 유형 리스트
val DiabetesTypeList = listOf(
    "당뇨 아님(예방 목적)",
    "제1형 당뇨",
    "제2형 당뇨",
    "임신성 당뇨",
    "당뇨 전단계(공복혈당장애)"
)

//평소 활동량 리스트
val activityLevelList = listOf(
    UserActivityLevelModel(
        titleIconResourceId = R.mipmap.no_entry,
        title = "거의 안함",
        description = "운동을 거의 하지 않아요"
    ),
    UserActivityLevelModel(
        titleIconResourceId = R.mipmap.exercise_low,
        title = "주 1 ~2회",
        description = "가벼운 운동을 일주일에 1~2회 해요"
    ),
    UserActivityLevelModel(
        titleIconResourceId = R.mipmap.exercise_medium,
        title = "주 3 ~5회",
        description = "보통 강도의 운동을 일주일에 3~5회 해요"
    ),
    UserActivityLevelModel(
        titleIconResourceId = R.mipmap.exercise_high,
        title = "거의 매일",
        description = "거의 매일 꾸준히 운동해요"
    )
)

val SignUpDefault = SignUpForm(
    isSocial = false,
    nickname = "",
    email = "",
    password = "",
    passwordCheck = "",
    gender = Gender.Male,
    birthday = "",
    height = "",
    weight = "",
    diabetesType = DiabetesTypeList[0],
    hemoglobin = "",
    isHemoglobinRecentResultUnknown = false,
    goalGlucose = "",
    activityLevel = activityLevelList[0].title
)
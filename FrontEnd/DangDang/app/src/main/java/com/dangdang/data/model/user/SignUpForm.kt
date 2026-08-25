package com.dangdang.data.model.user

import androidx.annotation.Keep

@Keep
data class SignUpForm(
    var isSocial: Boolean,
    var profileImageUrl: String?,
    var joinedAt: String,
    var notificationEnabled: Boolean,
    var nickname: String,
    var email: String,
    //비밀번호
    var password: String?,
    //비밀번호 확인
    var passwordCheck: String?,
    //성별
    var gender: String,
    //생일
    var birthDate: String,
    //키
    var height: String,
    //몸무게
    var weight: String,
    //당뇨 유형
    var diagnosisGroup: String?,
    //당화혈색소
    var hba1c: String,
    var isHemoglobinRecentResultUnknown: Boolean,
    //식후 2시간 목표 혈당
    var targetGlucose: String,
    //평소 활동량
    var activityLevel: String
)

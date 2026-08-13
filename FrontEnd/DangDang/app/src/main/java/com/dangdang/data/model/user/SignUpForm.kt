package com.dangdang.data.model.user

import androidx.annotation.Keep
import com.dangdang.data.enums.Gender

@Keep
data class SignUpForm(
    var isSocial: Boolean,
    var profileImageUrl: String,
    var joined_at: String,
    var notification_enabled: Boolean,
    var nickname: String,
    var email: String,
    //비밀번호
    var password: String,
    //비밀번호 확인
    var passwordCheck: String,
    //성별
    var gender: Gender,
    //생일
    var birth_date: String,
    //키
    var height: String,
    //몸무게
    var weight: String,
    //당화혈색소
    var hba1c: String,
    var isHemoglobinRecentResultUnknown: Boolean,
    //식후 2시간 목표 혈당
    var target_glucose: String,
    //평소 활동량
    var activity_level: String
)

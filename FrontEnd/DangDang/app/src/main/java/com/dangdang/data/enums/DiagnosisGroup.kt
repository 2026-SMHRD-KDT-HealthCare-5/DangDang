package com.dangdang.data.enums

import androidx.annotation.Keep

@Keep
enum class DiagnosisGroup(val title: String) {
    Normal("정상"),
    Prediabetes("전당뇨"),
    DiabetesType2("제2형당뇨")
}
package com.dangdang.common.utils

import com.dangdang.data.model.chat.GlucoseFeedbackTemplateModel

val GlucoseFeedbackTemplates = listOf(
    GlucoseFeedbackTemplateModel(
        title = "식전 혈당",
        value = {
            it.beginGlucose
        },
        unit = " mg/dL"
    ),
    GlucoseFeedbackTemplateModel(
        title = "AI 예상 식후 혈당",
        value = {
            it.aiPredictAfterGlucose
        },
        unit = " mg/dL"
    ),
    GlucoseFeedbackTemplateModel(
        title = "걷기 후 혈당",
        value = {
            it.realAfterGlucose
        },
        unit = " mg/dL"
    ),
    GlucoseFeedbackTemplateModel(
        title = "실제 걸음 거리",
        value = {
            it.walkDistance
        },
        unit = "km"
    ),
)

//추천 채팅 질문 타입
const val AnalysisFoodType = "analysis" //음식 분석 & 걷기
const val BeforeMealTipType = "beforeMealTip" //식전 관리 팁
const val TodayWalkTargetType = "todayWalkTarget" //오늘 걷기 목표

//채팅 단계 타입
const val BeforeMealGlucoseInputStage = "beforeMealGlucoseInput" //식전 혈당 입력
const val InputAteFoodStage = "inputAteFood" //먹은 음식 입력
const val InputAteWeightStage = "inputAteWeight" // 먹은 양 입력
const val AnalysisFoodStage = "analysisFood" //음식 분석 결과 확정 이전 단계
const val RecommendWalkDistanceStage = "recommendWalkDistance" //걷기 챌린지 추천 단계
const val AfterWalkGlucoseInputStage = "afterWalkGlucoseInput" //걷기 후 혈당 입력 단계
const val AIFeedbackStage = "aiFeedback" //ai 혈당 피드백

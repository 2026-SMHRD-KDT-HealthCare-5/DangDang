package com.dangdang.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * [각주 FA] GET /api/home 응답입니다. 홈 화면 구성 요소(주간 걷기달성 / 오늘 혈당 추이 /
 * 내 걷기 거리 요약)를 한 번에 반환합니다.
 *
 * [각주] (수정 2026-08-21) 기존 팀 챌린지 현황(teamChallenge) 블록은 홈 화면에서 삭제하고,
 * 개인 걷기 거리 요약(walkingDistance: 오늘/이번달/전체)으로 교체했습니다(사용자 결정).
 * 팀 챌린지 정보는 GET /api/teams/me · GET /api/teams/{teamNo}에서 그대로 조회 가능합니다.
 *
 * @lastModified 2026-08-21
 */
public record HomeResponse(
        List<WeeklyAttendanceDay> weeklyAttendance,
        GlucoseTrend glucoseTrend,
        WalkingDistanceSummary walkingDistance
) {
    /**
     * [각주] status는 그날 끝난 미션이 없거나(미래 요일 포함) null입니다 — DONE/MISSED만
     * 실제 값이고, "NONE"이라는 문자열은 안 씁니다(WeeklyAttendanceStatus 각주 참고).
     */
    public record WeeklyAttendanceDay(
            String day,
            WeeklyAttendanceStatus status
    ) {
    }

    /** targetGlucose는 users.target_glucose, points는 오늘 하루치(PRE/POST_WALK)만 시간순. */
    public record GlucoseTrend(
            Integer targetGlucose,
            List<GlucosePoint> points
    ) {
    }

    /** time은 "HH:mm" 문자열, type은 "PRE" 또는 "POST_WALK". */
    public record GlucosePoint(
            String time,
            Integer glucose,
            String type
    ) {
    }

    /**
     * [각주] (추가 2026-08-21) 개인 걷기 거리 요약입니다. 단위는 km(팀 쪽 거리 표기와 통일).
     * COMPLETE/PARTIAL 미션만 집계합니다(EXPIRED 제외 — walk-missions 각주 참고).
     */
    public record WalkingDistanceSummary(
            BigDecimal todayDistance,
            BigDecimal monthlyDistance,
            BigDecimal totalDistance
    ) {
    }
}

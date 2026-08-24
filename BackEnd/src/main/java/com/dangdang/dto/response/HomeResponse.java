package com.dangdang.dto.response;

import java.util.List;

/**
 * [각주 FA] GET /api/home 응답입니다. 홈 화면 구성 요소(주간 걷기달성 / 오늘 혈당 추이 /
 * 팀 챌린지 요약)를 한 번에 내려줍니다(노션 "홈 화면" 명세).
 *
 * @lastModified 2026-08-21
 */
public record HomeResponse(
        List<WeeklyAttendanceDay> weeklyAttendance,
        GlucoseTrend glucoseTrend,
        TeamChallengeSummaryResponse teamChallenge
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
}

package com.dangdang.dto.response;

/**
 * [각주 FB] GET /api/home 의 weeklyAttendance[].status 값입니다.
 * 노션 명세엔 DONE/MISSED/NONE 세 값이 있었는데, "끝난 미션 없음/미래 요일"인 경우까지
 * 굳이 NONE이라는 문자열을 만들 필요 없이 그냥 status를 null로 두기로 결정했습니다
 * (일주일치 데이터를 그대로 불러와서 없으면 null인 게 자연스럽다는 판단, 노션도 같이 수정함).
 * 그래서 이 enum은 DONE/MISSED 딱 2개만 있습니다.
 *
 * @lastModified 2026-08-21
 */
public enum WeeklyAttendanceStatus {
    DONE,
    MISSED
}

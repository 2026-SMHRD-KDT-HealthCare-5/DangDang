package com.dangdang.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * [각주 FC] GET /api/home 의 teamChallenge 블록입니다. 팀 챌린지 현황 조회(GET /api/teams/{no})와
 * 거의 같은 계산인데, topMembers는 전체가 아니라 상위 3명만 자릅니다(홈 화면 요약용).
 * 팀 가입 안 한 유저는 이 값 자체가 null입니다 — TeamService.getTeamChallengeSummary() 참고.
 *
 * @lastModified 2026-08-21
 */
public record TeamChallengeSummaryResponse(
        Integer teamNo,
        String teamName,
        String challengeMonth,
        BigDecimal targetDistance,
        BigDecimal currentDistance,
        int progressRate,
        List<TopMember> topMembers
) {
    public record TopMember(
            String nickname,
            BigDecimal totalDistance
    ) {
    }
}

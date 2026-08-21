package com.dangdang.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * [각주 EH] GET /api/teams/{team_no} (팀 챌린지 현황 조회) 응답입니다.
 * GET /api/teams/me (내 팀 조회 — 노션에 없지만 프론트가 team_no를 알아낼 방법이 없어서
 * 추가한 API, 아래 TeamController 각주 참고)에서도 같은 DTO를 그대로 재사용합니다.
 *
 * currentDistance/progressRate = "이번 달"(월간 리셋) 기준, members[].totalDistance =
 * "가입 이후 누적" 기준 — 서로 집계 기간이 달라서 노션 명세도 화면에 라벨을 따로 표기하라고
 * 되어 있습니다. 단위는 전부 km입니다.
 *
 * @lastModified 2026-08-20
 */
public record TeamDetailResponse(
        Integer teamNo,
        String teamName,
        String teamIntro,
        String challengeMonth,
        BigDecimal targetDistance,
        BigDecimal currentDistance,
        int progressRate,
        long memberCount,
        int capacity,
        boolean isCreator,
        List<MemberRanking> members
) {
    /** 팀원 1명의 "가입 이후 누적거리" 랭킹 항목입니다. */
    public record MemberRanking(
            String nickname,
            BigDecimal totalDistance
    ) {
    }
}

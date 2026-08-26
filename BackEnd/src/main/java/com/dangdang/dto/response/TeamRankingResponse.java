package com.dangdang.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * [각주 EI] GET /api/rankings/teams?month= 응답입니다 (노션 "팀 월간 랭킹 조회" 명세).
 * 개인 랭킹(GET /api/rankings/users)을 대체합니다 — 개인 랭킹은 더 이상 제공하지 않습니다.
 * 정렬 기준은 monthlyDistance 절대값 내림차순입니다(진행률 기준 아님 — 목표를 낮게 잡은
 * 소규모 팀이 유리해지는 걸 막기 위함, 노션 명세 그대로).
 *
 * @lastModified 2026-08-20
 */
public record TeamRankingResponse(
        String month,
        List<Ranking> rankings,
        Integer myTeamRank
) {
    public record Ranking(
            int rank,
            Integer teamNo,
            String teamName,
            long memberCount,
            BigDecimal monthlyDistance
    ) {
    }
}

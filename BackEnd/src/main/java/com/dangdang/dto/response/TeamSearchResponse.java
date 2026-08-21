package com.dangdang.dto.response;

import java.math.BigDecimal;

/**
 * [각주 EG] GET /api/teams?keyword= 응답의 목록 항목 1개입니다 (노션 "팀 검색/목록 조회" 명세).
 * currentDistance는 "이번 달" 팀 누적, progressRate는 currentDistance/targetDistance*100을
 * 0~100 범위로 자른 값입니다(100 초과 시 100 고정). 단위는 전부 km입니다.
 *
 * @lastModified 2026-08-20
 */
public record TeamSearchResponse(
        Integer teamNo,
        String teamName,
        String teamIntro,
        long memberCount,
        int capacity,
        BigDecimal targetDistance,
        BigDecimal currentDistance,
        int progressRate
) {
}

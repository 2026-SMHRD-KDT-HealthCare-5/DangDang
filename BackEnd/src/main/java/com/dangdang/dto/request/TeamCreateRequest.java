package com.dangdang.dto.request;

import java.math.BigDecimal;

/**
 * [각주 EE] POST /api/teams 요청 바디입니다 (노션 "팀 만들기" 명세).
 * targetDistance 단위는 km입니다(팀 쪽 거리 단위는 전부 km — Team.java 각주 참고).
 * teamName 20자/teamIntro 100자 제한은 TeamService에서 검증합니다.
 *
 * @lastModified 2026-08-20
 */
public record TeamCreateRequest(
        String teamName,
        String teamIntro,
        BigDecimal targetDistance
) {
}

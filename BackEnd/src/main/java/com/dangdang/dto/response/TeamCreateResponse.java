package com.dangdang.dto.response;

/**
 * [각주 EF] POST /api/teams 응답입니다. 201 Created + team_no (노션 명세 그대로).
 *
 * @lastModified 2026-08-20
 */
public record TeamCreateResponse(
        Integer teamNo
) {
}

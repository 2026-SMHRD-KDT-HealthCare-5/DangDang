package com.dangdang.controller;

import com.dangdang.dto.response.TeamRankingResponse;
import com.dangdang.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * [각주 EZ] 랭킹 API. 팀(teams)과 카테고리가 달라(노션 "카테고리: rankings") TeamController와
 * 분리했습니다. 개인 랭킹(GET /api/rankings/users)은 폐기되어 여기 없습니다.
 *
 * @lastModified 2026-08-20
 */
@RestController
@RequestMapping("/api/rankings")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    /** [각주 FA] GET /api/rankings/teams?month=YYYY-MM — 팀 월간 랭킹 조회. month 생략 시 이번 달. */
    @GetMapping("/teams")
    public ResponseEntity<TeamRankingResponse> getTeamMonthlyRanking(
            Authentication authentication,
            @RequestParam(required = false) String month
    ) {
        Integer userNo = (Integer) authentication.getPrincipal();
        return ResponseEntity.ok(rankingService.getTeamMonthlyRanking(userNo, month));
    }
}

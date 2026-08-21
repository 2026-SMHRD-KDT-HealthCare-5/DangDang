package com.dangdang.controller;

import com.dangdang.dto.request.TeamCreateRequest;
import com.dangdang.dto.response.TeamCreateResponse;
import com.dangdang.dto.response.TeamDetailResponse;
import com.dangdang.dto.response.TeamSearchResponse;
import com.dangdang.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * [각주 ES] 팀(Team) 생성/검색/가입/탈퇴/상세조회 API. 노션 "엔드포인트" DB의 5개 팀 관련
 * 페이지(팀 만들기/팀 검색·가입/팀 가입하기/팀 나가기/팀 챌린지 현황 조회) 명세를 그대로
 * 구현했고, GET /me는 노션에 없지만 프론트 화면설계서(커뮤니티 메인/팀챌린지 탭)가 team_no를
 * 미리 알고 있다는 전제로 GET /api/teams/{no}만 적어놔서, "내 team_no를 어떻게 알아내는지"
 * 빠진 부분을 채우려고 추가했습니다(TeamService.getMyTeam() 각주 참고).
 *
 * @lastModified 2026-08-20
 */
@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    /** [각주 ET] POST /api/teams — 팀 만들기. 성공 시 201 Created + team_no. */
    @PostMapping
    public ResponseEntity<TeamCreateResponse> createTeam(
            Authentication authentication,
            @RequestBody TeamCreateRequest request
    ) {
        Integer userNo = (Integer) authentication.getPrincipal();
        TeamCreateResponse response = teamService.createTeam(userNo, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** [각주 EU] GET /api/teams?keyword= — 팀 검색/목록 조회. keyword 생략 시 전체 목록. */
    @GetMapping
    public ResponseEntity<List<TeamSearchResponse>> searchTeams(
            @RequestParam(required = false) String keyword
    ) {
        return ResponseEntity.ok(teamService.searchTeams(keyword));
    }

    /**
     * [각주 EV] GET /api/teams/me — 내 팀 조회(비공식 추가 API). 가입한 팀이 있으면
     * 200 + 팀 챌린지 현황과 동일한 본문, 없으면 204 No Content입니다(walk-missions의
     * GET /active와 동일한 패턴).
     */
    @GetMapping("/me")
    public ResponseEntity<TeamDetailResponse> getMyTeam(Authentication authentication) {
        Integer userNo = (Integer) authentication.getPrincipal();
        TeamDetailResponse response = teamService.getMyTeam(userNo);
        return (response != null) ? ResponseEntity.ok(response) : ResponseEntity.noContent().build();
    }

    /** [각주 EW] GET /api/teams/{team_no} — 팀 챌린지 현황 조회. */
    @GetMapping("/{teamNo}")
    public ResponseEntity<TeamDetailResponse> getTeamDetail(
            Authentication authentication,
            @PathVariable Integer teamNo
    ) {
        Integer userNo = (Integer) authentication.getPrincipal();
        return ResponseEntity.ok(teamService.getTeamDetail(userNo, teamNo));
    }

    /** [각주 EX] POST /api/teams/{team_no}/join — 팀 가입하기. */
    @PostMapping("/{teamNo}/join")
    public ResponseEntity<Void> joinTeam(
            Authentication authentication,
            @PathVariable Integer teamNo
    ) {
        Integer userNo = (Integer) authentication.getPrincipal();
        teamService.joinTeam(userNo, teamNo);
        return ResponseEntity.ok().build();
    }

    /** [각주 EY] DELETE /api/teams/{team_no}/members/me — 팀 나가기. */
    @DeleteMapping("/{teamNo}/members/me")
    public ResponseEntity<Void> leaveTeam(
            Authentication authentication,
            @PathVariable Integer teamNo
    ) {
        Integer userNo = (Integer) authentication.getPrincipal();
        teamService.leaveTeam(userNo, teamNo);
        return ResponseEntity.noContent().build();
    }
}

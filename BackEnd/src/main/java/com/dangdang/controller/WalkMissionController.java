package com.dangdang.controller;

import com.dangdang.dto.request.ExpireMissionRequest;
import com.dangdang.dto.request.TrackRequest;
import com.dangdang.dto.response.ActiveMissionResponse;
import com.dangdang.dto.response.EndMissionResponse;
import com.dangdang.dto.response.ExpireMissionResponse;
import com.dangdang.dto.response.StartMissionResponse;
import com.dangdang.dto.response.TrackResponse;
import com.dangdang.service.WalkMissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * [각주 CD] 걷기 미션(WalkMission) 조회/만료 관련 API. 미션 "생성"은 IntakeLogController의
 * confirm()(최종 확정)에서 자동으로 일어나므로 여기엔 없습니다.
 *
 * @lastModified 2026-08-18
 */
@RestController
@RequestMapping("/api/walk-missions")
@RequiredArgsConstructor
public class WalkMissionController {

    private final WalkMissionService walkMissionService;

    /**
     * [각주 CE] 앱 콜드 스타트(껐다 다시 켬) 시 호출 — 걷기 화면을 복구해야 하는지 판단합니다.
     * 활성 미션(READY/IN_PROGRESS)은 uq_active_mission 인덱스로 유저당 최대 1개라
     * 있으면 200 + 내용, 없으면 204(본문 없음)를 돌려줍니다.
     */
    @GetMapping("/active")
    public ResponseEntity<ActiveMissionResponse> getActiveMission(Authentication authentication) {
        Integer userNo = (Integer) authentication.getPrincipal();
        ActiveMissionResponse response = walkMissionService.getActiveMission(userNo);
        return (response != null) ? ResponseEntity.ok(response) : ResponseEntity.noContent().build();
    }

    /**
     * [각주 CF] 프론트가 INACTIVE(30분 미활동 감지) 또는 CANCELLED(콜드스타트 시 로컬
     * 진행상태 없음) 사유로 미션을 직접 종료시킬 때 호출합니다. IN_PROGRESS 상태의
     * 미션에만 쓸 수 있습니다 — READY/이미 끝난 미션은 대상이 아닙니다.
     */
    @PostMapping("/{missionNo}/expire")
    public ResponseEntity<ExpireMissionResponse> expireMission(
            Authentication authentication,
            @PathVariable Integer missionNo,
            @RequestBody ExpireMissionRequest request
    ) {
        Integer userNo = (Integer) authentication.getPrincipal();
        ExpireMissionResponse response = walkMissionService.expireMission(userNo, missionNo, request);
        return ResponseEntity.ok(response);
    }

    /**
     * [각주 CY] "걷기 시작" 버튼 — 챗봇의 MISSION_CARD에서 이 API를 호출하면 걷기 화면으로
     * 넘어갑니다. READY 상태 미션에서만 가능합니다.
     *
     * @lastModified 2026-08-19
     */
    @PostMapping("/{missionNo}/start")
    public ResponseEntity<StartMissionResponse> startMission(
            Authentication authentication,
            @PathVariable Integer missionNo
    ) {
        Integer userNo = (Integer) authentication.getPrincipal();
        StartMissionResponse response = walkMissionService.startMission(userNo, missionNo);
        return ResponseEntity.ok(response);
    }

    /**
     * [각주 CZ] 걷는 동안 프론트가 30초마다 호출하는 위치 폴링입니다. IN_PROGRESS 상태
     * 미션에서만 가능합니다.
     *
     * @lastModified 2026-08-19
     */
    @PostMapping("/{missionNo}/track")
    public ResponseEntity<TrackResponse> trackMission(
            Authentication authentication,
            @PathVariable Integer missionNo,
            @RequestBody TrackRequest request
    ) {
        Integer userNo = (Integer) authentication.getPrincipal();
        TrackResponse response = walkMissionService.trackMission(userNo, missionNo, request);
        return ResponseEntity.ok(response);
    }

    /**
     * [각주 DA] "걷기 종료" 버튼 — 사용자가 직접 종료합니다. 목표 거리 도달 여부에 따라
     * COMPLETE/PARTIAL로 저장되고, 챗봇에 걷기 후 혈당 입력 카드가 함께 생성됩니다.
     *
     * @lastModified 2026-08-19
     */
    @PostMapping("/{missionNo}/end")
    public ResponseEntity<EndMissionResponse> endMission(
            Authentication authentication,
            @PathVariable Integer missionNo
    ) {
        Integer userNo = (Integer) authentication.getPrincipal();
        EndMissionResponse response = walkMissionService.endMission(userNo, missionNo);
        return ResponseEntity.ok(response);
    }
}

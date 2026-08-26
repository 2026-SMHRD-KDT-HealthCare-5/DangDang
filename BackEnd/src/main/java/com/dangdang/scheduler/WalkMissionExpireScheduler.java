package com.dangdang.scheduler;

import com.dangdang.service.WalkMissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * [각주 CG] 걷기 미션 자동 만료 배치입니다. 매분 0초에 한 번씩 실행됩니다.
 * "정상 경로"는 프론트가 스스로 감지해서 POST /{mission_no}/expire를 호출하는 것이고,
 * 이 배치는 프론트가 그 처리를 놓쳤을 때(앱을 아예 다시 안 켠 경우 등)를 대비한 백업입니다
 * (노션 "걷기 자동 종료" 명세: "서버는 앱의 생사를 직접 알 수 없고 폴링 수신 여부로만 판정한다").
 *
 * @Scheduled(cron = "0 * * * * *") : 앞에서부터 "초 분 시 일 월 요일" 순서입니다.
 * "0 * * * * *" = 초가 0일 때(=매 분 정각) 실행하라는 뜻입니다.
 *
 * @lastModified 2026-08-18
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WalkMissionExpireScheduler {

    private final WalkMissionService walkMissionService;

    @Scheduled(cron = "0 * * * * *")
    public void expireStaleMissions() {
        try {
            walkMissionService.expireStaleMissionsBatch();
        } catch (Exception e) {
            // [각주] 배치 한 번 실패했다고 서버 전체가 죽으면 안 되므로, 로그만 남기고 다음 분에 재시도합니다.
            log.error("걷기 미션 배치 만료 처리 실패: {}", e.getMessage(), e);
        }
    }
}

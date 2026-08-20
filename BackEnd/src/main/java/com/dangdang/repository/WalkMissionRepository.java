package com.dangdang.repository;

import com.dangdang.entity.WalkMission;
import com.dangdang.entity.WalkMissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * [각주 BB] walk_mission 테이블 CRUD.
 *
 * findFirstByUserNoAndStatusIn : "이 유저의 활성 미션(READY/IN_PROGRESS)이 있는지" 조회합니다.
 * DB에 uq_active_mission(부분 유니크 인덱스)이 걸려 있어서 항상 0건 또는 1건만 나옵니다 —
 * 그래서 findFirst(Optional 1건)로 받아도 안전합니다.
 *
 * [각주 BY] findByStatusAndCreatedAtBefore / findByStatusAndLastTrackedAtBefore는
 * WalkMissionExpireScheduler(매분 배치)가 씁니다. "Before"는 SQL로 치면
 * "그 컬럼 < 기준시각"인데, lastTrackedAt이 NULL인 행은 비교 자체가 성립하지 않아
 * (SQL에서 NULL과의 비교는 항상 알 수 없음/거짓) 자동으로 결과에서 빠집니다 — 그래서
 * 아직 한 번도 트래킹 안 된 IN_PROGRESS 미션까지 잘못 만료시킬 걱정은 안 해도 됩니다.
 *
 * @lastModified 2026-08-18
 */
public interface WalkMissionRepository extends JpaRepository<WalkMission, Integer> {
    Optional<WalkMission> findFirstByUserNoAndStatusIn(Integer userNo, List<WalkMissionStatus> statuses);

    List<WalkMission> findByStatusAndCreatedAtBefore(WalkMissionStatus status, LocalDateTime cutoff);

    List<WalkMission> findByStatusAndLastTrackedAtBefore(WalkMissionStatus status, LocalDateTime cutoff);
}

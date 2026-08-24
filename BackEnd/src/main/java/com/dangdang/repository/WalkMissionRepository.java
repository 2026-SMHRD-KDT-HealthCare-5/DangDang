package com.dangdang.repository;

import com.dangdang.entity.WalkMission;
import com.dangdang.entity.WalkMissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
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

    // [각주] (추가 2026-08-21) GET /api/home 의 weeklyAttendance/glucoseTrend(POST_WALK) 계산에 씁니다.
    // end_time이 채워진 미션(=COMPLETE/PARTIAL/EXPIRED로 끝난 것)만 걸립니다 — READY/IN_PROGRESS는
    // end_time이 NULL이라 자동으로 빠집니다.
    List<WalkMission> findByUserNoAndEndTimeBetween(Integer userNo, LocalDateTime start, LocalDateTime end);

    /**
     * [각주] (추가 2026-08-21) GET /api/home 의 "내 걷기 거리" 블록(오늘/이번달)에 씁니다.
     * 팀 실적 집계랑 똑같이 COMPLETE/PARTIAL만 포함합니다(EXPIRED는 중간에 취소/타임아웃/미활동으로
     * 끊긴 거라 "걸은 성과"로 안 침, walk-missions 쪽 CANCELLED 정책과 동일한 기준).
     * 단위는 m — 서비스 계층에서 km로 변환합니다.
     */
    @Query("SELECT COALESCE(SUM(w.actualDistance), 0) FROM WalkMission w " +
            "WHERE w.userNo = :userNo AND w.status IN :statuses " +
            "AND w.endTime >= :start AND w.endTime < :end")
    BigDecimal sumDistanceMByUserAndEndTimeBetween(@Param("userNo") Integer userNo,
                                                     @Param("statuses") List<WalkMissionStatus> statuses,
                                                     @Param("start") LocalDateTime start,
                                                     @Param("end") LocalDateTime end);

    /** [각주] 위와 같은 기준, 기간 제한 없이 전체 누적("총 거리")입니다. */
    @Query("SELECT COALESCE(SUM(w.actualDistance), 0) FROM WalkMission w " +
            "WHERE w.userNo = :userNo AND w.status IN :statuses")
    BigDecimal sumTotalDistanceMByUser(@Param("userNo") Integer userNo,
                                        @Param("statuses") List<WalkMissionStatus> statuses);
}

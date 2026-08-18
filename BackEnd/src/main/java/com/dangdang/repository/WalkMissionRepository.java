package com.dangdang.repository;

import com.dangdang.entity.WalkMission;
import com.dangdang.entity.WalkMissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * [각주 BB] walk_mission 테이블 CRUD.
 *
 * findFirstByUserNoAndStatusIn : "이 유저의 활성 미션(READY/IN_PROGRESS)이 있는지" 조회합니다.
 * DB에 uq_active_mission(부분 유니크 인덱스)이 걸려 있어서 항상 0건 또는 1건만 나옵니다 —
 * 그래서 findFirst(Optional 1건)로 받아도 안전합니다.
 *
 * @lastModified 2026-08-18
 */
public interface WalkMissionRepository extends JpaRepository<WalkMission, Integer> {
    Optional<WalkMission> findFirstByUserNoAndStatusIn(Integer userNo, List<WalkMissionStatus> statuses);
}

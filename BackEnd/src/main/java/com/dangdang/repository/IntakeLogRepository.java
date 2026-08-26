package com.dangdang.repository;

import com.dangdang.entity.IntakeLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * [각주 AZ] intake_log 테이블 CRUD.
 *
 * @lastModified 2026-08-21
 */
public interface IntakeLogRepository extends JpaRepository<IntakeLog, Integer> {

    // [각주] (추가 2026-08-21) GET /api/home 의 glucoseTrend(PRE 포인트) 계산에 씁니다.
    List<IntakeLog> findByUserNoAndIntakeAtBetween(Integer userNo, LocalDateTime start, LocalDateTime end);
}

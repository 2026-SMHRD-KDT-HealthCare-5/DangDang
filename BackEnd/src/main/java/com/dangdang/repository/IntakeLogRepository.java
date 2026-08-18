package com.dangdang.repository;

import com.dangdang.entity.IntakeLog;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * [각주 AZ] intake_log 테이블 CRUD. 지금은 confirmIntake()에서 save()만 씁니다.
 *
 * @lastModified 2026-08-18
 */
public interface IntakeLogRepository extends JpaRepository<IntakeLog, Integer> {
}

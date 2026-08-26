package com.dangdang.repository;

import com.dangdang.entity.CustomFood;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * [각주 BA] custom_food 테이블 CRUD. 지금은 confirmIntake()에서 save()만 씁니다.
 *
 * @lastModified 2026-08-18
 */
public interface CustomFoodRepository extends JpaRepository<CustomFood, Integer> {
}

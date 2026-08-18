package com.dangdang.repository;

import com.dangdang.entity.FoodInfo;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * [각주 BM] food_info(식약처 공공 영양데이터, 읽기 전용) 조회 전용입니다.
 * confirmIntake()가 foodNo로 1인분 기준 영양성분을 다시 조회할 때 findById()만 씁니다.
 *
 * @lastModified 2026-08-18
 */
public interface FoodInfoRepository extends JpaRepository<FoodInfo, Integer> {
}

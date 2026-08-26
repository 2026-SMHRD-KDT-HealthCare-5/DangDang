package com.dangdang.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * [각주 BL] food_info 테이블과 매핑되는 엔티티입니다 — 식약처 공공 영양데이터로, 읽기 전용입니다
 * (Spring이 이 테이블에 INSERT/UPDATE를 하지 않습니다. @Builder를 일부러 안 뒀습니다).
 *
 * confirmIntake()가 "맞아요"(식약처 매칭 확정) 경로에서, 프론트가 보낸 foodNo로 1인분 기준
 * 영양성분을 다시 조회할 때 씁니다 — recognize 응답의 nutrition을 프론트가 다시 보내게 하지
 * 않고, 서버가 DB 원본을 신뢰할 수 있는 값으로 재조회합니다.
 *
 * @lastModified 2026-08-18
 */
@Entity
@Table(name = "food_info")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FoodInfo {

    @Id
    @Column(name = "food_no")
    private Integer foodNo;

    @Column(name = "food_name", nullable = false, length = 100)
    private String foodName;

    @Column(name = "calorie", nullable = false, precision = 6, scale = 2)
    private BigDecimal calorie;

    @Column(name = "carb", nullable = false, precision = 6, scale = 2)
    private BigDecimal carb;

    @Column(name = "sugar", nullable = false, precision = 6, scale = 2)
    private BigDecimal sugar;

    @Column(name = "protein", precision = 6, scale = 2)
    private BigDecimal protein;

    @Column(name = "fat", precision = 6, scale = 2)
    private BigDecimal fat;

    @Column(name = "fiber", precision = 6, scale = 2)
    private BigDecimal fiber;

    @Column(name = "serving_size")
    private Integer servingSize;
}

package com.dangdang.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * [각주 AV] custom_food 테이블과 매핑되는 엔티티입니다.
 * 식약처 DB(food_info)에서 못 찾은 음식 — "틀려요, AI로 분석하기"(AI추정) 또는
 * "직접입력하기"(사용자직접) 둘 중 하나로 확보된 영양성분을 저장합니다.
 *
 * [각주 AW] (원래 원칙, 2026-08-18) 이 엔티티는 원래 "맞아요"(최종 확정) 누르는 시점에만
 * 저장하도록 설계했습니다 — AI 재분석/직접입력 단계 자체는 DB에 아무것도 안 쓰고, 사용자가
 * 결과가 마음에 안 들면 검색어 다시 입력/재분석/직접입력을 몇 번이든 반복할 수 있게(취소되면
 * 미저장) 하기 위함이었습니다.
 *
 * [각주] (수정 2026-08-25, 프론트 요청) "AI로 분석하기"(reanalyze) 경로는 이 원칙이 깨졌습니다.
 * `RecognizeProxyService.reanalyze()`가 이제 매 호출마다 바로 이 테이블에 저장하고 PK를
 * 응답에 실어줍니다(프론트가 customFoodNo를 참조용으로 미리 받아야 해서). 그 결과 사용자가
 * "틀려요, 다시 분석"을 여러 번 누르면 그때마다 행이 하나씩 쌓이고, 실제 확정은 최대 1건뿐이라
 * 나머지는 고아 행으로 남습니다. **"직접입력하기"(USER_INPUT) 경로는 여전히 기존 원칙 그대로**
 * — confirmIntake() 시점에만 저장됩니다(직접입력은 재시도 미리보기 자체가 없어서).
 *
 * @lastModified 2026-08-25
 */
@Entity
@Table(name = "custom_food")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomFood {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "custom_food_no")
    private Integer customFoodNo;

    @Column(name = "user_no", nullable = false)
    private Integer userNo;

    @Column(name = "food_name", nullable = false, length = 100)
    private String foodName;

    @Column(name = "serving_size")
    private Integer servingSize;

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

    @Column(name = "calorie", nullable = false, precision = 6, scale = 2)
    private BigDecimal calorie;

    // [각주] "AI추정" / "사용자입력" 문자열 그대로 저장합니다 (CustomFoodSource enum 참고).
    // DiagnosisGroup과 같은 이유로 @Enumerated(STRING)을 안 씁니다 — 그러면 "AI_ESTIMATE"처럼
    // enum 상수 이름(영문)이 그대로 저장돼서 varchar(10)을 넘기거나 명세와 다른 문자열이 저장됩니다.
    @Column(name = "source", nullable = false, length = 10)
    private String source;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private CustomFood(Integer userNo, String foodName, Integer servingSize,
                        BigDecimal carb, BigDecimal sugar, BigDecimal protein,
                        BigDecimal fat, BigDecimal fiber, BigDecimal calorie,
                        String source) {
        this.userNo = userNo;
        this.foodName = foodName;
        this.servingSize = servingSize;
        this.carb = carb;
        this.sugar = sugar;
        this.protein = protein;
        this.fat = fat;
        this.fiber = fiber;
        this.calorie = calorie;
        this.source = source;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}

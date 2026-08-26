package com.dangdang.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * [각주 AY] intake_log 테이블과 매핑되는 엔티티입니다. "맞아요"(최종 확정) 시점에만 저장됩니다.
 *
 * DB에 아래 CHECK 제약이 걸려 있습니다 — foodNo/customFoodNo 중 정확히 하나만 값이 있어야 합니다.
 * CONSTRAINT chk_food_reference CHECK (
 *     (food_no IS NOT NULL AND custom_food_no IS NULL) OR
 *     (food_no IS NULL     AND custom_food_no IS NOT NULL)
 * )
 * 이 검증은 DB가 최종적으로 막아주지만, 에러 메시지를 사용자 친화적으로 주기 위해
 * IntakeLogService.confirmIntake()에서 저장 전에 자바 코드로도 먼저 검증합니다.
 *
 * @lastModified 2026-08-18
 */
@Entity
@Table(name = "intake_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IntakeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_no")
    private Integer logNo;

    @Column(name = "user_no", nullable = false)
    private Integer userNo;

    // 식약처 매칭("맞아요") — foodNo/customFoodNo 중 하나만 채워집니다.
    @Column(name = "food_no")
    private Integer foodNo;

    // AI추정/사용자입력 — foodNo/customFoodNo 중 하나만 채워집니다.
    @Column(name = "custom_food_no")
    private Integer customFoodNo;

    @Column(name = "intake_at", nullable = false)
    private LocalDateTime intakeAt;

    @Column(name = "pre_glucose")
    private Integer preGlucose;

    // [각주] FastAPI predict가 돌려준 predictedGlucoseRise를 baseline(식전 혈당)에 더한
    // "예상 식후 혈당"입니다. 나중에 POST_GLUCOSE(실측값)와 비교해서 피드백을 줄 때 씁니다.
    @Column(name = "post_glucose_est")
    private Integer postGlucoseEst;

    @Column(name = "portion", nullable = false, precision = 5, scale = 2)
    private BigDecimal portion;

    @Builder
    private IntakeLog(Integer userNo, Integer foodNo, Integer customFoodNo,
                       Integer preGlucose, Integer postGlucoseEst, BigDecimal portion) {
        this.userNo = userNo;
        this.foodNo = foodNo;
        this.customFoodNo = customFoodNo;
        this.preGlucose = preGlucose;
        this.postGlucoseEst = postGlucoseEst;
        this.portion = portion;
    }

    @PrePersist
    protected void onCreate() {
        this.intakeAt = LocalDateTime.now();
    }
}

package com.dangdang.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * [각주 AT] walk_mission 테이블과 매핑되는 엔티티입니다.
 * 음식 최종 확정("맞아요") 시점에 intake_log 저장과 함께 딱 1건 생성됩니다(status=READY).
 * 이후 걷기 시작/완료/취소는 이 row를 UPDATE(status만 바꿈)해서 반영합니다 — 확정마다
 * 새로 여러 건이 생기는 게 아니라, 끼니 1번 = row 1개입니다.
 *
 * DB에 아래 제약이 걸려 있습니다 (user_no당 활성 미션은 최대 1개):
 * CREATE UNIQUE INDEX uq_active_mission ON walk_mission (user_no)
 *     WHERE status IN ('READY', 'IN_PROGRESS');
 *
 * @lastModified 2026-08-18
 */
@Entity
@Table(name = "walk_mission")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WalkMission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mission_no")
    private Integer missionNo;

    @Column(name = "user_no", nullable = false)
    private Integer userNo;

    // [각주] intake_log 1건당 walk_mission 1건 — 이 끼니가 어떤 섭취 기록에서 나온 미션인지 참조합니다.
    @Column(name = "log_no", nullable = false)
    private Integer logNo;

    @Column(name = "target_kcal", nullable = false, precision = 6, scale = 2)
    private BigDecimal targetKcal;

    @Column(name = "target_distance", nullable = false, precision = 5, scale = 2)
    private BigDecimal targetDistance;

    @Column(name = "actual_distance", precision = 5, scale = 2)
    private BigDecimal actualDistance;

    @Column(name = "post_walk_glucose")
    private Integer postWalkGlucose;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private WalkMissionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "expire_reason", length = 20)
    private ExpireReason expireReason;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "last_tracked_at")
    private LocalDateTime lastTrackedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private WalkMission(Integer userNo, Integer logNo, BigDecimal targetKcal, BigDecimal targetDistance) {
        this.userNo = userNo;
        this.logNo = logNo;
        this.targetKcal = targetKcal;
        this.targetDistance = targetDistance;
        this.status = WalkMissionStatus.READY;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * [각주 AU] confirmIntake()에서, 이미 활성 상태(READY/IN_PROGRESS)였던 "이전" 미션을
     * 자동으로 취소 처리할 때 씁니다. status를 EXPIRED로 바꾸고 expire_reason=CANCEL,
     * end_time을 지금 시각으로 남깁니다.
     */
    public void cancelForNewConfirm() {
        this.status = WalkMissionStatus.EXPIRED;
        this.expireReason = ExpireReason.CANCEL;
        this.endTime = LocalDateTime.now();
    }
}

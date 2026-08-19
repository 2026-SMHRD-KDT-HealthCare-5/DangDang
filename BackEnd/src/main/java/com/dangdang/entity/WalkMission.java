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
 * [각주] (수정) target_distance/actual_distance 단위를 km → m(미터)로 바꿨습니다
 * (WalkMissionService의 체크포인트/이상치 감지 로직이 원래 "미터" 단위를 가정하고 있었는데,
 * FastAPI가 km으로 내려주고 있어서 단위가 안 맞던 버그를 여기서 바로잡았습니다).
 * ↓↓↓ [DB 반영 필요 — 마이그레이션 파일은 안 만들었습니다, 아래 내용 그대로 직접 반영해주세요] ↓↓↓
 * km 기준 DECIMAL(5,2)는 최대 999.99까지만 저장 가능한데, m 기준으로는 값이 최대
 * 2700 안팎까지 나올 수 있어서(정수 4자리 필요) 자리수가 부족합니다. 아래처럼 컬럼을 늘려주세요:
 *   ALTER TABLE walk_mission MODIFY target_distance DECIMAL(6,2) NOT NULL;
 *   ALTER TABLE walk_mission MODIFY actual_distance DECIMAL(6,2) NULL;
 * (기존에 저장돼 있던 값은 km라서 지금 남아있는 테스트 데이터와는 단위가 안 맞습니다 —
 *  운영 데이터가 아니라 테스트 데이터뿐이면 그냥 정리하고 새로 테스트하는 걸 추천합니다.)
 * ↑↑↑ ------------------------------------------------------------------------- ↑↑↑
 *
 * @lastModified 2026-08-19
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

    // [각주] (수정) km → m로 단위가 바뀌면서 정수 자리가 최대 4자리까지 필요해져 precision을
    // 5 → 6으로 올렸습니다(DB 컬럼도 같이 늘려야 함 — 클래스 상단 주석 참고).
    @Column(name = "target_distance", nullable = false, precision = 6, scale = 2)
    private BigDecimal targetDistance;

    @Column(name = "actual_distance", precision = 6, scale = 2)
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
     * 자동으로 취소 처리할 때 씁니다. status를 EXPIRED로 바꾸고 expire_reason=CANCELLED,
     * end_time을 지금 시각으로 남깁니다.
     *
     * @lastModified 2026-08-18
     */
    public void cancelForNewConfirm() {
        this.status = WalkMissionStatus.EXPIRED;
        this.expireReason = ExpireReason.CANCELLED;
        this.endTime = LocalDateTime.now();
    }

    /**
     * [각주 BS] POST /api/walk-missions/{mission_no}/expire 가 호출합니다. INACTIVE(30분 미활동)
     * 또는 CANCELLED(콜드스타트 시 로컬 진행 상태 없음) 두 사유로 프론트가 직접 종료시킬 때 씁니다.
     * TIMEOUT 배치의 expireByTimeout()과 달리, 프론트가 실측한 actualDistance를 함께 받습니다.
     *
     * @lastModified 2026-08-18
     */
    public void expireByFrontend(ExpireReason reason, BigDecimal actualDistance) {
        this.status = WalkMissionStatus.EXPIRED;
        this.expireReason = reason;
        this.actualDistance = actualDistance;
        this.endTime = LocalDateTime.now();
    }

    /**
     * [각주 BT] 서버 @Scheduled 배치(매분)가 호출합니다. READY 상태로 created_at 2시간 초과된
     * 미션을 정리합니다 — 시작도 안 한 미션이라 actualDistance는 남기지 않습니다.
     *
     * @lastModified 2026-08-18
     */
    public void expireByTimeout() {
        this.status = WalkMissionStatus.EXPIRED;
        this.expireReason = ExpireReason.TIMEOUT;
        this.endTime = LocalDateTime.now();
    }

    /**
     * [각주 BU] 서버 @Scheduled 배치(매분)가 호출합니다. IN_PROGRESS 상태로 last_tracked_at이
     * 30분 초과된 미션을 정리합니다. 주 목적은 "30분간 실제 위치 이동이 없음" 감지입니다 —
     * /track이 폴링(30초 주기)마다 거리가 실제로 늘었을 때만 last_tracked_at을 갱신하도록
     * 만들 예정이라, 앱을 켜놓고 가만히 있는 경우와 앱을 아예 꺼버린 경우가 둘 다 이 같은
     * "last_tracked_at 정체"로 자연스럽게 잡힙니다. (/track은 아직 미구현)
     *
     * @lastModified 2026-08-19
     */
    public void expireByInactiveBatch() {
        this.status = WalkMissionStatus.EXPIRED;
        this.expireReason = ExpireReason.INACTIVE;
        this.endTime = LocalDateTime.now();
    }

    /**
     * [각주 CJ] POST /api/walk-missions/{mission_no}/start 가 호출합니다. READY → IN_PROGRESS
     * 전환이고, startTime/lastTrackedAt을 둘 다 "지금"으로 초기화합니다. lastTrackedAt을 null로
     * 안 두고 시작 시각으로 채워두는 이유 — 이렇게 해야 사용자가 시작만 누르고 정말 한 번도
     * 안 움직여도, 30분 뒤 INACTIVE 배치가 last_tracked_at 정체를 정상적으로 잡아냅니다
     * (null이면 배치의 "Before" 비교 자체가 안 걸려서 영원히 안 잡힙니다).
     *
     * @lastModified 2026-08-19
     */
    public void startWalking() {
        this.status = WalkMissionStatus.IN_PROGRESS;
        this.startTime = LocalDateTime.now();
        this.lastTrackedAt = this.startTime;
    }

    /**
     * [각주 CK] /track 폴링마다 호출 — "실시간 정확한 총 거리"만 갱신합니다. 이동 임계값
     * 판단(체크포인트)이랑은 별개라 last_tracked_at은 안 건드립니다 (WalkMissionService 참고).
     *
     * @lastModified 2026-08-19
     */
    public void recordDistance(BigDecimal distance) {
        this.actualDistance = distance;
    }

    /** [각주 CL] 최소 이동거리(1m) 검증을 통과했을 때만 호출 — last_tracked_at을 지금 시각으로 갱신합니다. */
    public void markMovementCheckpoint() {
        this.lastTrackedAt = LocalDateTime.now();
    }

    /**
     * [각주 CM] POST /api/walk-missions/{mission_no}/end 가 호출합니다. 목표 거리 도달 여부에 따라
     * COMPLETE 또는 PARTIAL로 저장하고 endTime을 지금 시각으로 남깁니다. actualDistance는 이미
     * /track이 계속 갱신해온 값을 그대로 쓰므로 여기서 다시 안 건드립니다.
     *
     * @lastModified 2026-08-19
     */
    public void completeManually(WalkMissionStatus finalStatus) {
        this.status = finalStatus;
        this.endTime = LocalDateTime.now();
    }
}

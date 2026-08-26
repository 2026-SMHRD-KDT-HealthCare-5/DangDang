package com.dangdang.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * [각주 EB] team_member 테이블과 매핑되는 엔티티입니다. 팀 가입 = 이 row 1건 생성,
 * 팀 나가기 = 이 row 삭제(하드 딜리트, 노션 "팀 나가기" 명세).
 *
 * [각주] (수정 2026-08-20) 실제 DB 컬럼은 team_member_no가 아니라 member_no입니다
 * (DangDang_schema.md 기준). joined_at은 스키마 파일엔 DATE로 적혀 있었지만, 실제 살아있는
 * DB에 붙여서 부팅해보니 TIMESTAMP였습니다(Hibernate 스키마 검증 에러로 확인) — 파일이
 * 최신 상태를 완전히 반영 못 하고 있던 것 같아서, 실제 DB 기준(TIMESTAMP)으로 맞췄습니다.
 * total_distance(가입 이후 누적, km)는 이미 "저장되는 컬럼"으로 만들어져 있어서,
 * 원래 제가 하려던 것처럼 walk_mission을 매번 SUM해서 계산하는 대신, 이 컬럼을 직접 씁니다 —
 * 대신 걷기 미션이 COMPLETE/PARTIAL로 끝날 때마다 이 값을 더해줘야 하므로
 * WalkMissionService.endMission()에 addTeamDistanceIfJoined() 호출을 추가했습니다.
 *
 * [각주] last_active_at 컬럼은 매핑 안 했습니다 — "미접속 자동 강퇴" 로직이 삭제된 걸로 이미
 * 정리됐고(노션 "팀 나가기" 명세), DB에서도 NOT NULL 제약을 풀어둔 상태(확장 보류)라 아예
 * 안 건드립니다.
 *
 * @lastModified 2026-08-20
 */
@Entity
@Table(name = "team_member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_no")
    private Integer memberNo;

    @Column(name = "team_no", nullable = false)
    private Integer teamNo;

    @Column(name = "user_no", nullable = false)
    private Integer userNo;

    // [각주] DB 컬럼 타입이 TIMESTAMP입니다(스키마 파일과 다름, 위 클래스 각주 참고).
    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    // [각주] "가입 이후 누적거리(km)" — 저장되는 컬럼입니다. 가입 시점엔 0으로 시작하고,
    // 걷기 미션이 COMPLETE/PARTIAL로 끝날 때마다 addDistance()로 더해집니다.
    @Column(name = "total_distance", nullable = false, precision = 7, scale = 2)
    private BigDecimal totalDistance;

    @Builder
    private TeamMember(Integer teamNo, Integer userNo) {
        this.teamNo = teamNo;
        this.userNo = userNo;
        this.totalDistance = BigDecimal.ZERO;
    }

    @PrePersist
    protected void onCreate() {
        this.joinedAt = LocalDateTime.now();
    }

    /**
     * [각주] 걷기 미션이 COMPLETE/PARTIAL로 끝날 때마다 WalkMissionService가 호출합니다.
     * distanceKm은 이번 미션의 actualDistance(m)를 km로 환산한 값입니다.
     */
    public void addDistance(BigDecimal distanceKm) {
        this.totalDistance = this.totalDistance.add(distanceKm);
    }
}

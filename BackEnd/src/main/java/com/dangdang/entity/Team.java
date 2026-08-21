package com.dangdang.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * [각주 EA] team 테이블과 매핑되는 엔티티입니다. 팀은 전부 공개(is_public 개념 없음)이고,
 * 초대/승인 절차 없이 아무나 검색해서 바로 가입할 수 있습니다(노션 "팀 만들기"/"팀 가입하기" 명세).
 *
 * [각주] (수정 2026-08-20) 이 테이블은 이미 DB에 만들어져 있었습니다 — 프로젝트에 올라와 있는
 * DangDang_schema.md를 뒤늦게 확인했는데, 제가 처음에 임의로 설계했던 컬럼 타입과 실제 DB가
 * 달라서(예: created_at이 TIMESTAMP가 아니라 DATE) 스키마 검증 에러가 났었습니다. 이제
 * DangDang_schema.md 기준으로 다시 맞췄습니다. 앞으로 팀 관련 스키마 확인은 이 파일을 기준으로
 * 합니다 — 마이그레이션 파일은 여전히 안 만들고, 필요한 변경만 각주로 안내합니다.
 *
 * [각주] target_distance 단위는 km입니다 — walk_mission의 target_distance/actual_distance는
 * (걸음 1회 단위라) m로 바꿨지만, 팀은 한 달치 여러 명 실적을 합산하는 규모라 km가 자연스러워서
 * 그대로 km로 뒀습니다.
 *
 * @lastModified 2026-08-20
 */
@Entity
@Table(name = "team")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_no")
    private Integer teamNo;

    // [각주] 팀 만들기 화면설계서 비고: "팀명 20자" 제한 — length=20으로 서버에서도 같이 검증합니다.
    // [각주] DB엔 UNIQUE 제약이 없습니다(DangDang_schema.md 기준) — 팀명 중복 체크는
    // TeamService에서 existsByTeamName()으로 애플리케이션 레벨에서만 막습니다.
    @Column(name = "team_name", nullable = false, length = 20)
    private String teamName;

    // [각주] 화면설계서 비고: "소개 100자" 제한.
    @Column(name = "team_intro", length = 100)
    private String teamIntro;

    // [각주] 팀 생성자 = 방장. 별도 role 컬럼 없이 이 필드로만 방장 여부를 판단합니다
    // (isCreator = team.creatorNo.equals(요청자 userNo)).
    @Column(name = "creator_no", nullable = false)
    private Integer creatorNo;

    @Column(name = "target_distance", nullable = false, precision = 6, scale = 2)
    private BigDecimal targetDistance;

    // [각주] DB 컬럼 타입이 DATE입니다(시분초 없음) — LocalDate로 매핑합니다.
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDate createdAt;

    @Builder
    private Team(String teamName, String teamIntro, Integer creatorNo, BigDecimal targetDistance) {
        this.teamName = teamName;
        this.teamIntro = teamIntro;
        this.creatorNo = creatorNo;
        this.targetDistance = targetDistance;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDate.now();
    }

    /**
     * [각주] (추가 2026-08-21) 방장이 팀을 나갈 때 호출합니다. 남은 팀원 중 가입일(joinedAt)이
     * 가장 오래된 사람에게 방장을 자동으로 넘깁니다 — TeamService.leaveTeam() 참고.
     */
    public void transferCreator(Integer newCreatorNo) {
        this.creatorNo = newCreatorNo;
    }
}

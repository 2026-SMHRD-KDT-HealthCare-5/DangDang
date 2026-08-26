package com.dangdang.repository;

import com.dangdang.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * [각주 ED] team_member 테이블 CRUD + 팀 실적 집계 쿼리.
 *
 * [각주] (수정 2026-08-20) DangDang_schema.md 기준으로 다시 맞췄습니다. "가입 이후 누적"은
 * team_member.total_distance 컬럼을 그대로 쓰므로(TeamMember.addDistance() 참고) 더 이상
 * walk_mission을 SUM할 필요가 없어졌습니다 — findMemberDistanceRankingByTeam()이 단순해졌습니다.
 * "이번 달 누적"은 저장 컬럼이 없어서 여전히 walk_mission을 그때그때 합산합니다.
 *
 * sumMonthlyDistanceMByTeam : 팀 하나의 "이번 달(또는 조회월) 누적거리(m)". 팀 검색/상세/
 * 랭킹 전부 이 쿼리 하나를 재사용합니다. 서비스 계층에서 1000으로 나눠 km로 변환해서 응답합니다.
 *
 * findMemberDistanceRankingByTeam : 팀 하나의 "팀원별 가입 이후 누적거리(km)" 내림차순 목록
 * (team_member.total_distance 컬럼 직접 조회, 이미 km 단위).
 *
 * findTeamMonthlyRanking : 전체 팀의 "이번 달(또는 조회월) 누적거리(m)" 내림차순 목록.
 * 팀 월간 랭킹 조회(GET /api/rankings/teams)에서 씁니다.
 *
 * @lastModified 2026-08-20
 */
public interface TeamMemberRepository extends JpaRepository<TeamMember, Integer> {

    // [각주] "한 유저는 동시에 한 팀만" 전제로 내 팀 조회(GET /api/teams/me)에 씁니다.
    Optional<TeamMember> findByUserNo(Integer userNo);

    long countByTeamNo(Integer teamNo);

    boolean existsByTeamNoAndUserNo(Integer teamNo, Integer userNo);

    List<TeamMember> findByTeamNo(Integer teamNo);

    void deleteByTeamNoAndUserNo(Integer teamNo, Integer userNo);

    // [각주] (추가 2026-08-21) 방장이 나갈 때 방장을 넘길 대상(가입일이 가장 오래된 남은 팀원)을
    // 찾는 데 씁니다. 남은 팀원이 없으면 빈 Optional — TeamService.leaveTeam()이 이 경우
    // 팀 자체를 삭제합니다.
    Optional<TeamMember> findFirstByTeamNoOrderByJoinedAtAsc(Integer teamNo);

    @Query(value = """
            SELECT COALESCE(SUM(wm.actual_distance), 0)
            FROM team_member tm
            LEFT JOIN walk_mission wm
                ON wm.user_no = tm.user_no
                AND wm.status IN ('COMPLETE', 'PARTIAL')
                AND wm.end_time >= :monthStart
                AND wm.end_time < :monthEnd
                AND wm.end_time >= tm.joined_at
            WHERE tm.team_no = :teamNo
            """, nativeQuery = true)
    BigDecimal sumMonthlyDistanceMByTeam(@Param("teamNo") Integer teamNo,
                                          @Param("monthStart") LocalDateTime monthStart,
                                          @Param("monthEnd") LocalDateTime monthEnd);

    @Query(value = """
            SELECT u.nickname AS "nickname", tm.total_distance AS "totalDistanceKm"
            FROM team_member tm
            JOIN users u ON u.user_no = tm.user_no
            WHERE tm.team_no = :teamNo
            ORDER BY tm.total_distance DESC
            """, nativeQuery = true)
    List<MemberDistanceRow> findMemberDistanceRankingByTeam(@Param("teamNo") Integer teamNo);

    @Query(value = """
            SELECT t.team_no AS "teamNo", t.team_name AS "teamName",
                   COUNT(DISTINCT tm.member_no) AS "memberCount",
                   COALESCE(SUM(CASE WHEN wm.status IN ('COMPLETE', 'PARTIAL')
                                       AND wm.end_time >= :monthStart
                                       AND wm.end_time < :monthEnd
                                       AND wm.end_time >= tm.joined_at
                                      THEN wm.actual_distance ELSE 0 END), 0) AS "monthlyDistanceM"
            FROM team t
            JOIN team_member tm ON tm.team_no = t.team_no
            LEFT JOIN walk_mission wm ON wm.user_no = tm.user_no
            GROUP BY t.team_no, t.team_name
            ORDER BY "monthlyDistanceM" DESC
            """, nativeQuery = true)
    List<TeamRankingRow> findTeamMonthlyRanking(@Param("monthStart") LocalDateTime monthStart,
                                                 @Param("monthEnd") LocalDateTime monthEnd);

    /**
     * [각주] 네이티브 쿼리 결과를 그냥 Object[]로 안 받고 이렇게 projection 인터페이스로
     * 받으면 Spring Data JPA가 컬럼명(AS 뒤 별칭)을 보고 자동으로 채워줍니다.
     */
    interface MemberDistanceRow {
        String getNickname();
        BigDecimal getTotalDistanceKm();
    }

    interface TeamRankingRow {
        Integer getTeamNo();
        String getTeamName();
        Long getMemberCount();
        BigDecimal getMonthlyDistanceM();
    }
}

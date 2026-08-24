package com.dangdang.service;

import com.dangdang.dto.request.TeamCreateRequest;
import com.dangdang.dto.response.TeamCreateResponse;
import com.dangdang.dto.response.TeamChallengeSummaryResponse;
import com.dangdang.dto.response.TeamDetailResponse;
import com.dangdang.dto.response.TeamSearchResponse;
import com.dangdang.entity.Team;
import com.dangdang.entity.TeamMember;
import com.dangdang.exception.BusinessException;
import com.dangdang.exception.ErrorCode;
import com.dangdang.repository.TeamMemberRepository;
import com.dangdang.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * [각주 EJ] 팀(Team) 생성/검색/가입/탈퇴/상세조회를 담당합니다. WalkMissionService처럼 팀
 * 도메인 관련 기능을 하나의 서비스로 모았습니다(챌린지 집계 로직도 별도 서비스로 안 뺐습니다 —
 * 팀 상세조회 하나에서만 쓰이는 로직이라 굳이 나눌 필요가 없다고 판단했습니다).
 *
 * [각주] 팀 관련 거리는 전부 km. "이번 달 누적"(currentDistance/progressRate)은 저장 컬럼이
 * 없어서 walk_mission을 그때그때 합산(m)한 뒤 여기서 km로 변환합니다. "가입 이후 누적"
 * (팀원별 totalDistance)은 team_member.total_distance 컬럼에 이미 km로 저장돼 있어서
 * (WalkMissionService.addTeamDistanceIfJoined() 참고) 변환 없이 그대로 씁니다.
 *
 * @lastModified 2026-08-20
 */
@Service
@RequiredArgsConstructor
public class TeamService {

    // [각주] 화면설계서/API 명세에 "최대 10명 정원"으로 고정돼 있어 상수로 둡니다.
    private static final int TEAM_CAPACITY = 10;
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;

    /**
     * [각주 EK] POST /api/teams. 팀명 20자/소개 100자 검증 후 팀 생성, 생성자를 방장으로
     * team_member에도 즉시 등록합니다(노션 명세: "생성자는 자동으로 팀 멤버(방장)로 등록됨").
     */
    @Transactional
    public TeamCreateResponse createTeam(Integer userNo, TeamCreateRequest request) {
        String teamName = request.teamName();
        if (teamName == null || teamName.isBlank() || teamName.length() > 20) {
            throw new BusinessException(ErrorCode.INVALID_TEAM_NAME_LENGTH);
        }
        if (request.teamIntro() != null && request.teamIntro().length() > 100) {
            throw new BusinessException(ErrorCode.INVALID_TEAM_INTRO_LENGTH);
        }
        if (teamRepository.existsByTeamName(teamName)) {
            throw new BusinessException(ErrorCode.TEAM_NAME_DUPLICATED);
        }
        // [각주] "한 유저는 동시에 한 팀만" 전제이므로, 팀을 새로 만드는 것도 가입의 일종이라
        // 이미 다른 팀에 속해 있으면 막습니다(TeamMember.java 각주 참고).
        if (teamMemberRepository.findByUserNo(userNo).isPresent()) {
            throw new BusinessException(ErrorCode.ALREADY_JOINED);
        }

        Team team = Team.builder()
                .teamName(teamName)
                .teamIntro(request.teamIntro())
                .creatorNo(userNo)
                .targetDistance(request.targetDistance())
                .build();
        Team savedTeam = teamRepository.save(team);

        TeamMember creatorMembership = TeamMember.builder()
                .teamNo(savedTeam.getTeamNo())
                .userNo(userNo)
                .build();
        teamMemberRepository.save(creatorMembership);

        return new TeamCreateResponse(savedTeam.getTeamNo());
    }

    /**
     * [각주 EL] GET /api/teams?keyword=. 모든 팀이 공개라 별도 필터 없이 팀명 부분일치 검색만
     * 합니다. keyword가 비어있으면 전체 팀이 다 걸립니다("".contains 는 전체 매치).
     */
    @Transactional(readOnly = true)
    public List<TeamSearchResponse> searchTeams(String keyword) {
        String safeKeyword = (keyword == null) ? "" : keyword;
        LocalDateTime[] monthRange = currentMonthRange();

        return teamRepository.findByTeamNameContaining(safeKeyword).stream()
                .map(team -> {
                    long memberCount = teamMemberRepository.countByTeamNo(team.getTeamNo());
                    BigDecimal currentDistance = monthlyDistanceKm(team.getTeamNo(), monthRange[0], monthRange[1]);
                    int progressRate = calculateProgressRate(currentDistance, team.getTargetDistance());
                    return new TeamSearchResponse(
                            team.getTeamNo(), team.getTeamName(), team.getTeamIntro(),
                            memberCount, TEAM_CAPACITY, team.getTargetDistance(), currentDistance, progressRate);
                })
                .toList();
    }

    /**
     * [각주 EM] POST /api/teams/{team_no}/join. 정원(10명) 체크 → 중복가입 체크 순서로
     * 검증합니다. DB의 uq_team_member(team_no, user_no)가 최후 방어선이지만, 사용자에게
     * 더 정확한 에러(TEAM_FULL/ALREADY_JOINED)를 보여주려고 서비스에서 먼저 걸러냅니다.
     */
    @Transactional
    public void joinTeam(Integer userNo, Integer teamNo) {
        Team team = teamRepository.findById(teamNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));

        if (teamMemberRepository.findByUserNo(userNo).isPresent()) {
            throw new BusinessException(ErrorCode.ALREADY_JOINED);
        }
        if (teamMemberRepository.countByTeamNo(team.getTeamNo()) >= TEAM_CAPACITY) {
            throw new BusinessException(ErrorCode.TEAM_FULL);
        }

        TeamMember membership = TeamMember.builder()
                .teamNo(team.getTeamNo())
                .userNo(userNo)
                .build();
        teamMemberRepository.save(membership);
    }

    /**
     * [각주 EN] DELETE /api/teams/{team_no}/members/me. 가입 안 한 팀이면 404.
     *
     * [각주] (추가 2026-08-21) 나간 뒤 상태에 따라 두 가지를 추가로 처리합니다(사용자 결정):
     * 1) 나간 게 마지막 팀원이었으면 → 팀(team row) 자체를 삭제
     * 2) 나간 사람이 방장이었고 팀원이 아직 남아있으면 → 남은 팀원 중 가입일이 가장 오래된
     *    사람에게 방장을 자동으로 넘김 (Team.transferCreator() 참고)
     */
    @Transactional
    public void leaveTeam(Integer userNo, Integer teamNo) {
        Team team = teamRepository.findById(teamNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));
        if (!teamMemberRepository.existsByTeamNoAndUserNo(teamNo, userNo)) {
            throw new BusinessException(ErrorCode.NOT_TEAM_MEMBER);
        }

        boolean wasCreator = team.getCreatorNo().equals(userNo);
        teamMemberRepository.deleteByTeamNoAndUserNo(teamNo, userNo);

        Optional<TeamMember> oldestRemaining = teamMemberRepository.findFirstByTeamNoOrderByJoinedAtAsc(teamNo);
        if (oldestRemaining.isEmpty()) {
            // 마지막 팀원이 나감 — 팀 자체를 삭제합니다.
            teamRepository.delete(team);
        } else if (wasCreator) {
            // 방장이 나갔고 팀원이 남아있음 — 가장 오래 가입한 사람에게 방장을 넘깁니다.
            team.transferCreator(oldestRemaining.get().getUserNo());
            teamRepository.save(team);
        }
    }

    /** [각주 EO] GET /api/teams/{team_no} (팀 챌린지 현황 조회). */
    @Transactional(readOnly = true)
    public TeamDetailResponse getTeamDetail(Integer userNo, Integer teamNo) {
        Team team = teamRepository.findById(teamNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));
        return buildDetailResponse(team, userNo);
    }

    /**
     * [각주 EP] GET /api/teams/me — 노션 명세에는 없지만 제가 추가한 API입니다. 커뮤니티
     * 메인/팀챌린지 탭 화면설계서의 연결 API가 GET /api/teams/{no}인데, 프론트가 로그인
     * 직후 자기 team_no를 알아낼 방법이 5개 기존 엔드포인트(만들기/검색/가입/나가기/상세)
     * 어디에도 없어서, walk-missions의 GET /active와 같은 패턴(200+본문 또는 204)으로
     * 만들었습니다. 가입한 팀이 없으면 컨트롤러가 null 대신 204를 내려줍니다.
     */
    @Transactional(readOnly = true)
    public TeamDetailResponse getMyTeam(Integer userNo) {
        return teamMemberRepository.findByUserNo(userNo)
                .map(myMembership -> {
                    Team team = teamRepository.findById(myMembership.getTeamNo())
                            .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));
                    return buildDetailResponse(team, userNo);
                })
                .orElse(null);
    }

    /**
     * [각주] (추가 2026-08-21) GET /api/home 의 teamChallenge 블록에서 씁니다. 팀 상세조회랑
     * 계산은 거의 같은데, 팀원 전체가 아니라 상위 3명만 잘라서 돌려줍니다(홈 화면 요약용).
     * 가입한 팀이 없으면 null입니다.
     */
    @Transactional(readOnly = true)
    public TeamChallengeSummaryResponse getTeamChallengeSummary(Integer userNo) {
        return teamMemberRepository.findByUserNo(userNo)
                .map(myMembership -> {
                    Team team = teamRepository.findById(myMembership.getTeamNo())
                            .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));

                    LocalDateTime[] monthRange = currentMonthRange();
                    BigDecimal currentDistance = monthlyDistanceKm(team.getTeamNo(), monthRange[0], monthRange[1]);
                    int progressRate = calculateProgressRate(currentDistance, team.getTargetDistance());
                    String challengeMonth = YearMonth.now().format(MONTH_FORMATTER);

                    List<TeamChallengeSummaryResponse.TopMember> topMembers = teamMemberRepository
                            .findMemberDistanceRankingByTeam(team.getTeamNo()).stream()
                            .limit(3)
                            .map(row -> new TeamChallengeSummaryResponse.TopMember(row.getNickname(), row.getTotalDistanceKm()))
                            .toList();

                    return new TeamChallengeSummaryResponse(team.getTeamNo(), team.getTeamName(), challengeMonth,
                            team.getTargetDistance(), currentDistance, progressRate, topMembers);
                })
                .orElse(null);
    }

    private TeamDetailResponse buildDetailResponse(Team team, Integer requesterUserNo) {
        LocalDateTime[] monthRange = currentMonthRange();
        long memberCount = teamMemberRepository.countByTeamNo(team.getTeamNo());
        BigDecimal currentDistance = monthlyDistanceKm(team.getTeamNo(), monthRange[0], monthRange[1]);
        int progressRate = calculateProgressRate(currentDistance, team.getTargetDistance());
        boolean isCreator = team.getCreatorNo().equals(requesterUserNo);

        // [각주] total_distance는 team_member 테이블에 이미 km 단위로 저장돼 있어서(TeamMember.java
        // 각주 참고) 여기선 m→km 변환(toKm) 없이 그대로 씁니다.
        List<TeamDetailResponse.MemberRanking> members = teamMemberRepository
                .findMemberDistanceRankingByTeam(team.getTeamNo()).stream()
                .map(row -> new TeamDetailResponse.MemberRanking(row.getNickname(), row.getTotalDistanceKm()))
                .toList();

        String challengeMonth = YearMonth.now().format(MONTH_FORMATTER);

        return new TeamDetailResponse(
                team.getTeamNo(), team.getTeamName(), team.getTeamIntro(), challengeMonth,
                team.getTargetDistance(), currentDistance, progressRate, memberCount, TEAM_CAPACITY,
                isCreator, members);
    }

    private BigDecimal monthlyDistanceKm(Integer teamNo, LocalDateTime monthStart, LocalDateTime monthEnd) {
        BigDecimal meters = teamMemberRepository.sumMonthlyDistanceMByTeam(teamNo, monthStart, monthEnd);
        return toKm(meters);
    }

    private BigDecimal toKm(BigDecimal meters) {
        BigDecimal safeMeters = (meters == null) ? BigDecimal.ZERO : meters;
        return safeMeters.divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP);
    }

    /** [각주] progressRate = currentDistance/targetDistance*100, 0~100 범위로 자름(100 초과 시 100 고정 — 노션 명세). */
    private int calculateProgressRate(BigDecimal currentDistanceKm, BigDecimal targetDistanceKm) {
        if (targetDistanceKm == null || targetDistanceKm.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        int rate = currentDistanceKm.multiply(BigDecimal.valueOf(100))
                .divide(targetDistanceKm, 0, RoundingMode.HALF_UP)
                .intValue();
        return Math.min(100, Math.max(0, rate));
    }

    /** [각주] 이번 달의 [1일 00:00, 다음 달 1일 00:00) 범위입니다. */
    static LocalDateTime[] currentMonthRange() {
        YearMonth thisMonth = YearMonth.now();
        LocalDateTime start = thisMonth.atDay(1).atStartOfDay();
        LocalDateTime end = thisMonth.plusMonths(1).atDay(1).atStartOfDay();
        return new LocalDateTime[]{start, end};
    }
}

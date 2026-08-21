package com.dangdang.service;

import com.dangdang.dto.response.TeamRankingResponse;
import com.dangdang.entity.TeamMember;
import com.dangdang.exception.BusinessException;
import com.dangdang.exception.ErrorCode;
import com.dangdang.repository.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * [각주 EQ] GET /api/rankings/teams (팀 월간 랭킹 조회). 노션 명세: 기존
 * "개인 전체 랭킹 조회(GET /api/rankings/users)"를 대체합니다 — 개인 랭킹은 제공하지 않습니다.
 * 팀 카테고리(생성/검색/가입/탈퇴/상세)와는 다른 API 카테고리(rankings)라 TeamService와
 * 별도 서비스로 분리했습니다.
 *
 * @lastModified 2026-08-20
 */
@Service
@RequiredArgsConstructor
public class RankingService {

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final TeamMemberRepository teamMemberRepository;

    /**
     * [각주 ER] month 파라미터가 없으면 이번 달, 있으면 "YYYY-MM" 형식으로 파싱합니다.
     * 정렬 기준은 monthlyDistance 절대값 내림차순(진행률 기준 아님 — 노션 명세).
     */
    @Transactional(readOnly = true)
    public TeamRankingResponse getTeamMonthlyRanking(Integer userNo, String month) {
        YearMonth targetMonth = parseMonth(month);
        LocalDateTime monthStart = targetMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = targetMonth.plusMonths(1).atDay(1).atStartOfDay();

        List<TeamMemberRepository.TeamRankingRow> rows =
                teamMemberRepository.findTeamMonthlyRanking(monthStart, monthEnd);

        List<TeamRankingResponse.Ranking> rankings = new ArrayList<>();
        Integer myTeamRank = null;
        Integer myTeamNo = teamMemberRepository.findByUserNo(userNo)
                .map(TeamMember::getTeamNo)
                .orElse(null);

        for (int i = 0; i < rows.size(); i++) {
            TeamMemberRepository.TeamRankingRow row = rows.get(i);
            int rank = i + 1;
            rankings.add(new TeamRankingResponse.Ranking(
                    rank, row.getTeamNo(), row.getTeamName(), row.getMemberCount(), toKm(row.getMonthlyDistanceM())));
            if (myTeamNo != null && myTeamNo.equals(row.getTeamNo())) {
                myTeamRank = rank;
            }
        }

        return new TeamRankingResponse(targetMonth.format(MONTH_FORMATTER), rankings, myTeamRank);
    }

    private YearMonth parseMonth(String month) {
        if (month == null || month.isBlank()) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(month, MONTH_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new BusinessException(ErrorCode.INVALID_MONTH_FORMAT);
        }
    }

    private BigDecimal toKm(BigDecimal meters) {
        BigDecimal safeMeters = (meters == null) ? BigDecimal.ZERO : meters;
        return safeMeters.divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP);
    }
}

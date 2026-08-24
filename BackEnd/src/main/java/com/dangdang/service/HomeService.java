package com.dangdang.service;

import com.dangdang.dto.response.HomeResponse;
import com.dangdang.dto.response.WeeklyAttendanceStatus;
import com.dangdang.entity.User;
import com.dangdang.entity.WalkMission;
import com.dangdang.entity.WalkMissionStatus;
import com.dangdang.repository.IntakeLogRepository;
import com.dangdang.repository.UserRepository;
import com.dangdang.repository.WalkMissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * [각주 FD] GET /api/home 하나를 위한 서비스입니다. 홈 화면 세 블록(주간 걷기달성 / 오늘 혈당
 * 추이 / 내 걷기 거리 요약)을 조립합니다.
 *
 * [각주] (수정 2026-08-21) 원래 있던 팀 챌린지 요약 블록은 삭제하고 개인 걷기 거리 요약으로
 * 교체하면서, TeamService 의존성도 같이 뺐습니다(더 이상 홈에서 팀 정보를 안 씀).
 *
 * @lastModified 2026-08-21
 */
@Service
@RequiredArgsConstructor
public class HomeService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    // [각주] 월요일부터 시작하는 이번 주 7일 라벨입니다.
    private static final String[] KOREAN_DAY_LABELS = {"월", "화", "수", "목", "금", "토", "일"};
    // [각주] "내가 걸은 거리" 집계에 포함할 상태 — 팀 실적 집계와 동일 기준(EXPIRED 제외).
    private static final List<WalkMissionStatus> COUNTED_STATUSES =
            List.of(WalkMissionStatus.COMPLETE, WalkMissionStatus.PARTIAL);

    private final WalkMissionRepository walkMissionRepository;
    private final IntakeLogRepository intakeLogRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public HomeResponse getHome(Integer userNo) {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(DayOfWeek.MONDAY);
        LocalDateTime weekStart = monday.atStartOfDay();
        LocalDateTime weekEnd = monday.plusWeeks(1).atStartOfDay();

        // [각주] 이번 주(월~일)에 "끝난" 미션을 한 번에 불러와서, weeklyAttendance랑
        // glucoseTrend의 POST_WALK 포인트(오늘 것만) 둘 다에 재사용합니다 — DB 쿼리 한 번으로 충분.
        List<WalkMission> weekMissions = walkMissionRepository.findByUserNoAndEndTimeBetween(userNo, weekStart, weekEnd);

        List<HomeResponse.WeeklyAttendanceDay> weeklyAttendance = buildWeeklyAttendance(monday, weekMissions);
        HomeResponse.GlucoseTrend glucoseTrend = buildGlucoseTrend(userNo, today, weekMissions);
        HomeResponse.WalkingDistanceSummary walkingDistance = buildWalkingDistanceSummary(userNo, today);

        return new HomeResponse(weeklyAttendance, glucoseTrend, walkingDistance);
    }

    /**
     * [각주] 하루라도 COMPLETE 미션이 있으면 DONE, 없는데 끝난 미션(PARTIAL/EXPIRED)은 있으면
     * MISSED, 그날 끝난 미션 자체가 없으면(미래 요일 포함) status는 null입니다 — "NONE" 문자열은
     * 안 씁니다(WeeklyAttendanceStatus 각주 참고).
     */
    private List<HomeResponse.WeeklyAttendanceDay> buildWeeklyAttendance(LocalDate monday, List<WalkMission> weekMissions) {
        List<HomeResponse.WeeklyAttendanceDay> result = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate day = monday.plusDays(i);
            List<WalkMission> dayMissions = weekMissions.stream()
                    .filter(mission -> mission.getEndTime().toLocalDate().equals(day))
                    .toList();

            WeeklyAttendanceStatus status = null;
            if (dayMissions.stream().anyMatch(mission -> mission.getStatus() == WalkMissionStatus.COMPLETE)) {
                status = WeeklyAttendanceStatus.DONE;
            } else if (!dayMissions.isEmpty()) {
                status = WeeklyAttendanceStatus.MISSED;
            }

            result.add(new HomeResponse.WeeklyAttendanceDay(KOREAN_DAY_LABELS[i], status));
        }
        return result;
    }

    /**
     * [각주] 오늘 하루치 PRE(식전, intake_log)와 POST_WALK(걷기 후, walk_mission) 포인트를
     * 합쳐서 시간순으로 정렬합니다. AI 예상치(postGlucoseEst)는 넣지 않습니다(노션 명세).
     */
    private HomeResponse.GlucoseTrend buildGlucoseTrend(Integer userNo, LocalDate today, List<WalkMission> weekMissions) {
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.plusDays(1).atStartOfDay();

        List<HomeResponse.GlucosePoint> points = new ArrayList<>();

        intakeLogRepository.findByUserNoAndIntakeAtBetween(userNo, todayStart, todayEnd).stream()
                .filter(log -> log.getPreGlucose() != null)
                .forEach(log -> points.add(new HomeResponse.GlucosePoint(
                        log.getIntakeAt().toLocalTime().format(TIME_FORMATTER), log.getPreGlucose(), "PRE")));

        weekMissions.stream()
                .filter(mission -> mission.getEndTime().toLocalDate().equals(today) && mission.getPostWalkGlucose() != null)
                .forEach(mission -> points.add(new HomeResponse.GlucosePoint(
                        mission.getEndTime().toLocalTime().format(TIME_FORMATTER), mission.getPostWalkGlucose(), "POST_WALK")));

        points.sort(Comparator.comparing(HomeResponse.GlucosePoint::time));

        Integer targetGlucose = userRepository.findById(userNo).map(User::getTargetGlucose).orElse(null);
        return new HomeResponse.GlucoseTrend(targetGlucose, points);
    }

    /**
     * [각주] (추가 2026-08-21) 오늘/이번달/전체 걷기 거리(km)입니다. walk_mission.actual_distance는
     * m 단위라 여기서 1000으로 나눠 km로 바꿉니다(팀 쪽 거리 표기와 통일).
     */
    private HomeResponse.WalkingDistanceSummary buildWalkingDistanceSummary(Integer userNo, LocalDate today) {
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.plusDays(1).atStartOfDay();

        YearMonth thisMonth = YearMonth.now();
        LocalDateTime monthStart = thisMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = thisMonth.plusMonths(1).atDay(1).atStartOfDay();

        BigDecimal todayDistanceM = walkMissionRepository.sumDistanceMByUserAndEndTimeBetween(
                userNo, COUNTED_STATUSES, todayStart, todayEnd);
        BigDecimal monthlyDistanceM = walkMissionRepository.sumDistanceMByUserAndEndTimeBetween(
                userNo, COUNTED_STATUSES, monthStart, monthEnd);
        BigDecimal totalDistanceM = walkMissionRepository.sumTotalDistanceMByUser(userNo, COUNTED_STATUSES);

        return new HomeResponse.WalkingDistanceSummary(toKm(todayDistanceM), toKm(monthlyDistanceM), toKm(totalDistanceM));
    }

    private BigDecimal toKm(BigDecimal meters) {
        BigDecimal safeMeters = (meters == null) ? BigDecimal.ZERO : meters;
        return safeMeters.divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP);
    }
}

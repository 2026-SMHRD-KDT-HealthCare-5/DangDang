package com.dangdang.service;

import com.dangdang.dto.request.ExpireMissionRequest;
import com.dangdang.dto.request.PostGlucoseRequest;
import com.dangdang.dto.request.TrackRequest;
import com.dangdang.dto.response.ActiveMissionResponse;
import com.dangdang.dto.response.EndMissionResponse;
import com.dangdang.dto.response.ExpireMissionResponse;
import com.dangdang.dto.response.PostGlucoseResponse;
import com.dangdang.dto.response.StartMissionResponse;
import com.dangdang.dto.response.TrackResponse;
import com.dangdang.entity.AiChat;
import com.dangdang.entity.ChatType;
import com.dangdang.entity.ExpireReason;
import com.dangdang.entity.IntakeLog;
import com.dangdang.entity.User;
import com.dangdang.entity.WalkMission;
import com.dangdang.entity.WalkMissionStatus;
import com.dangdang.exception.BusinessException;
import com.dangdang.exception.ErrorCode;
import com.dangdang.repository.AiChatRepository;
import com.dangdang.repository.IntakeLogRepository;
import com.dangdang.repository.UserRepository;
import com.dangdang.repository.WalkMissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * [각주 BZ] walk_mission의 "조회/시작/트래킹/종료/만료" 쪽을 담당합니다. "생성" 쪽(READY 자동 생성)은
 * IntakeLogService.confirmIntake()가 맡고 있어서 여기서는 다루지 않습니다.
 *
 * - getActiveMission()      : GET /api/walk-missions/active
 * - startMission()          : POST /{mission_no}/start
 * - trackMission()          : POST /{mission_no}/track (30초 폴링)
 * - endMission()            : POST /{mission_no}/end (수동 종료)
 * - recordPostGlucose()     : POST /{mission_no}/post-glucose (걷기 후 실측 혈당 입력)
 * - expireMission()         : POST /api/walk-missions/{mission_no}/expire (프론트가 직접 호출)
 * - expireStaleMissionsBatch() : WalkMissionExpireScheduler(매분 배치)가 호출. TIMEOUT은
 *   "시작을 안 함"을, INACTIVE는 "last_tracked_at 30분간 정체 = 실제 이동 없음"을 감지합니다
 *   (앱을 켜놓고 안 움직이는 경우/아예 꺼버린 경우 둘 다 같은 신호로 잡힙니다 — WalkMission.expireByInactiveBatch() 주석 참고).
 *
 * [각주 CR] checkpointDistanceCache : "언제 last_tracked_at을 마지막으로 갱신했는지" 판단하는
 * 기준선(체크포인트 거리)을 DB 컬럼이 아니라 이 서비스 안의 메모리(Map)로만 관리합니다.
 * DB를 오갈 필요 없이 매 폴링마다 즉시 비교할 수 있어 효율적이고, 서버가 재시작되면 진행 중이던
 * 걷기는 어차피 이어하기를 지원하지 않고 CANCELLED로 정리되는 정책이라 캐시가 날아가도 문제
 * 없습니다(사용자 결정 사항). 미션이 끝나는 모든 경로에서 evictCheckpointCache()로 지워줘야
 * 서버를 오래 켜둬도 메모리가 계속 쌓이지 않습니다.
 *
 * @lastModified 2026-08-19
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WalkMissionService {

    private static final int TIMEOUT_HOURS = 2;
    private static final int INACTIVE_MINUTES = 30;
    private static final BigDecimal MIN_MOVEMENT_METERS = BigDecimal.ONE;
    private static final double MAX_SPEED_MPS = 4.5; // 초당 4.5m ≈ 시속 16.2km, 가벼운 조깅보다 빠른 수준
    private static final BigDecimal DEFAULT_WEIGHT_KG = BigDecimal.valueOf(60);

    private final WalkMissionRepository walkMissionRepository;
    private final AiChatRepository aiChatRepository;
    private final UserRepository userRepository;
    private final IntakeLogRepository intakeLogRepository;

    private final Map<Integer, BigDecimal> checkpointDistanceCache = new ConcurrentHashMap<>();

    /**
     * [각주 CA] 활성 미션(READY/IN_PROGRESS)이 있으면 그 내용을, 없으면 null을 돌려줍니다.
     * 컨트롤러가 null이면 204 No Content로, 아니면 200 OK + 바디로 응답합니다.
     */
    public ActiveMissionResponse getActiveMission(Integer userNo) {
        return walkMissionRepository.findFirstByUserNoAndStatusIn(
                        userNo, List.of(WalkMissionStatus.READY, WalkMissionStatus.IN_PROGRESS))
                .map(this::toActiveMissionResponse)
                .orElse(null);
    }

    /**
     * [각주 CS] POST /{mission_no}/start 가 호출합니다. READY → IN_PROGRESS 전환입니다.
     */
    @Transactional
    public StartMissionResponse startMission(Integer userNo, Integer missionNo) {
        WalkMission mission = walkMissionRepository.findById(missionNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.MISSION_NOT_FOUND));

        if (!mission.getUserNo().equals(userNo)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_MISSION_ACCESS);
        }
        if (mission.getStatus() != WalkMissionStatus.READY) {
            throw new BusinessException(ErrorCode.MISSION_NOT_STARTABLE);
        }

        mission.startWalking();
        walkMissionRepository.save(mission);

        return new StartMissionResponse(mission.getMissionNo(), mission.getStatus(),
                mission.getStartTime(), mission.getCreatedAt());
    }

    /**
     * [각주 CT] POST /{mission_no}/track 이 호출합니다 (30초 폴링). 순서:
     * 1) 미션 존재/소유자/IN_PROGRESS 확인
     * 2) 이동 이상치(비정상 속도) 검사 — 저장된 actualDistance 대비 이번 값 증가분을,
     *    "진짜 경과시간"(지금 − last_tracked_at)으로 나눠 속도를 계산합니다. 기준치 이상이면
     *    이상치로 보고 이번 폴링을 통째로 무시합니다(거리 반영 안 함, 미션은 그대로 IN_PROGRESS
     *    유지 — 강제 종료는 안 합니다, 프론트가 경고만 띄우는 것으로 결정됨).
     * 3) 정상이면 actualDistance는 매번 무조건 갱신(실시간 정확한 값), last_tracked_at은
     *    메모리 캐시의 체크포인트와 비교해서 1m 이상 차이날 때만 갱신.
     *
     * @lastModified 2026-08-19
     */
    @Transactional
    public TrackResponse trackMission(Integer userNo, Integer missionNo, TrackRequest request) {
        WalkMission mission = walkMissionRepository.findById(missionNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.MISSION_NOT_FOUND));

        if (!mission.getUserNo().equals(userNo)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_MISSION_ACCESS);
        }
        if (mission.getStatus() != WalkMissionStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.MISSION_NOT_IN_PROGRESS);
        }

        BigDecimal previousDistance = mission.getActualDistance() != null ? mission.getActualDistance() : BigDecimal.ZERO;
        BigDecimal reportedDistance = request.currentDistance() != null ? request.currentDistance() : previousDistance;
        BigDecimal delta = reportedDistance.subtract(previousDistance);

        LocalDateTime baselineTime = mission.getLastTrackedAt() != null ? mission.getLastTrackedAt() : mission.getStartTime();
        long elapsedSeconds = Math.max(1, Duration.between(baselineTime, LocalDateTime.now()).getSeconds());

        boolean anomalyDetected = delta.compareTo(BigDecimal.ZERO) > 0
                && (delta.doubleValue() / elapsedSeconds) > MAX_SPEED_MPS;

        if (anomalyDetected) {
            // [각주 CU] "포켓몬스터처럼" — 미션을 강제 종료하지 않고, 이번 폴링의 거리만 반영 안 함.
            // actualDistance/checkpoint/last_tracked_at 전부 그대로 둡니다.
            boolean goalReachedOnAnomaly = previousDistance.compareTo(mission.getTargetDistance()) >= 0;
            return new TrackResponse(goalReachedOnAnomaly, true);
        }

        mission.recordDistance(reportedDistance);

        BigDecimal checkpoint = checkpointDistanceCache.getOrDefault(missionNo, BigDecimal.ZERO);
        if (reportedDistance.subtract(checkpoint).compareTo(MIN_MOVEMENT_METERS) >= 0) {
            checkpointDistanceCache.put(missionNo, reportedDistance);
            mission.markMovementCheckpoint();
        }

        walkMissionRepository.save(mission);

        boolean goalReached = reportedDistance.compareTo(mission.getTargetDistance()) >= 0;
        return new TrackResponse(goalReached, false);
    }

    /**
     * [각주 CV] POST /{mission_no}/end 가 호출합니다 (사용자가 직접 "걷기 종료" 버튼).
     * actualDistance는 /track이 이미 실시간으로 갱신해온 값을 그대로 신뢰합니다
     * (클라이언트가 다시 보낸 값을 안 믿는 이유는 confirmIntake()의 food_info 재조회와 같은
     * 이유 — 서버가 계속 검증해온 값이 클라이언트가 임의로 보낸 값보다 신뢰할 수 있습니다).
     *
     * 소모 칼로리는 MET(운동대사당량) 공식으로 계산합니다: kcal = MET × 체중(kg) × 시간(hour).
     * 속도(평균 km/h)에 따라 MET 값을 다르게 적용합니다 — calculateBurnedKcal() 참고.
     *
     * @lastModified 2026-08-19
     */
    @Transactional
    public EndMissionResponse endMission(Integer userNo, Integer missionNo) {
        WalkMission mission = walkMissionRepository.findById(missionNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.MISSION_NOT_FOUND));

        if (!mission.getUserNo().equals(userNo)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_MISSION_ACCESS);
        }
        if (mission.getStatus() != WalkMissionStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.MISSION_NOT_IN_PROGRESS);
        }

        BigDecimal actualDistance = mission.getActualDistance() != null ? mission.getActualDistance() : BigDecimal.ZERO;
        WalkMissionStatus finalStatus = actualDistance.compareTo(mission.getTargetDistance()) >= 0
                ? WalkMissionStatus.COMPLETE : WalkMissionStatus.PARTIAL;

        mission.completeManually(finalStatus);
        walkMissionRepository.save(mission);
        evictCheckpointCache(missionNo);

        long durationMinutes = Duration.between(mission.getStartTime(), mission.getEndTime()).toMinutes();
        BigDecimal weightKg = userRepository.findById(userNo).map(User::getWeight).orElse(DEFAULT_WEIGHT_KG);
        BigDecimal burnedKcal = calculateBurnedKcal(actualDistance, durationMinutes, weightKg);

        savePostGlucoseChat(userNo, missionNo);

        return new EndMissionResponse(finalStatus, actualDistance, durationMinutes, burnedKcal, true);
    }

    /**
     * [각주 CW] MET(Metabolic Equivalent of Task, 운동대사당량) 기반 소모 칼로리 추정 공식입니다.
     * AI나 별도 학습 데이터가 필요한 게 아니라, 운동생리학에서 흔히 쓰는 표준 공식입니다
     * (Compendium of Physical Activities 기준 MET 표를 걷기 속도 구간별로 단순화했습니다).
     *
     * kcal = MET × 체중(kg) × 시간(hour)
     *
     * 평균 속도(km/h) = 실제 이동거리(km 환산) ÷ 소요시간(hour) 로 구해서, 그 속도 구간에 맞는
     * MET 값을 고릅니다 — 빨리 걸을수록 더 많은 칼로리를 태우는 걸 반영합니다.
     * 체중이 미입력(null)인 사용자는 60kg를 기본값으로 씁니다(다른 곳의 hba1c 기본값 패턴과 동일).
     *
     * [각주] (수정) actualDistance가 이제 m(미터) 단위라서, MET 구간표(km/h 기준)에 넣기 전에
     * 여기서만 내부적으로 km로 환산합니다 — DB/응답값 자체는 그대로 m을 씁니다.
     */
    private BigDecimal calculateBurnedKcal(BigDecimal actualDistanceM, long durationMinutes, BigDecimal weightKg) {
        if (durationMinutes <= 0) {
            return BigDecimal.ZERO;
        }
        double hours = durationMinutes / 60.0;
        double distanceKm = actualDistanceM.doubleValue() / 1000.0;
        double speedKmh = distanceKm / hours;
        double met = resolveWalkingMet(speedKmh);
        double kcal = met * weightKg.doubleValue() * hours;
        return BigDecimal.valueOf(kcal).setScale(1, RoundingMode.HALF_UP);
    }

    /** 걷기 속도 구간별 MET 값 (표준 운동생리학 수치를 간단히 4단계로 나눔). */
    private double resolveWalkingMet(double speedKmh) {
        if (speedKmh < 3.2) return 2.0;   // 아주 느린 걸음
        if (speedKmh < 4.8) return 3.0;   // 보통 걸음
        if (speedKmh < 6.4) return 4.3;   // 빠른 걸음
        return 6.0;                        // 경보/가벼운 조깅
    }

    /** POST_GLUCOSE 카드 저장 — 규칙 기반 고정 문구라 LLM을 안 씁니다(노션 명세 그대로). */
    private void savePostGlucoseChat(Integer userNo, Integer missionNo) {
        String cardDataJson = "{\"missionNo\":" + missionNo + "}";
        AiChat aiChat = AiChat.builder()
                .userNo(userNo)
                .aiMessage("걷기 완료! 🎉 수고했어요!\n이제 혈당을 입력해주세요.")
                .chatType(ChatType.POST_GLUCOSE)
                .cardData(cardDataJson)
                .build();
        aiChatRepository.save(aiChat);
    }

    /**
     * [각주 DE] POST /{mission_no}/post-glucose 가 호출합니다 (노션 "걷기 후 혈당 기록" 명세).
     * 걷기 종료(/end) 응답 시점에 이미 챗봇에 POST_GLUCOSE 카드가 떠 있는 상태에서, 사용자가
     * 그 카드에 실측 혈당을 입력해 제출하면 이걸 호출합니다.
     *
     * 순서:
     * 1) 미션 존재/소유자 확인
     * 2) 상태가 COMPLETE/PARTIAL인지 확인 (EXPIRED 미션엔 입력 불가 — 409)
     * 3) 이미 입력된 값이 있으면 거부 (재입력/수정 미지원 — 409)
     * 4) postWalkGlucose 저장
     * 5) goalAchieved(=status가 COMPLETE였는지)에 따라 RESULT_CARD_SUCCESS/FAIL 챗봇 카드 저장
     *    (Gemini 호출 없음 — 필요한 4개 수치가 전부 서버가 이미 아는 값이라 규칙 기반 고정 문구로 처리)
     *
     * @lastModified 2026-08-19
     */
    @Transactional
    public PostGlucoseResponse recordPostGlucose(Integer userNo, Integer missionNo, PostGlucoseRequest request) {
        WalkMission mission = walkMissionRepository.findById(missionNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.MISSION_NOT_FOUND));

        if (!mission.getUserNo().equals(userNo)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_MISSION_ACCESS);
        }
        if (mission.getStatus() != WalkMissionStatus.COMPLETE && mission.getStatus() != WalkMissionStatus.PARTIAL) {
            throw new BusinessException(ErrorCode.MISSION_NOT_FINISHED);
        }
        if (mission.getPostWalkGlucose() != null) {
            throw new BusinessException(ErrorCode.POST_GLUCOSE_ALREADY_RECORDED);
        }

        mission.recordPostWalkGlucose(request.postWalkGlucose());
        walkMissionRepository.save(mission);

        // [각주] logNo는 WalkMission 생성 시(confirmIntake) 항상 같이 채워지는 필수 FK라
        // 여기서 못 찾는 건 데이터 정합성이 깨진 예외 상황입니다 — 그래도 500 대신 사용자가
        // 이해할 수 있는 404로 응답하도록 MISSION_NOT_FOUND를 재사용했습니다.
        IntakeLog intakeLog = intakeLogRepository.findById(mission.getLogNo())
                .orElseThrow(() -> new BusinessException(ErrorCode.MISSION_NOT_FOUND));

        boolean goalAchieved = mission.getStatus() == WalkMissionStatus.COMPLETE;
        String feedbackMessage = goalAchieved
                ? "목표를 달성했어요! 오늘도 건강한 습관을 실천했네요! 계속 함께 관리해요"
                : "목표를 달성하지 못했어요... 하지만 오늘도 열심히 하려는 모습이 정말 멋져요! 계속 함께 관리해봐요!";

        saveResultCardChat(userNo, mission, intakeLog, goalAchieved, feedbackMessage);

        return new PostGlucoseResponse(
                mission.getMissionNo(),
                intakeLog.getPreGlucose(),
                intakeLog.getPostGlucoseEst(),
                mission.getPostWalkGlucose(),
                mission.getTargetDistance(),
                mission.getActualDistance(),
                goalAchieved,
                feedbackMessage
        );
    }

    /** RESULT_CARD_SUCCESS/FAIL 카드 저장 — 수치 요약과 응원 문구를 하나의 카드로 합칩니다(노션 명세). */
    private void saveResultCardChat(Integer userNo, WalkMission mission, IntakeLog intakeLog,
                                     boolean goalAchieved, String feedbackMessage) {
        String cardDataJson = "{"
                + "\"missionNo\":" + mission.getMissionNo() + ","
                + "\"preGlucose\":" + intakeLog.getPreGlucose() + ","
                + "\"postGlucoseEst\":" + intakeLog.getPostGlucoseEst() + ","
                + "\"postWalkGlucose\":" + mission.getPostWalkGlucose() + ","
                + "\"targetDistance\":" + mission.getTargetDistance() + ","
                + "\"actualDistance\":" + mission.getActualDistance()
                + "}";

        AiChat aiChat = AiChat.builder()
                .userNo(userNo)
                .aiMessage(feedbackMessage)
                .chatType(goalAchieved ? ChatType.RESULT_CARD_SUCCESS : ChatType.RESULT_CARD_FAIL)
                .cardData(cardDataJson)
                .build();
        aiChatRepository.save(aiChat);
    }

    /**
     * [각주 CB] 프론트가 INACTIVE(30분 미활동) 또는 CANCELLED(콜드스타트 시 로컬 진행상태 없음)
     * 사유로 미션을 직접 종료시킬 때 호출합니다.
     *
     * 1) 미션 존재/소유자 확인
     * 2) IN_PROGRESS 상태인지 확인 (이미 끝난 미션을 또 만료시키는 요청 방지)
     * 3) expireReason 문자열 검증 (TIMEOUT은 배치 전용이라 프론트가 보낼 수 없음)
     * 4) 상태 변경 + NOTICE 챗봇 메시지 저장
     */
    @Transactional
    public ExpireMissionResponse expireMission(Integer userNo, Integer missionNo, ExpireMissionRequest request) {
        WalkMission mission = walkMissionRepository.findById(missionNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.MISSION_NOT_FOUND));

        if (!mission.getUserNo().equals(userNo)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_MISSION_ACCESS);
        }
        if (mission.getStatus() != WalkMissionStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.MISSION_NOT_EXPIRABLE);
        }

        ExpireReason reason = parseFrontendExpireReason(request.expireReason());
        mission.expireByFrontend(reason, request.actualDistance());
        walkMissionRepository.save(mission);
        evictCheckpointCache(missionNo);

        String noticeMessage = buildNoticeMessage(reason);
        saveNoticeChat(userNo, noticeMessage);

        return new ExpireMissionResponse(mission.getStatus(), mission.getExpireReason(), noticeMessage);
    }

    /**
     * [각주 CC] WalkMissionExpireScheduler(매분 @Scheduled)가 호출하는 배치 본체입니다.
     * 두 가지를 정리합니다 — 둘 다 "프론트가 처리했어야 하는데 놓친 경우"의 백업 경로입니다.
     * 1) READY 상태로 created_at이 2시간 넘은 미션 → EXPIRED(TIMEOUT)
     * 2) IN_PROGRESS 상태로 last_tracked_at이 30분 넘은 미션 → EXPIRED(INACTIVE)
     */
    @Transactional
    public void expireStaleMissionsBatch() {
        LocalDateTime now = LocalDateTime.now();

        List<WalkMission> timedOut = walkMissionRepository.findByStatusAndCreatedAtBefore(
                WalkMissionStatus.READY, now.minusHours(TIMEOUT_HOURS));
        for (WalkMission mission : timedOut) {
            mission.expireByTimeout();
            walkMissionRepository.save(mission);
            saveNoticeChat(mission.getUserNo(), buildNoticeMessage(ExpireReason.TIMEOUT));
        }

        List<WalkMission> inactive = walkMissionRepository.findByStatusAndLastTrackedAtBefore(
                WalkMissionStatus.IN_PROGRESS, now.minusMinutes(INACTIVE_MINUTES));
        for (WalkMission mission : inactive) {
            mission.expireByInactiveBatch();
            walkMissionRepository.save(mission);
            evictCheckpointCache(mission.getMissionNo());
            saveNoticeChat(mission.getUserNo(), buildNoticeMessage(ExpireReason.INACTIVE));
        }

        if (!timedOut.isEmpty() || !inactive.isEmpty()) {
            log.info("걷기 미션 배치 만료 처리: TIMEOUT {}건, INACTIVE {}건", timedOut.size(), inactive.size());
        }
    }

    /**
     * [각주 CX] 미션이 어떤 경로로든 끝날 때 반드시 호출해야 하는 캐시 정리 메서드입니다.
     * public인 이유: IntakeLogService.confirmIntake()가 "이미 활성 미션이 있으면 자동취소"할
     * 때도 이걸 호출해야 해서(다른 서비스에서 부르니까 private이면 안 됨).
     */
    public void evictCheckpointCache(Integer missionNo) {
        checkpointDistanceCache.remove(missionNo);
    }

    private ActiveMissionResponse toActiveMissionResponse(WalkMission mission) {
        return new ActiveMissionResponse(
                mission.getMissionNo(),
                mission.getStatus(),
                mission.getTargetDistance(),
                mission.getTargetKcal(),
                mission.getActualDistance(),
                mission.getStartTime(),
                mission.getLastTrackedAt(),
                mission.getCreatedAt()
        );
    }

    /** "TIMEOUT"은 배치 전용이라 프론트 요청에서는 허용하지 않습니다. */
    private ExpireReason parseFrontendExpireReason(String rawReason) {
        Optional<ExpireReason> parsed = java.util.Arrays.stream(ExpireReason.values())
                .filter(candidate -> candidate.name().equals(rawReason))
                .findFirst();
        if (parsed.isEmpty() || parsed.get() == ExpireReason.TIMEOUT) {
            throw new BusinessException(ErrorCode.INVALID_EXPIRE_REASON);
        }
        return parsed.get();
    }

    private String buildNoticeMessage(ExpireReason reason) {
        return switch (reason) {
            case CANCELLED -> "연결이 끊겨서 이전 걷기 미션이 종료됐어요.";
            case INACTIVE -> "30분 동안 움직임이 없어서 걷기 미션이 자동으로 종료됐어요.";
            case TIMEOUT -> "시간이 많이 지나 걷기 미션이 자동으로 취소됐어요.";
        };
    }

    /** NOTICE 타입 ai_chat row를 저장합니다 (card_data 없이 안내 문구만). */
    private void saveNoticeChat(Integer userNo, String message) {
        AiChat aiChat = AiChat.builder()
                .userNo(userNo)
                .aiMessage(message)
                .chatType(ChatType.NOTICE)
                .cardData(null)
                .build();
        aiChatRepository.save(aiChat);
    }
}

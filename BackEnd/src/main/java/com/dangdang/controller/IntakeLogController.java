package com.dangdang.controller;

import com.dangdang.dto.request.IntakeConfirmRequest;
import com.dangdang.dto.request.PortionPredictRequest;
import com.dangdang.dto.request.PreGlucoseRequest;
import com.dangdang.dto.response.FoodRecognitionResponse;
import com.dangdang.dto.response.IntakeConfirmResponse;
import com.dangdang.dto.response.PortionPredictResponse;
import com.dangdang.dto.response.PreGlucoseResponse;
import com.dangdang.dto.response.ReanalyzeResponse;
import com.dangdang.exception.BusinessException;
import com.dangdang.exception.ErrorCode;
import com.dangdang.service.IntakeLogService;
import com.dangdang.service.RecognizeProxyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * [각주 U] 음식 섭취 기록(IntakeLog) 관련 API.
 * 안드로이드가 직접 FastAPI를 호출하지 않고, 항상 Spring을 거쳐서 호출합니다
 * (기획서 아키텍처: Android -> Spring -> FastAPI). Spring은 여기서 인증(JWT) 확인,
 * 입력값 검증을 하고, 실제 AI 판단은 FastAPI에 위임합니다.
 */
@RestController
@RequestMapping("/api/intake-logs")
@RequiredArgsConstructor
public class IntakeLogController {

    private final IntakeLogService intakeLogService;
    private final RecognizeProxyService recognizeProxyService;

    /**
     * [각주 X] 식전 혈당 입력 (8단계 흐름의 ① 단계, 음식 인식보다 먼저 호출됨).
     * - preGlucose를 보내면 그 값을 그대로 씀 (preGlucoseDefault=null로 응답).
     * - preGlucose를 생략(또는 null)하면 서버가 사용자 hba1c 구간으로 기본값을 계산해서
     *   response.preGlucose에 채워 돌려줌 (이 경우 preGlucoseDefault도 같은 값).
     * 이 단계에서는 아직 INTAKE_LOG에 저장하지 않습니다 — 여기서 정해진 값은 프론트가
     * 들고 있다가 recognize/최종확정 호출 때 baseline/preGlucose로 함께 실어 보냅니다.
     */
    @PostMapping("/preglucose")
    public ResponseEntity<PreGlucoseResponse> preGlucose(
            Authentication authentication,
            @RequestBody(required = false) PreGlucoseRequest request
    ) {
        Integer userNo = (Integer) authentication.getPrincipal();
        Integer preGlucose = request == null ? null : request.preGlucose();

        PreGlucoseResponse response = intakeLogService.resolvePreGlucose(userNo, preGlucose);
        return ResponseEntity.ok(response);
    }

    /**
     * 음식 인식 (사진 또는 텍스트).
     * - image / message 중 최소 하나는 필수
     * - baseline(식전 혈당)은 선택 — 안 보내면 FastAPI가 기본값을 적용
     * - [각주] (수정) diagnosisGroup은 더 이상 요청 파라미터로 안 받습니다. 사용자의 실제 진단
     *   정보라 요청마다 프론트가 골라 보낼 이유가 없어서, 서버가 항상 로그인 사용자의
     *   users.diagnosis_group을 그대로 조회해서 씁니다.
     */
    @PostMapping(value = "/recognize", consumes = "multipart/form-data")
    public ResponseEntity<FoodRecognitionResponse> recognize(
            Authentication authentication,
            @RequestParam(required = false) MultipartFile image,
            @RequestParam(required = false) String message,
            @RequestParam(required = false) Double baseline
    ) {
        boolean hasImage = image != null && !image.isEmpty();
        boolean hasMessage = message != null && !message.isBlank();

        if (!hasImage && !hasMessage) {
            throw new BusinessException(ErrorCode.MISSING_FOOD_INPUT);
        }

        // [각주 Q] AuthController.logout()과 동일한 패턴: JwtAuthenticationFilter가
        // SecurityContext에 넣어둔 로그인 사용자의 userNo를 꺼냅니다.
        Integer userNo = (Integer) authentication.getPrincipal();

        FoodRecognitionResponse response = intakeLogService.recognizeFood(userNo, image, message, baseline);
        return ResponseEntity.ok(response);
    }

    /**
     * [각주 AN] "얼마나 드셨어요?"(portion) 응답 후 호출 — recognize에서 받은 1인분 영양성분을
     * 그대로 body에 실어 보내면, portion 반영된 예상 혈당 상승량 + 걷기 미션 목표치를 돌려줍니다.
     * 아직 "맞아요" 최종 확정 전 단계라 DB 저장은 없습니다(recognize와 동일하게 순수 프록시).
     */
    @PostMapping("/predict")
    public ResponseEntity<PortionPredictResponse> predict(
            Authentication authentication,
            @RequestBody PortionPredictRequest request
    ) {
        Integer userNo = (Integer) authentication.getPrincipal();
        PortionPredictResponse response = intakeLogService.predictPortion(userNo, request);
        return ResponseEntity.ok(response);
    }

    /**
     * [각주 BP] 음식 최종 확정("맞아요"). foodNo(식약처 매칭)와 customFood(AI재분석/직접입력
     * 결과) 중 정확히 하나만 body에 실어 보내야 합니다 — IntakeConfirmRequest 참고.
     * 여기서만 실제로 DB에 저장됩니다(intake_log/custom_food/walk_mission/ai_chat).
     *
     * @lastModified 2026-08-18
     */
    @PostMapping
    public ResponseEntity<IntakeConfirmResponse> confirm(
            Authentication authentication,
            @RequestBody IntakeConfirmRequest request
    ) {
        Integer userNo = (Integer) authentication.getPrincipal();
        IntakeConfirmResponse response = intakeLogService.confirmIntake(userNo, request);
        return ResponseEntity.ok(response);
    }

    /**
     * [각주 BQ] "틀려요, AI로 분석하기" — recognize가 식약처 DB에서 못 찾았거나 사용자가
     * 결과가 틀렸다고 할 때 호출합니다. image/foodName 중 최소 하나는 필수입니다.
     * recognize/predict와 동일하게 순수 프록시라 DB에는 아무것도 저장하지 않습니다.
     * 여기서 나온 결과가 마음에 들면 "맞아요"(confirm)를 눌러야 저장됩니다.
     *
     * @lastModified 2026-08-18
     */
    @PostMapping(value = "/reanalyze", consumes = "multipart/form-data")
    public ResponseEntity<ReanalyzeResponse> reanalyze(
            Authentication authentication,
            @RequestParam(required = false) MultipartFile image,
            @RequestParam(required = false) String foodName,
            @RequestParam(required = false) Double baseline
    ) {
        boolean hasImage = image != null && !image.isEmpty();
        boolean hasFoodName = foodName != null && !foodName.isBlank();

        if (!hasImage && !hasFoodName) {
            throw new BusinessException(ErrorCode.MISSING_REANALYZE_INPUT);
        }

        Integer userNo = (Integer) authentication.getPrincipal();
        ReanalyzeResponse response = recognizeProxyService.reanalyze(userNo, image, foodName, baseline);
        return ResponseEntity.ok(response);
    }

    // [각주 BR] (삭제) "직접입력하기" 전용 미리보기(/custom-food)는 뺐습니다 — 직접입력은
    // 검색어 재입력/AI분석처럼 "결과가 마음에 안 들어서 다시 시도"할 일이 없어서, 미리보기 없이
    // 프론트가 입력 폼 제출 즉시 confirm(POST /api/intake-logs, customFood.source="사용자입력")을
    // 바로 호출하면 됩니다. confirm 응답에 predictedGlucoseRise 등이 이미 들어있어서 보여줄
    // 정보도 그대로 나오고, 저장도 그 한 번의 호출로 끝납니다.
}

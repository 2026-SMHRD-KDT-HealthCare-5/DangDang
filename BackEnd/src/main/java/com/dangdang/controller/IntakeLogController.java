package com.dangdang.controller;

import com.dangdang.dto.request.PreGlucoseRequest;
import com.dangdang.dto.response.FoodRecognitionResponse;
import com.dangdang.dto.response.PreGlucoseResponse;
import com.dangdang.exception.BusinessException;
import com.dangdang.exception.ErrorCode;
import com.dangdang.service.IntakeLogService;
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
     * - baseline(식전 혈당), diagnosisGroup(진단군)은 선택 — 안 보내면 FastAPI가 기본값을 적용
     */
    @PostMapping(value = "/recognize", consumes = "multipart/form-data")
    public ResponseEntity<FoodRecognitionResponse> recognize(
            Authentication authentication,
            @RequestParam(required = false) MultipartFile image,
            @RequestParam(required = false) String message,
            @RequestParam(required = false) Double baseline,
            @RequestParam(required = false) String diagnosisGroup
    ) {
        boolean hasImage = image != null && !image.isEmpty();
        boolean hasMessage = message != null && !message.isBlank();

        if (!hasImage && !hasMessage) {
            throw new BusinessException(ErrorCode.MISSING_FOOD_INPUT);
        }

        // [각주 Q] AuthController.logout()과 동일한 패턴: JwtAuthenticationFilter가
        // SecurityContext에 넣어둔 로그인 사용자의 userNo를 꺼냅니다.
        Integer userNo = (Integer) authentication.getPrincipal();

        FoodRecognitionResponse response =
                intakeLogService.recognizeFood(userNo, image, message, baseline, diagnosisGroup);
        return ResponseEntity.ok(response);
    }
}

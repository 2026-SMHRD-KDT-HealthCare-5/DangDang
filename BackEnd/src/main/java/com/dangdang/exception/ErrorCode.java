package com.dangdang.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * [각주 D] 서버에서 발생할 수 있는 "비즈니스 오류"를 한 곳에 모아둔 목록입니다.
 * 코드 여기저기서 오류 메시지를 직접 문자열로 적지 않고, 이 enum(열거형)을 참조하게 하면
 * 오류 종류/HTTP 상태코드/메시지를 한눈에 관리할 수 있습니다.
 */
@Getter
public enum ErrorCode {

    // --- 인증(auth) 관련 ---
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "AUTH_409_DUPLICATE_EMAIL", "이미 존재하는 이메일입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_401_INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_INVALID_TOKEN", "유효하지 않거나 만료된 토큰입니다."),
    AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "AUTH_401_REQUIRED", "로그인이 필요합니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH_404_USER_NOT_FOUND", "존재하지 않는 사용자입니다."),
    INVALID_ACTIVITY_LEVEL(HttpStatus.BAD_REQUEST, "AUTH_400_INVALID_ACTIVITY_LEVEL",
            "평소 활동량 값이 올바르지 않습니다. (거의 안함 / 주 1~2회 / 주 3~5회 / 매일 중 하나여야 합니다)"),
    INVALID_DIAGNOSIS_GROUP(HttpStatus.BAD_REQUEST, "AUTH_400_INVALID_DIAGNOSIS_GROUP",
            "진단군 값이 올바르지 않습니다. (건강군 / 전당뇨 / 2형당뇨 중 하나여야 합니다)"),

    // --- 음식 인식(intake-logs) 관련 ---
    MISSING_FOOD_INPUT(HttpStatus.BAD_REQUEST, "FOOD_400_MISSING_INPUT",
            "image 또는 message 중 하나는 필수입니다."),
    AI_SERVER_ERROR(HttpStatus.BAD_GATEWAY, "FOOD_502_AI_SERVER_ERROR",
            "AI 서버 호출에 실패했습니다."),
    MISSING_BASELINE(HttpStatus.BAD_REQUEST, "FOOD_400_MISSING_BASELINE",
            "baseline(식전 혈당)은 필수입니다. preglucose 단계에서 받은 값을 그대로 보내주세요."),

    // --- 음식 최종확정(confirm)/틀려요/직접입력 관련 ---
    MISSING_REANALYZE_INPUT(HttpStatus.BAD_REQUEST, "FOOD_400_MISSING_REANALYZE_INPUT",
            "image 또는 foodName 중 하나는 필수입니다."),
    INVALID_CONFIRM_FOOD_REFERENCE(HttpStatus.BAD_REQUEST, "FOOD_400_INVALID_CONFIRM_FOOD_REFERENCE",
            "foodNo와 customFood 중 정확히 하나만 보내주세요."),
    INVALID_CUSTOM_FOOD_SOURCE(HttpStatus.BAD_REQUEST, "FOOD_400_INVALID_CUSTOM_FOOD_SOURCE",
            "source 값이 올바르지 않습니다. (\"AI추정\" 또는 \"사용자입력\" 중 하나여야 합니다)"),
    FOOD_NOT_FOUND(HttpStatus.NOT_FOUND, "FOOD_404_FOOD_NOT_FOUND",
            "존재하지 않는 음식입니다. (food_no를 다시 확인해주세요)");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}

package com.dangdang.exception;

import com.dangdang.dto.response.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * [각주 E] @RestControllerAdvice : 모든 컨트롤러에서 발생한 예외를 "한 군데에서" 잡아 처리하는 클래스입니다.
 * 이게 없으면 매 컨트롤러마다 try-catch를 반복해야 합니다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1) 우리가 의도적으로 던진 비즈니스 예외 (예: 이메일 중복 -> 409)
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiErrorResponse.of(errorCode.getCode(), errorCode.getMessage()));
    }

    // 2) @Valid 검증 실패 (예: 이메일 형식 오류, 필수값 누락) -> 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .orElse("요청값이 올바르지 않습니다.");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of("COMMON_400_INVALID_INPUT", message));
    }

    // 3) 그 외 예상하지 못한 모든 예외 -> 500 (서버가 죽지 않고 일관된 JSON으로 응답)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.of("COMMON_500_INTERNAL_ERROR", "서버 내부 오류가 발생했습니다."));
    }
}

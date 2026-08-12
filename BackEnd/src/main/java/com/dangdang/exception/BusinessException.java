package com.dangdang.exception;

import lombok.Getter;

/**
 * "이메일 중복", "비밀번호 불일치"처럼 서버 로직상 의도적으로 막아야 하는 상황에서 던지는 예외입니다.
 * Service 코드에서 throw new BusinessException(ErrorCode.DUPLICATE_EMAIL) 처럼 사용하면,
 * GlobalExceptionHandler가 이를 잡아서 알맞은 HTTP 응답으로 변환해줍니다.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}

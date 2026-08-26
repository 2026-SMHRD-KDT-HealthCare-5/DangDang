package com.dangdang.dto.request;

import jakarta.validation.constraints.NotBlank;

/** 토큰 재발급 요청 (POST /api/auth/refresh) */
public record RefreshRequest(

        @NotBlank(message = "refreshToken은 필수입니다.")
        String refreshToken
) {
}

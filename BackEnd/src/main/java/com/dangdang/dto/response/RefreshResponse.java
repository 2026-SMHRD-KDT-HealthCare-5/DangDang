package com.dangdang.dto.response;

/**
 * 토큰 재발급 응답 (200 OK).
 * [각주 O] refresh_token 테이블 도입 이후로는 accessToken뿐 아니라 refreshToken도 매번 새로
 * 발급합니다(회전/rotation). 기존 refreshToken은 이 응답을 받는 즉시 서버에서 폐기되므로,
 * 안드로이드 앱은 응답받은 refreshToken으로 로컬에 저장된 값을 반드시 덮어써야 합니다.
 * (예전 refreshToken을 계속 쓰면 다음 refresh 호출에서 401 INVALID_TOKEN이 납니다)
 */
public record RefreshResponse(
        String accessToken,
        String refreshToken
) {
}

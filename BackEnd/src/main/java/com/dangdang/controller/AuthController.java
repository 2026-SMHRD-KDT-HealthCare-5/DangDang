package com.dangdang.controller;

import com.dangdang.dto.request.LoginRequest;
import com.dangdang.dto.request.RefreshRequest;
import com.dangdang.dto.request.SignUpRequest;
import com.dangdang.dto.response.RefreshResponse;
import com.dangdang.dto.response.SignUpResponse;
import com.dangdang.dto.response.TokenResponse;
import com.dangdang.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * [각주 L] "컨트롤러(Controller)"란?
 * 안드로이드 앱이 실제로 호출하는 HTTP 엔드포인트(URL)를 정의하는 계층입니다.
 * 역할은 "요청을 받아서 검증하고, Service에 위임한 뒤, 결과를 HTTP 응답으로 포장"하는 것뿐이고,
 * 실제 판단 로직은 절대 여기 두지 않습니다(기획서 4.1 계층별 책임).
 *
 * 이 클래스가 담당하는 API 4개는 Notion API 명세서(카테고리: auth) 그대로입니다.
 * - POST /api/auth/signup  : 회원가입
 * - POST /api/auth/login   : 이메일 로그인
 * - POST /api/auth/refresh : accessToken·refreshToken 재발급(회전)
 * - POST /api/auth/logout  : 로그아웃(서버측 refreshToken 전부 폐기)
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signup(@Valid @RequestBody SignUpRequest request) {
        SignUpResponse response = authService.signup(request);
        // [각주] 201 Created : "새 자원(회원)을 성공적으로 만들었다"는 의미의 HTTP 상태코드
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        RefreshResponse response = authService.refresh(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 로그아웃. SecurityConfig 설정상 이 경로는 permitAll이 아니므로,
     * 유효한 accessToken 없이 호출하면 JwtAuthenticationFilter -> CustomAuthenticationEntryPoint를 거쳐
     * 자동으로 401이 반환됩니다.
     *
     * [각주 Q] Authentication authentication 파라미터: 별도 어노테이션 없이 컨트롤러 메서드에
     * Authentication 타입을 선언하면, 스프링이 "지금 이 요청을 보낸 로그인 사용자 정보"를 자동으로
     * 넣어줍니다. JwtAuthenticationFilter가 SecurityContext에 넣어둔 값(userNo, [각주 G] 참고)이
     * 여기 principal 자리에 그대로 들어있습니다.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication) {
        Integer userNo = (Integer) authentication.getPrincipal();
        authService.logout(userNo);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}

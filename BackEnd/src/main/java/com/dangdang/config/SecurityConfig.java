package com.dangdang.config;

import com.dangdang.security.CustomAuthenticationEntryPoint;
import com.dangdang.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * [각주 I] Spring Security 설정의 핵심 파일입니다.
 * "어떤 URL은 로그인 없이 열어줄지", "비밀번호는 어떻게 암호화할지", "우리가 만든 JWT 필터를
 * 언제 실행할지"를 여기서 한 번에 정의합니다.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                           CustomAuthenticationEntryPoint authenticationEntryPoint) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    // [각주] BCrypt : 비밀번호를 "되돌릴 수 없는" 방식으로 암호화(해싱)하는 표준 알고리즘입니다.
    // 같은 비밀번호라도 매번 다른 결과가 나오지만(salt 덕분), matches()로 원문과 비교 검증은 가능합니다.
    // -> DB가 유출되어도 원래 비밀번호를 바로 알아낼 수 없습니다.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // REST API + JWT 조합에서는 CSRF(웹 폼 위조 방지) 보호가 필요 없어 끕니다.
                .csrf(csrf -> csrf.disable())

                // [각주] STATELESS : 서버가 세션(HttpSession)을 만들지 않겠다는 뜻입니다.
                // 로그인 상태는 오직 매 요청마다 실려오는 JWT로만 판단합니다.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // 회원가입/로그인/토큰재발급은 아직 로그인 전이므로 누구나 접근 가능해야 합니다.
                        .requestMatchers(
                                "/api/auth/signup",
                                "/api/auth/login",
                                "/api/auth/refresh"
                        ).permitAll()
                        // [각주] Swagger UI 자체와, 그 화면이 내부적으로 불러오는 API 명세(JSON) 경로.
                        // 이것도 로그인 없이 열려야 테스트 화면 자체에 들어갈 수 있습니다.
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()
                        // 그 외 모든 요청(예: /api/auth/logout)은 유효한 accessToken이 있어야 합니다.
                        .anyRequest().authenticated()
                )

                // 인증 실패 시 우리 공통 JSON 형식으로 응답하도록 연결
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint))

                // 스프링 시큐리티의 기본 필터보다 "먼저" 우리 JWT 필터가 실행되도록 순서를 지정합니다.
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

package com.dangdang.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * [각주 G] "필터(Filter)"란?
 * 컨트롤러에 요청이 도착하기 "전"에 가로채서 공통 작업을 처리하는 부품입니다.
 * 이 필터는 매 요청마다 Authorization 헤더의 JWT를 꺼내 검증하고, 유효하면
 * "이 요청은 userNo=n 인 로그인된 사용자가 보낸 것"이라고 스프링 시큐리티에 등록합니다.
 * (OncePerRequestFilter = 요청 하나당 딱 한 번만 실행되도록 보장해주는 스프링 클래스)
 *
 * 이 필터는 토큰이 없거나 잘못돼도 예외를 던지지 않고 그냥 통과시킵니다.
 * 최종적으로 "인증이 꼭 필요한지"는 SecurityConfig의 authorizeHttpRequests 설정이 판단합니다.
 * (permitAll 경로는 로그인 없이 통과, 그 외 경로는 인증 정보가 없으면 401)
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;

    public JwtAuthenticationFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String token = resolveToken(request);

        if (token != null && jwtProvider.validateToken(token) && jwtProvider.isAccessToken(token)) {
            Integer userNo = jwtProvider.getUserNo(token);

            // principal 자리에 userNo를 담아둡니다. 컨트롤러에서는 나중에
            // SecurityContextHolder.getContext().getAuthentication().getPrincipal() 로 꺼낼 수 있습니다.
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userNo, null, List.of());

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(HEADER);
        if (header != null && header.startsWith(PREFIX)) {
            return header.substring(PREFIX.length());
        }
        return null;
    }
}

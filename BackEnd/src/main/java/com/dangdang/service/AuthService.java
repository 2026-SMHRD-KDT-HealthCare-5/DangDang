package com.dangdang.service;

import com.dangdang.dto.request.LoginRequest;
import com.dangdang.dto.request.RefreshRequest;
import com.dangdang.dto.request.SignUpRequest;
import com.dangdang.dto.response.RefreshResponse;
import com.dangdang.dto.response.SignUpResponse;
import com.dangdang.dto.response.TokenResponse;
import com.dangdang.entity.ActivityLevel;
import com.dangdang.entity.DiagnosisGroup;
import com.dangdang.entity.RefreshToken;
import com.dangdang.entity.User;
import com.dangdang.exception.BusinessException;
import com.dangdang.exception.ErrorCode;
import com.dangdang.repository.RefreshTokenRepository;
import com.dangdang.repository.UserRepository;
import com.dangdang.security.JwtProvider;
import com.dangdang.security.TokenHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * [각주 J] "서비스(Service)"란?
 * 실제 비즈니스 로직(업무 규칙)이 들어가는 계층입니다.
 * Controller는 HTTP 요청/응답만 다루고, Repository는 DB 접근만 다루며,
 * "이메일이 중복이면 막는다", "비밀번호가 틀리면 로그인 거부한다" 같은 판단은 여기서 합니다.
 */
@Service
@RequiredArgsConstructor // final 필드를 생성자로 자동 주입해주는 Lombok 어노테이션 (의존성 주입)
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    /**
     * 회원가입.
     * [각주] @Transactional : 이 메서드 안의 DB 작업들을 "하나의 묶음"으로 처리합니다.
     * 중간에 예외가 발생하면 지금까지의 변경사항을 전부 되돌립니다(롤백). (기획서 8.2 트랜잭션 정책)
     */
    @Transactional
    public SignUpResponse signup(SignUpRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        // [각주 P] "거의 안함/주 1~2회/주 3~5회/매일" 같은 화면 문구를 1~4 코드로 바꿔서 저장합니다.
        // activityLevel이 안 넘어온 경우(선택 입력)는 매핑을 건너뜁니다.
        Integer activityLevelCode = request.activityLevel() == null
                ? null
                : ActivityLevel.fromRawText(request.activityLevel()).getCode();

        // [각주 V] activityLevel과 동일한 패턴: 값이 오면 셋 중 하나인지 검증하고,
        // 안 왔으면(선택 입력) null로 둡니다.
        String diagnosisGroup = request.diagnosisGroup() == null
                ? null
                : DiagnosisGroup.fromRawText(request.diagnosisGroup()).getApiValue();

        User user = User.builder()
                .email(request.email())
                .password(encodedPassword)
                .nickname(request.nickname())
                .gender(request.gender())
                .birthDate(request.birthDate())
                .height(request.height())
                .weight(request.weight())
                .hba1c(request.hba1c())
                .activityLevel(activityLevelCode)
                .diagnosisGroup(diagnosisGroup)
                .targetGlucose(request.targetGlucose())
                .build();

        User savedUser = userRepository.save(user);

        return new SignUpResponse(savedUser.getUserNo());
    }

    /**
     * 이메일 로그인. 성공 시 access/refresh 토큰을 발급하고,
     * refreshToken은 해시로 변환해 refresh_token 테이블에도 기록합니다(아래 [각주 O] 참고).
     * 저장까지 하므로 더 이상 읽기 전용(readOnly)이 아닙니다.
     */
    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                // [각주] 보안 관례: "이메일이 없음"과 "비밀번호가 틀림"을 서버가 구분해서 알려주지 않습니다.
                // 둘 다 같은 메시지(INVALID_CREDENTIALS)로 응답해야 공격자가 "가입된 이메일 목록"을
                // 하나씩 캐낼(추측할) 수 없습니다.
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = jwtProvider.createAccessToken(user.getUserNo());
        String refreshToken = jwtProvider.createRefreshToken(user.getUserNo());

        saveRefreshTokenRecord(user.getUserNo(), refreshToken);

        return new TokenResponse(accessToken, refreshToken);
    }

    /**
     * [각주 O] refreshToken 검증 + 회전(rotation).
     *
     * 예전(무상태 전용) 방식은 JWT 서명·만료만 확인하고 새 accessToken만 내줬습니다.
     * 이제는 DB(refresh_token 테이블) 기록까지 확인해서 세 가지를 추가로 막습니다.
     *  1) 로그아웃된 토큰 재사용 차단 — logout()이 revoked=true로 표시해두면 여기서 걸립니다.
     *  2) 이미 재발급에 한 번 쓰인(회전된) 토큰의 "재사용" 탐지 — 정상 사용자라면 한 refreshToken은
     *     한 번만 refresh에 쓰이고 그 즉시 폐기·교체됩니다. 폐기된 토큰이 또 들어온다는 건 누군가
     *     그 토큰을 훔쳐서 같이 쓰고 있다는 강한 신호이므로, 그 사용자의 모든 토큰을 강제로 끊습니다.
     *  3) 탈퇴 등으로 사라진 회원의 토큰 재사용 차단.
     * 그리고 재발급 시 refreshToken도 함께 새로 발급합니다(회전) — 그래야 위 2번 탐지가 의미가 있습니다.
     */
    @Transactional
    public RefreshResponse refresh(RefreshRequest request) {
        String refreshToken = request.refreshToken();

        if (!jwtProvider.validateToken(refreshToken) || !jwtProvider.isRefreshToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        String tokenHash = TokenHasher.sha256(refreshToken);
        RefreshToken saved = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));

        if (saved.isRevoked()) {
            // 이미 폐기(로그아웃 또는 이전 회전)된 토큰이 다시 들어옴 = 탈취 의심.
            // 해당 사용자의 살아있는 토큰을 전부 강제 폐기해 피해를 최소화합니다.
            refreshTokenRepository.revokeAllByUserNo(saved.getUserNo());
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        if (saved.isExpired()) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        Integer userNo = saved.getUserNo();
        if (!userRepository.existsById(userNo)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        saved.revoke(); // 방금 쓴 refreshToken은 이 시점부로 폐기(1회용)

        String newAccessToken = jwtProvider.createAccessToken(userNo);
        String newRefreshToken = jwtProvider.createRefreshToken(userNo);
        saveRefreshTokenRecord(userNo, newRefreshToken);

        return new RefreshResponse(newAccessToken, newRefreshToken);
    }

    /**
     * [각주 K] 로그아웃.
     * refresh_token 테이블이 생기기 전에는 "서버가 토큰을 무효화할 방법이 없다"는 한계가 있었지만,
     * 이제는 이 사용자의 살아있는 refreshToken을 DB에서 모두 revoked=true로 표시해 실제로 끊습니다.
     * accessToken 자체는 여전히 무상태라 자기 만료시간(최대 1시간)까지는 유효하지만, 그 accessToken으로
     * 재발급(refresh)은 더 이상 불가능해지므로 최대 1시간 내로 완전히 로그아웃 상태가 됩니다.
     * userNo는 AuthController에서 SecurityContext(로그인 필터가 검증한 accessToken)로부터 꺼내옵니다.
     */
    @Transactional
    public void logout(Integer userNo) {
        refreshTokenRepository.revokeAllByUserNo(userNo);
    }

    /** login()/refresh() 공통: 새로 발급한 refreshToken을 해시로 변환해 기록으로 남깁니다. */
    private void saveRefreshTokenRecord(Integer userNo, String rawRefreshToken) {
        RefreshToken entity = RefreshToken.builder()
                .userNo(userNo)
                .tokenHash(TokenHasher.sha256(rawRefreshToken))
                .expiresAt(jwtProvider.getExpiration(rawRefreshToken))
                .build();

        refreshTokenRepository.save(entity);
    }
}

package com.dangdang.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * [각주 FF] GET /api/users/me 응답입니다 (노션 "마이페이지 - 내 정보" 명세).
 * activityLevel은 DB의 정수(1~4) 대신 화면 문구("거의 안함" 등)로 변환해서 내려줍니다
 * (회원가입 때 받는 형식과 통일 — ActivityLevel.fromCode().getRawText() 참고).
 * diagnosisGroup(진단군)은 명세에 없어서 포함하지 않았습니다.
 *
 * @lastModified 2026-08-21
 */
public record UserInfoResponse(
        String nickname,
        String email,
        String gender,
        LocalDate birthDate,
        BigDecimal height,
        BigDecimal weight,
        BigDecimal hba1c,
        String activityLevel,
        Integer targetGlucose,
        boolean notificationEnabled,
        LocalDateTime joinedAt
) {
}

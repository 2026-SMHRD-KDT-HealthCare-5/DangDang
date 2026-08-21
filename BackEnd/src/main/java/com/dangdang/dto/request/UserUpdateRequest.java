package com.dangdang.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * [각주 FG] PATCH /api/users/me 요청 바디입니다. 전부 선택 입력(null 허용)이고,
 * 보낸 필드만 부분 수정됩니다 — User.updateProfile() 참고. email/password는 여기서
 * 아예 받지 않습니다(수정 불가 항목, 노션 명세: "이메일은 로그인 식별자라 변경 불가").
 * activityLevel은 회원가입과 동일하게 화면 문구("거의 안함" 등) 그대로 보내주면 됩니다.
 *
 * @lastModified 2026-08-21
 */
public record UserUpdateRequest(
        String nickname,
        String gender,
        LocalDate birthDate,
        BigDecimal height,
        BigDecimal weight,
        BigDecimal hba1c,
        String activityLevel,
        Integer targetGlucose
) {
}

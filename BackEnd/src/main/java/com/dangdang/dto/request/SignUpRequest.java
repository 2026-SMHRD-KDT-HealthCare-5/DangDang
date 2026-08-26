package com.dangdang.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * [각주 C] "DTO(Data Transfer Object)"란?
 * 클라이언트(안드로이드 앱)와 서버가 주고받는 JSON 데이터의 모양을 정의한 클래스입니다.
 * DB 테이블과 1:1인 엔티티(User)를 요청/응답에 그대로 쓰지 않는 이유는,
 * 비밀번호 같은 민감한 내부 정보가 실수로 노출되는 걸 막고, 화면/API마다 필요한 값만
 * 정확히 주고받기 위해서입니다.
 *
 * record는 자바 문법으로, 값을 담기만 하는 클래스를 짧게 선언할 때 씁니다.
 * (getter가 자동 생성되며 필드는 모두 불변입니다)
 *
 * 회원가입 요청 (POST /api/auth/signup)
 * - email/password/nickname 만 필수이고, 나머지 건강정보는 DB에서도 NULL 허용이라 선택 입력입니다.
 * - 어노테이션(@NotBlank 등)은 "Bean Validation" 규칙으로, 값이 오기 전에 서버가 자동으로 검사해줍니다.
 */
public record SignUpRequest(

        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(max = 30, message = "닉네임은 20자 이하여야 합니다.")
        String nickname,

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @Size(max = 100)
        String email,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, max = 12, message = "비밀번호는 8자 이상 12 이하여야 합니다.")
        String password,

        // 프론트 Gender enum(Male/Female 등)의 문자열 표현을 그대로 저장합니다. (예: "Male")
        @Size(max = 10)
        String gender,

        LocalDate birthDate,

        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal height,

        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal weight,

        // 당화혈색소(HbA1c)
        BigDecimal hba1c,

        /*
         * [각주 P] 평소 활동량. 프론트 화면에는 "거의 안함 / 주 1~2회 / 주 3~5회 / 매일" 문구로 노출되고,
         * 여기서는 그 문자열을 그대로 받습니다. DB에는 정수(1=하/2=중/3=상)로 저장해야 하므로,
         * 실제 매핑·검증은 AuthService에서 ActivityLevel.fromRawText()가 담당합니다.
         * (문자열이 셋 중 하나가 아니면 400 INVALID_ACTIVITY_LEVEL)
         */
        String activityLevel,

        /*
         * [각주 V] 당뇨 진단군. "건강군" / "전당뇨" / "2형당뇨" 문자열을 그대로 받습니다.
         * 검증은 AuthService에서 DiagnosisGroup.fromRawText()가 담당합니다.
         * (문자열이 셋 중 하나가 아니면 400 INVALID_DIAGNOSIS_GROUP)
         */
        String diagnosisGroup,

        // 식후 2시간 목표 혈당 (mg/dL, 정수)
        Integer targetGlucose
) {
}

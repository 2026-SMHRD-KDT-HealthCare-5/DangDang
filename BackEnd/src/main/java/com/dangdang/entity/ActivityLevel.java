package com.dangdang.entity;

import com.dangdang.exception.BusinessException;
import com.dangdang.exception.ErrorCode;

import java.util.Arrays;

/**
 * [각주 P] 평소 활동량을 나타내는 값입니다.
 * 안드로이드 화면에는 "주 1~3회 / 주 3~5회 / 주 5~7회" 라는 문자열로 보이지만,
 * DB(users.activity_level)에는 정수(1/2/3)만 저장하기로 스키마가 확정되어 있습니다.
 * 그래서 이 enum이 "화면 문구 ↔ 저장할 정수"를 이어주는 다리 역할을 합니다.
 *
 * code  1 = 하(下)  ← "주 1~3회"
 * code  2 = 중(中)  ← "주 3~5회"
 * code  3 = 상(上)  ← "주 5~7회"
 */
public enum ActivityLevel {

    LOW(1, "하", "주 1~3회"),
    MEDIUM(2, "중", "주 3~5회"),
    HIGH(3, "상", "주 5~7회");

    private final int code;
    private final String label;   // 하/중/상
    private final String rawText; // 프론트에서 그대로 넘어오는 문자열

    ActivityLevel(int code, String label, String rawText) {
        this.code = code;
        this.label = label;
        this.rawText = rawText;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    /**
     * 프론트에서 온 원문 문자열("주 1~3회" 등)로 알맞은 enum 값을 찾습니다.
     * 셋 중 어디에도 해당하지 않으면(오타, 새 옵션 추가 누락 등) 400 에러로 명확히 알려줍니다.
     */
    public static ActivityLevel fromRawText(String rawText) {
        return Arrays.stream(values())
                .filter(level -> level.rawText.equals(rawText))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_ACTIVITY_LEVEL));
    }

    /** DB에서 꺼낸 정수(code)로 다시 enum을 찾을 때 사용 (추후 /api/users/me 응답에 활용 가능). */
    public static ActivityLevel fromCode(int code) {
        return Arrays.stream(values())
                .filter(level -> level.code == code)
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_ACTIVITY_LEVEL));
    }
}

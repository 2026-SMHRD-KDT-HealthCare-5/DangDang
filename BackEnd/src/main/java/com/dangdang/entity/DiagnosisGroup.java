package com.dangdang.entity;

import com.dangdang.exception.BusinessException;
import com.dangdang.exception.ErrorCode;

import java.util.Arrays;

/**
 * [각주 V] 당뇨 진단군을 나타내는 값입니다.
 * 값이 두 종류로 나뉩니다 — 안드로이드가 실제로 보내는 문구(rawText)와, DB 저장 +
 * FastAPI 전달에 쓰이는 값(apiValue)이 서로 다르기 때문입니다.
 *
 * 안드로이드 쪽 표기: "정상" / "전당뇨" / "제2형당뇨"
 * FastAPI/모델 쪽 표기: "건강군" / "전당뇨" / "2형당뇨" (DIAGNOSIS_GROUPS, core/config.py)
 *
 * ⚠️ "정상"->"건강군", "제2형당뇨"->"2형당뇨" 변환이 특히 중요합니다. LightGBM 예측 모델이
 * 학습될 때 원-핫 인코딩 컬럼 이름이 정확히 'group_2형당뇨'(제 없음)로 고정되어 있어서
 * (모델 재학습 없이는 못 바꿈), 안드로이드가 보낸 문구를 그대로 FastAPI에 넘기면 "모르는
 * 진단군"으로 보고 조용히 "건강군" 기본값으로 처리해버립니다.
 */
public enum DiagnosisGroup {

    HEALTHY("정상", "건강군"),
    PREDIABETES("전당뇨", "전당뇨"),
    TYPE2("제2형당뇨", "2형당뇨");

    private final String rawText;   // 안드로이드가 그대로 보내는 문구
    private final String apiValue;  // DB(users.diagnosis_group) 저장 + FastAPI 전달용

    DiagnosisGroup(String rawText, String apiValue) {
        this.rawText = rawText;
        this.apiValue = apiValue;
    }

    public String getApiValue() {
        return apiValue;
    }

    /**
     * 프론트에서 온 원문 문자열("제2형당뇨" 등)이 유효한 값인지 검증하고,
     * 저장/FastAPI 전달용 값(apiValue)으로 바로 변환해서 돌려줍니다.
     * 셋 중 어디에도 해당하지 않으면 400 에러로 명확히 알려줍니다.
     */
    public static DiagnosisGroup fromRawText(String rawText) {
        return Arrays.stream(values())
                .filter(group -> group.rawText.equals(rawText))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_DIAGNOSIS_GROUP));
    }
}

package com.dangdang.entity;

import com.dangdang.exception.BusinessException;
import com.dangdang.exception.ErrorCode;

import java.util.Arrays;

/**
 * [각주 AX] custom_food.source 컬럼(varchar(10))에 들어가는 값입니다.
 * DiagnosisGroup과 동일한 이유로 "자바 상수 이름(영문)"과 "DB 저장값(한글)"을 분리했습니다.
 *
 * AI_ANALYSIS -> "AI추정"   : "틀려요, AI로 분석하기"를 눌러서 FastAPI reanalyze로 추정한 값
 * USER_INPUT  -> "사용자입력" : 사용자가 영양성분을 직접 타이핑해서 입력한 값
 *
 * [각주] (수정) 값을 "직접입력"에서 노션 명세("API 기본 명세서" 6.3절) 기준인 "사용자입력"으로
 * 바로잡았습니다. 자바 상수 이름도 DIRECT_INPUT -> USER_INPUT으로 맞춰서 dbValue와 뜻이
 * 바로 대응되게 했습니다.
 *
 * @lastModified 2026-08-18
 */
public enum CustomFoodSource {
    AI_ANALYSIS("AI추정"),
    USER_INPUT("사용자입력");

    private final String dbValue;

    CustomFoodSource(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    /**
     * DiagnosisGroup.fromRawText()와 동일한 패턴 — 문자열이 "AI추정"/"사용자입력" 둘 중
     * 하나인지 검증하고 그대로 돌려줍니다. IntakeLogService.confirmIntake()에서 씁니다.
     */
    public static String validateDbValue(String dbValue) {
        return Arrays.stream(values())
                .filter(candidate -> candidate.dbValue.equals(dbValue))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CUSTOM_FOOD_SOURCE))
                .dbValue;
    }
}

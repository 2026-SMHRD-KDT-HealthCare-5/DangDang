package com.dangdang.dto.request;

/**
 * [각주 X] POST /api/intake-logs/preglucose 요청 바디.
 * preGlucose는 선택 입력입니다 — 사용자가 "식전 혈당을 모른다"고 답하면 이 필드를
 * 아예 생략하거나 null로 보냅니다. 그러면 서버가 HbA1c 구간 기본값을 대신 계산해줍니다.
 */
public record PreGlucoseRequest(
        Integer preGlucose
) {
}

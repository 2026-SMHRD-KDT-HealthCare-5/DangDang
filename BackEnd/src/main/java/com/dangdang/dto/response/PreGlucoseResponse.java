package com.dangdang.dto.response;

/**
 * [각주 X] POST /api/intake-logs/preglucose 응답.
 *
 * preGlucose        : 이후 단계(recognize/최종확정)에서 실제로 쓰일 식전 혈당값.
 *                      사용자가 입력했으면 그 값 그대로, 안 했으면 서버가 계산한 기본값.
 * preGlucoseDefault : 기본값이 "적용된 경우에만" 값이 채워지고, 사용자가 직접 입력했다면 null.
 *                      (노션 "식전 혈당 입력" 명세 기준 — 프론트가 "기본값이 적용됐어요" 안내를
 *                      보여줄지 말지 이 필드 하나로 판단할 수 있게 하기 위함)
 */
public record PreGlucoseResponse(
        Integer preGlucose,
        Integer preGlucoseDefault
) {
}

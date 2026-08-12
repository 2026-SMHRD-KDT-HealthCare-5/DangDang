package com.dangdang.dto.response;

/** 회원가입 성공 응답 (201 Created) — 명세: "성공: 201 Created + user_no" */
public record SignUpResponse(
        Integer userNo
) {
}

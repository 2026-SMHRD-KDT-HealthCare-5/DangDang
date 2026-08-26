package com.dangdang.controller;

import com.dangdang.dto.request.NotificationUpdateRequest;
import com.dangdang.dto.request.UserUpdateRequest;
import com.dangdang.dto.response.UserInfoResponse;
import com.dangdang.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * [각주 FM] 내 정보 조회/수정, 알림 설정 API (카테고리: users).
 *
 * @lastModified 2026-08-21
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** [각주 FN] GET /api/users/me — 내 정보 조회. */
    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getMyInfo(Authentication authentication) {
        Integer userNo = (Integer) authentication.getPrincipal();
        return ResponseEntity.ok(userService.getMyInfo(userNo));
    }

    /** [각주 FO] PATCH /api/users/me — 내 정보 부분 수정. */
    @PatchMapping("/me")
    public ResponseEntity<UserInfoResponse> updateMyInfo(
            Authentication authentication,
            @RequestBody UserUpdateRequest request
    ) {
        Integer userNo = (Integer) authentication.getPrincipal();
        return ResponseEntity.ok(userService.updateMyInfo(userNo, request));
    }

    /** [각주 FP] PATCH /api/users/me/notification — 알림 설정 on/off. */
    @PatchMapping("/me/notification")
    public ResponseEntity<UserInfoResponse> updateNotification(
            Authentication authentication,
            @RequestBody NotificationUpdateRequest request
    ) {
        Integer userNo = (Integer) authentication.getPrincipal();
        return ResponseEntity.ok(userService.updateNotification(userNo, request));
    }
}

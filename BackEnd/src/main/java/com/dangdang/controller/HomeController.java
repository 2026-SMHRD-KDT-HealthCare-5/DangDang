package com.dangdang.controller;

import com.dangdang.dto.response.HomeResponse;
import com.dangdang.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * [각주 FE] 홈 화면 집계 API. 화면 구성 요소를 한 번에 반환하는 GET /api/home 하나뿐입니다.
 *
 * @lastModified 2026-08-21
 */
@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    @GetMapping
    public ResponseEntity<HomeResponse> getHome(Authentication authentication) {
        Integer userNo = (Integer) authentication.getPrincipal();
        return ResponseEntity.ok(homeService.getHome(userNo));
    }
}

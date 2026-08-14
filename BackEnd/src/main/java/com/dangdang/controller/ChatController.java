package com.dangdang.controller;

import com.dangdang.dto.request.ChatRequest;
import com.dangdang.dto.response.ChatHistoryResponse;
import com.dangdang.dto.response.ChatResponse;
import com.dangdang.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * [각주 AH] 당당이 챗봇(일반 대화) 관련 API.
 * 음식 인식/재분석은 IntakeLogController가 담당하고, 여긴 순수 텍스트 대화 + 이력 조회만 다룹니다.
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * 당당이와 대화하기. FastAPI(RAG+Gemini)의 답변을 받아 그 자리에서 ai_chat에 저장하고 돌려줍니다.
     */
    @PostMapping
    public ResponseEntity<ChatResponse> chat(
            Authentication authentication,
            @RequestBody ChatRequest request
    ) {
        Integer userNo = (Integer) authentication.getPrincipal();
        ChatResponse response = chatService.chat(userNo, request.message());
        return ResponseEntity.ok(response);
    }

    /**
     * 하루 단위 대화 이력 조회. date 생략 시 오늘(KST) 기준.
     * [각주 AI] @DateTimeFormat(iso = DATE) : 쿼리파라미터로 온 "2026-08-10" 같은 문자열을
     * 스프링이 자동으로 LocalDate로 변환해줍니다. 형식이 틀리면(예: "2026/08/10") 400 에러가 납니다.
     */
    @GetMapping("/history")
    public ResponseEntity<ChatHistoryResponse> history(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        Integer userNo = (Integer) authentication.getPrincipal();
        ChatHistoryResponse response = chatService.getHistory(userNo, date);
        return ResponseEntity.ok(response);
    }
}

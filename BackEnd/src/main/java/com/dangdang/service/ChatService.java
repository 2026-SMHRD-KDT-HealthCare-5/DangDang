package com.dangdang.service;

import com.dangdang.dto.response.ChatHistoryResponse;
import com.dangdang.dto.response.ChatResponse;
import com.dangdang.entity.AiChat;
import com.dangdang.entity.ChatType;
import com.dangdang.entity.User;
import com.dangdang.exception.BusinessException;
import com.dangdang.exception.ErrorCode;
import com.dangdang.repository.AiChatRepository;
import com.dangdang.repository.UserRepository;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * [각주 AF] 당당이 챗봇의 "일반 대화"와 "대화 이력 조회"를 담당합니다.
 * IntakeLogService(recognize/reanalyze)와 같은 패턴 — Spring이 FastAPI(/rag/chat)를
 * 대신 호출하고, 그 결과를 ai_chat 테이블에 저장하는 것까지 여기서 합니다.
 * (recognize와 달리 여긴 "저장까지"가 이 서비스의 역할입니다 — 대화는 확인 절차 없이 그 자리에서 확정이라서)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    // [각주 AL] 실제 DB(ai_chat.ai_message VARCHAR(1000))가 강제하는 한도입니다.
    // Gemini 답변이 이보다 길면 INSERT 자체가 예외로 죽으므로, 저장 직전에 미리 잘라냅니다.
    private static final int AI_MESSAGE_MAX_LENGTH = 1000;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final AiChatRepository aiChatRepository;

    @Value("${fastapi.base-url}")
    private String fastApiBaseUrl;

    @Value("${fastapi.internal-api-key}")
    private String internalApiKey;

    /**
     * POST /api/chat. 노션 명세: user_message/ai_message 둘 다 채워서 1레코드로 저장,
     * chat_type=TEXT, cardData 없음(일반 대화는 카드가 아니라서).
     */
    @Transactional
    public ChatResponse chat(Integer userNo, String message) {
        User user = userRepository.findById(userNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String reply = callFastApiChat(userNo, message, user.getDiagnosisGroup());

        AiChat aiChat = AiChat.builder()
                .userNo(userNo)
                .userMessage(truncate(message, 500))
                .aiMessage(truncate(reply, AI_MESSAGE_MAX_LENGTH))
                .chatType(ChatType.TEXT)
                .build();
        AiChat saved = aiChatRepository.save(aiChat);

        return new ChatResponse(saved.getAiChatNo(), saved.getAiMessage(), saved.getChatType());
    }

    /**
     * GET /api/chat/history?date=YYYY-MM-DD. date 생략 시 "오늘"(KST 기준).
     * 노션 명세: 반개구간 [date 00:00:00, date+1일 00:00:00), chattedAt 오름차순.
     */
    @Transactional(readOnly = true)
    public ChatHistoryResponse getHistory(Integer userNo, LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now(KST);
        LocalDateTime start = targetDate.atStartOfDay();
        LocalDateTime endExclusive = targetDate.plusDays(1).atStartOfDay();

        List<AiChat> records = aiChatRepository
                .findByUserNoAndChattedAtGreaterThanEqualAndChattedAtLessThanOrderByChattedAtAsc(
                        userNo, start, endExclusive);

        List<ChatHistoryResponse.ChatMessage> messages = records.stream()
                .map(this::toMessage)
                .toList();

        return new ChatHistoryResponse(targetDate.toString(), messages);
    }

    /**
     * DB 컬럼 길이 제한(VARCHAR)을 넘지 않도록 자릅니다. null이면 빈 문자열로 바꾸는데,
     * ai_message는 DB에서 NOT NULL이라 null을 그대로 넣으면 INSERT가 예외로 죽기 때문입니다.
     */
    private String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        return text.length() > maxLength ? text.substring(0, maxLength) : text;
    }

    private ChatHistoryResponse.ChatMessage toMessage(AiChat aiChat) {
        return new ChatHistoryResponse.ChatMessage(
                aiChat.getAiChatNo(),
                aiChat.getUserMessage(),
                aiChat.getAiMessage(),
                aiChat.getChatType(),
                parseCardData(aiChat.getCardData()),
                aiChat.getChattedAt()
        );
    }

    /** cardData는 DB에 문자열로 저장돼있어서, 응답에 진짜 JSON 객체로 내려보내려면 파싱이 필요합니다. */
    private JsonNode parseCardData(String cardData) {
        if (cardData == null || cardData.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readTree(cardData);
        } catch (JsonProcessingException e) {
            // 저장된 값이 깨져있어도 이력 조회 자체는 실패하면 안 되므로 이 카드만 null 처리하고 넘어갑니다.
            log.error("cardData 파싱 실패 (aiChatNo 조회 중): {}", e.getMessage());
            return null;
        }
    }

    private String callFastApiChat(Integer userNo, String message, String diagnosisGroup) {
        FastApiChatRequest requestBody = new FastApiChatRequest(userNo, message, diagnosisGroup);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // [각주 W]와 동일 — FastAPI core/security.py(verify_internal_api_key)가 이 헤더를 검사합니다.
        headers.set("X-Internal-Api-Key", internalApiKey);
        HttpEntity<FastApiChatRequest> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            FastApiChatResponse response = restTemplate.postForObject(
                    fastApiBaseUrl + "/rag/chat", requestEntity, FastApiChatResponse.class);
            return response != null ? response.reply() : null;
        } catch (RestClientException e) {
            log.error("FastAPI 호출 실패 (chat): {}", e.getMessage());
            throw new BusinessException(ErrorCode.AI_SERVER_ERROR);
        }
    }

    /**
     * FastAPI schemas/chat.py의 ChatRequest와 맞춘 내부 전용 요청 형식입니다.
     * [각주 AG] FastAPI(Python/pydantic)는 스네이크케이스(user_no)를 쓰고 자바 필드는
     * 카멜케이스(userNo)를 쓰기 때문에, @JsonProperty로 실제 전송될 JSON 키 이름을
     * 명시적으로 맞춰줘야 합니다 — 안 맞추면 FastAPI가 그 필드를 못 찾아 422 에러를 냅니다.
     */
    private record FastApiChatRequest(
            @JsonProperty("user_no") Integer userNo,
            String message,
            @JsonProperty("diagnosis_group") String diagnosisGroup
    ) {
    }

    /** FastAPI schemas/chat.py의 ChatResponse({ "reply": "..." })와 맞춘 내부 전용 응답 형식입니다. */
    private record FastApiChatResponse(String reply) {
    }
}

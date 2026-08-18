package com.dangdang.client;

import com.dangdang.exception.BusinessException;
import com.dangdang.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * [각주 BH] FastAPI(AI 서버) 내부 호출을 한 곳에 모아둔 공통 클라이언트입니다.
 *
 * 지금까지는 IntakeLogService/ChatService가 각자 RestTemplate + X-Internal-Api-Key 헤더 +
 * 에러 처리를 따로따로 들고 있었는데, 이 클래스는 그 중복을 새로 만드는 곳(RecognizeProxyService)
 * 부터 줄이기 위해 만들었습니다. 기존 IntakeLogService.recognizeFood()/predictPortion()은
 * 이번에 건드리지 않았습니다(요청 안 하신 리팩터링이라 그대로 뒀습니다) — 나중에 원하시면
 * 그쪽도 이 클래스를 쓰도록 정리할 수 있습니다.
 *
 * @lastModified 2026-08-18
 */
@Slf4j
@Component
public class FastApiClient {

    private final RestTemplate restTemplate;

    @Value("${fastapi.base-url}")
    private String fastApiBaseUrl;

    @Value("${fastapi.internal-api-key}")
    private String internalApiKey;

    public FastApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /** JSON 바디로 FastAPI를 호출합니다 (예: /rag/intake-logs/predict). */
    public <T> T postJson(String path, Object requestBody, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Internal-Api-Key", internalApiKey);
        HttpEntity<Object> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            return restTemplate.postForObject(fastApiBaseUrl + path, requestEntity, responseType);
        } catch (RestClientException e) {
            log.error("FastAPI 호출 실패 ({}): {}", path, e.getMessage());
            throw new BusinessException(ErrorCode.AI_SERVER_ERROR);
        }
    }

    /** multipart/form-data 바디로 FastAPI를 호출합니다 (예: /rag/intake-logs/reanalyze, image 업로드 포함). */
    public <T> T postMultipart(String path, MultiValueMap<String, Object> body, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("X-Internal-Api-Key", internalApiKey);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            return restTemplate.postForObject(fastApiBaseUrl + path, requestEntity, responseType);
        } catch (RestClientException e) {
            log.error("FastAPI 호출 실패 ({}): {}", path, e.getMessage());
            throw new BusinessException(ErrorCode.AI_SERVER_ERROR);
        }
    }

    /**
     * MultipartFile(스프링이 받은 업로드 파일)을 RestTemplate이 multipart 바디에 실을 수 있는
     * 형태로 바꿔줍니다. IntakeLogService.toResource()와 동일한 목적/구현입니다.
     */
    public ByteArrayResource toResource(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            return new ByteArrayResource(bytes) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.AI_SERVER_ERROR);
        }
    }
}

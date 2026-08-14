package com.dangdang.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * [각주 R] RestTemplate: 스프링에서 "다른 서버에 HTTP 요청을 보낼 때" 쓰는 클라이언트입니다.
 * 우리 서비스에서는 FastAPI(AI 서버, :8000)를 내부적으로 호출할 때 이걸 씁니다.
 * (스프링 부트가 자동으로 만들어주는 빈이 아니라서, 이렇게 직접 @Bean으로 등록해야
 * @Autowired/생성자 주입으로 다른 곳(IntakeLogService 등)에서 가져다 쓸 수 있습니다.)
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

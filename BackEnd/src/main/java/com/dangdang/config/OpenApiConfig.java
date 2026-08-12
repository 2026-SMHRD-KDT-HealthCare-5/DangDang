package com.dangdang.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * [각주 R] Swagger UI 상단에 "Authorize" 버튼을 만들어주는 설정입니다.
 * /api/auth/logout 처럼 accessToken이 필요한 API를 테스트할 때, 여기 등록된 이름("bearerAuth")
 * 덕분에 Swagger UI에서 토큰을 한 번만 입력해두면 이후 모든 요청에 자동으로 Authorization 헤더가
 * 붙습니다. (로그인 API로 받은 accessToken을 "Bearer " 접두어 없이 값만 넣으면 됩니다)
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI dangdangOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("당당(DangDang) API")
                        .description("로그인/회원가입 (M1 단계)")
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}

// [각주 1] Gradle 빌드 스크립트: "이 프로젝트를 어떤 라이브러리로, 어떻게 빌드/실행할지" 정의하는 설정 파일입니다.
// 프론트엔드(Android)와 동일하게 Kotlin DSL(.kts) 문법을 사용합니다.
plugins {
    java
    id("org.springframework.boot") version "3.3.4"          // 스프링부트 실행/패키징 지원
    id("io.spring.dependency-management") version "1.1.6"    // 라이브러리 버전 자동 관리
}

group = "com.dangdang"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21) // 설계 문서 기준 Java 21 사용
    }
}

repositories {
    mavenCentral() // 라이브러리(의존성)를 내려받는 저장소
}

dependencies {
    // [각주 2] "spring-boot-starter-*" : 특정 기능 묶음을 한 번에 가져오는 패키지 세트입니다.
    implementation("org.springframework.boot:spring-boot-starter-web")        // REST API(컨트롤러) 개발
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")   // DB 접근(ORM, 아래 [각주 A] 참고)
    implementation("org.springframework.boot:spring-boot-starter-security")   // 로그인/인증·인가
    implementation("org.springframework.boot:spring-boot-starter-validation") // 요청값 검증(@NotBlank 등)

    // [각주 3] Flyway: DB 테이블 생성/변경 이력을 코드(SQL 파일)로 관리해주는 "DB 버전관리 도구"
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")

    runtimeOnly("org.postgresql:postgresql") // PostgreSQL 드라이버(자바가 DB와 통신하는 부품)

    // [각주 4] JWT(JSON Web Token) 발급/검증 라이브러리. 로그인 성공 시 발급하는 "출입증" 토큰을 만듭니다.
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    // [각주 5] Lombok: @Getter, @Builder 같은 어노테이션으로 반복적인 getter/setter 코드를 자동 생성해주는 도구
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // [각주 6] springdoc-openapi: 컨트롤러 코드를 스캔해서 Swagger UI(테스트용 웹 화면)를
    // 자동으로 만들어주는 라이브러리. 실행 후 http://localhost:8080/swagger-ui.html 로 접속.
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")

    // [각주 7] spring-dotenv: 프로젝트 루트의 .env 파일을 읽어서 자동으로 환경변수처럼 등록해주는
    // 라이브러리. 이게 있으면 터미널/IntelliJ 설정마다 따로 DB_PASSWORD 등을 입력할 필요 없이,
    // .env 파일 하나만 채워두면 gradlew든 IntelliJ 초록버튼이든 항상 동일하게 값을 읽습니다.
    implementation("me.paulschwarz:spring-dotenv:4.0.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

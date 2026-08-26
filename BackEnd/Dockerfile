# [각주 1] Dockerfile : "이 서버를 어떤 재료로, 어떤 순서로 조립해서 배포용 상자(이미지)로
# 만들지" 적어두는 설명서입니다. 이 파일 하나로 어떤 컴퓨터(EC2든 뭐든)에서도 항상 똑같은
# 방식으로 Spring Boot 서버를 빌드/실행할 수 있습니다.
#
# [각주 2] 멀티스테이지 빌드 : 아래처럼 FROM이 두 번 나오는 방식입니다.
#   1단계(build)  — 소스코드를 컴파일해서 실행 가능한 app.jar를 만드는 "공사장" 단계.
#                   Gradle, JDK 등 빌드 도구가 다 들어있어서 용량이 큽니다.
#   2단계(런타임) — 완성된 app.jar만 가져와서 실행만 하는 "완제품" 단계. 빌드 도구는
#                   필요 없으니 훨씬 가벼운 JRE(실행전용)만 담아서 이미지 용량을 줄입니다.
# 최종적으로 배포되는 건 2단계 이미지만이라, 서버 용량/시작 속도에 이득이 있습니다.

# ---------- 1단계: 빌드 ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# gradle 관련 파일부터 먼저 복사해서 의존성 캐시를 최대한 재사용합니다
# (소스코드만 바뀌고 build.gradle.kts가 안 바뀌면, 라이브러리를 매번 새로 안 받아도 됨)
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./
RUN chmod +x gradlew

COPY src src

# [각주 3] bootJar : Spring Boot가 실행 가능한 단일 jar(app.jar)로 묶어주는 Gradle 작업.
# -x test : 이미지 빌드할 때는 테스트를 다시 돌리지 않습니다(시간 절약 — 테스트는 배포 전에 로컬/CI에서 이미 돌렸다고 가정).
# --no-daemon : 컨테이너 안에서는 매번 새로 뜨는 일회성 빌드라 Gradle 데몬(백그라운드 상주 프로세스)이 필요 없습니다.
RUN ./gradlew bootJar -x test --no-daemon

# ---------- 2단계: 런타임(실제 배포되는 이미지) ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# 1단계에서 만든 jar 파일만 가져옵니다 (소스코드/Gradle 캐시는 여기 안 들어감)
COPY --from=build /app/build/libs/*.jar app.jar

# [각주 4] 이 컨테이너가 8080 포트를 쓴다는 걸 문서화하는 용도입니다(실제 포트 개방은
# docker-compose.yml의 ports/expose 설정이 담당 — EXPOSE 자체는 강제력이 없습니다).
EXPOSE 8080

# [각주 5] DB_URL/JWT_SECRET 같은 값들은 이 안에 안 넣습니다 — docker-compose.yml이
# env_file로 BackEnd/.env를 읽어서 "실행할 때" 컨테이너 환경변수로 주입해줍니다.
# 그래야 이미지 자체에는 비밀번호가 안 박히고, 서버(운영/개발)마다 다른 .env로 같은 이미지를 재사용할 수 있습니다.
ENTRYPOINT ["java", "-jar", "app.jar"]

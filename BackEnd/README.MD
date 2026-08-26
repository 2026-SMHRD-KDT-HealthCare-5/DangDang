# 당당(DangDang) 백엔드 — 최종 README

## 목차
1. 한눈에 보기
2. 전체 시스템 아키텍처
3. 폴더 구조
4. 기능별 API 목록
5. 로컬 실행 방법
6. 배포 방법 (Docker + Nginx + Cloudflare Tunnel / AWS EC2)
7. 인증 구조 (JWT + refresh_token 회전 + 단일 세션 정책)
8. 음식 인식/재분석/예측 흐름 (FastAPI 연동)
9. DB 관리 원칙 — 마이그레이션 파일을 만들지 않는 이유
10. 트러블슈팅 기록
11. 백엔드 초심자를 위한 용어 각주 모음
12. 다음 단계로 넘어갈 때 참고

---

## 1. 한눈에 보기

**당당**은 먹은 음식을 사진/텍스트로 입력하면 AI가 식후 혈당 상승량을 예측하고, 그 혈당을 낮추는 데 필요한 만큼만 걷도록 맞춤 걷기 미션을 제시하는 헬스케어 서비스입니다.

이 저장소(`BackEnd/`)는 그중 **Spring Boot 서버** 파트입니다. 안드로이드 앱은 이 서버만 호출하고, AI 추론(음식 인식·혈당 예측·챗봇)은 이 서버가 내부적으로 FastAPI 서버(`AIServer/`)를 호출해서 처리합니다.

| 항목 | 값 |
|---|---|
| 언어/프레임워크 | Java 21, Spring Boot 3.3.4 (Gradle Kotlin DSL) |
| DB | PostgreSQL (학원 캠퍼스 서버에서 호스팅, 팀 공용) |
| 인증 | JWT (accessToken + refreshToken 회전) |
| AI 연동 | FastAPI(:8000)를 내부 전용으로 호출 (Gemini API 기반) |
| API 문서 | Swagger UI (`/swagger-ui/index.html`) |
| 배포 | Docker Compose (Spring + FastAPI + Nginx) → Cloudflare Tunnel 또는 AWS EC2 |

---

## 2. 전체 시스템 아키텍처

```
Android 앱
   │  (HTTPS, 모든 요청은 Spring만 호출)
   ▼
Nginx (리버스 프록시, :80 — 유일한 외부 공개 진입점)
   │
   ▼
Spring Boot :8080  ←──(X-Internal-Api-Key 헤더)──→  FastAPI :8000
  · 모든 비즈니스 로직           · 음식 인식/재분석/예측/챗봇
  · 모든 DB 쓰기(회원/미션/식사기록)  · Gemini API 호출
  · JWT 인증                    · food_info 조회는 읽기 전용
   │                                   │
   ▼ (읽기/쓰기)                       ▼ (읽기 전용, food_info만)
        PostgreSQL (공유 DB, 학원 캠퍼스 서버)
```

**핵심 설계 원칙 (절대 깨지 않는 규칙)**
- 안드로이드 앱은 **Spring만** 호출한다. FastAPI는 외부에 노출되지 않고, Spring이 내부망(`X-Internal-Api-Key` 헤더로 인증)에서만 호출한다.
- **DB 쓰기는 Spring만** 한다. FastAPI는 `food_info`(식약처 영양성분 테이블) 조회만 하는 읽기 전용 접근이다.
- 배포 시에는 이 구조 그대로 앞단에 Nginx만 추가한다 (Nginx → Spring, FastAPI는 컨테이너 내부망에만 존재).

레포 루트의 `dangdang_system_architecture.pptx`에 이 구조를 슬라이드 한 장으로 정리해 두었습니다 (발표 자료용).

---

## 3. 폴더 구조

```
BackEnd/
├── build.gradle.kts / settings.gradle.kts
├── Dockerfile / .dockerignore
└── src/main/
    ├── java/com/dangdang/
    │   ├── DangdangServerApplication.java
    │   ├── config/            SecurityConfig, OpenApiConfig(Swagger), RestClientConfig
    │   ├── controller/        AuthController, UserController, IntakeLogController,
    │   │                      WalkMissionController, TeamController, RankingController,
    │   │                      ChatController, HomeController
    │   ├── service/           위 컨트롤러 1:1 대응 + RecognizeProxyService(FastAPI 중계)
    │   ├── client/            FastApiClient (FastAPI 내부 호출 전담)
    │   ├── repository/        Spring Data JPA 리포지토리 (엔티티당 1개)
    │   ├── entity/            User, RefreshToken, IntakeLog, CustomFood, FoodInfo,
    │   │                      WalkMission, Team, TeamMember, AiChat 등
    │   ├── dto/request, dto/response   요청/응답 JSON 모양
    │   ├── security/          JwtProvider, JwtAuthenticationFilter, TokenHasher
    │   ├── scheduler/         WalkMissionExpireScheduler (걷기 미션 자동 만료 배치)
    │   └── exception/         BusinessException, ErrorCode, GlobalExceptionHandler
    └── resources/
        └── application.yml   (DB 마이그레이션 SQL 파일은 두지 않음 — 9번 항목 참고)

(루트 기준)
AIServer/fastapi_app/   AI 서버 (FastAPI, Python) — 4번 항목 참고
FrontEnd/DangDang/      Android 앱
nginx/nginx.conf        리버스 프록시 설정 (배포용)
docker-compose.yml      Spring+FastAPI+Nginx 통합 실행 설정
Document/               배포 가이드, 프론트 핸드오프 문서 등
```

요청 처리 순서: **Controller → Service → Repository/FastApiClient → DB/FastAPI**, 응답은 역순으로 돌아옵니다. 각 파일 상단에 `[각주 A]`, `[각주 B]` 식으로 번호를 매겨 코드 안에 개념을 직접 설명해두었습니다.

---

## 4. 기능별 API 목록

| 기능 | 컨트롤러 | 엔드포인트 |
|---|---|---|
| 회원가입/로그인 | `AuthController` | `POST /api/auth/signup`, `/login`, `/refresh`, `/logout` |
| 내 정보 | `UserController` | `GET /api/users/me`, `PATCH /me`, `PATCH /me/notification` |
| 식사 기록 (음식 인식→확정) | `IntakeLogController` | `POST /api/intake-logs/preglucose`, `/recognize`, `/reanalyze`, `/predict`, `POST /api/intake-logs`(확정) |
| 걷기 미션 | `WalkMissionController` | `GET /active`, `POST /{no}/start`, `/track`, `/end`, `/post-glucose`, `/{no}/expire` |
| 팀 챌린지 | `TeamController` | `POST /api/teams`, `GET /`, `/me`, `/{teamNo}`, `POST /{teamNo}/join`, `DELETE /{teamNo}/members/me` |
| 랭킹 | `RankingController` | `GET /api/rankings/teams` |
| 챗봇 '당당이' | `ChatController` | `POST /api/chat`, `GET /api/chat/history` |
| 홈 대시보드 | `HomeController` | `GET /api/home` |

전체 요청/응답 스키마는 Swagger UI(`/swagger-ui/index.html`)에서 실시간으로 확인하는 것이 가장 정확합니다 (코드가 바뀌면 자동으로 갱신되므로, 문서보다 우선순위가 높습니다).

**식사 기록의 정확한 호출 순서** (자주 헷갈리는 부분이라 명시):
1. `POST /preglucose` — 식전 혈당 입력
2. `POST /recognize` — 사진/이름으로 음식 인식 시도
   - 식약처 DB에서 매칭됨 → `POST /predict`(섭취량 반영 예측) → `POST /`(확정)
   - 매칭 안 됨(`foodName: null`로 응답) → 아래 3번으로
3. `POST /reanalyze` — "AI로 분석하기" 선택 시. Gemini 추정치를 `custom_food`에 저장하고 `customFoodNo` 반환
4. `POST /predict` (reanalyze 결과 기반) → `POST /`(확정)
5. 사용자가 직접 입력하는 경우는 recognize 실패 후 바로 `POST /`(확정)만 호출 (portion은 이미 1로 간주)

---

## 5. 로컬 실행 방법

1. **Java 21** 설치 확인: `java -version`
2. **PostgreSQL** 접속 정보 확보 (학원 캠퍼스 DB 서버 또는 로컬 DB)
3. `BackEnd/.env` 파일 생성 (git에 올라가지 않음, 각자 로컬 값만):

   | 변수명 | 설명 |
   |---|---|
   | `DB_URL` | DB 호스트 주소 (예: `project-db-campus.smhrd.com`) |
   | `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` | DB 접속 정보 |
   | `JWT_SECRET` | `openssl rand -base64 64`로 생성 |
   | `FASTAPI_BASE_URL` | 로컬 개발 시 `http://localhost:8000` |
   | `INTERNAL_API_KEY` | FastAPI `.env`의 같은 이름 변수와 **반드시 동일한 값** |

4. **IntelliJ(추천)**: `BackEnd` 폴더 열기 → Gradle 자동 임포트 → `DangdangServerApplication` 실행(▶)
   **터미널**: `./gradlew bootRun`
5. FastAPI 서버(`AIServer/fastapi_app`)도 같이 띄워야 음식 인식/챗봇 기능이 동작합니다: `uvicorn main:app --reload --port 8000`
6. Swagger로 테스트: `http://localhost:8080/swagger-ui/index.html`
   - 로그인이 필요한 API는 `/api/auth/login`으로 먼저 `accessToken`을 받고, 우측 상단 **Authorize** 버튼에 `Bearer` 접두어 없이 값만 입력

---

## 6. 배포 방법 (Docker + Nginx + Cloudflare Tunnel / AWS EC2)

배포 구성은 `docker-compose.yml`(레포 루트) 하나로 관리합니다.

```
services: backend(Spring, 내부 8080) → fastapi(내부 8000) → nginx(외부 공개 80)
```

- **DB는 컨테이너에 포함하지 않음** — 기존 학원(SMHRD) PostgreSQL 서버를 그대로 사용합니다.
- FastAPI는 `expose`만 하고 `ports`는 열지 않아 EC2/호스트 바깥에서 절대 직접 접근할 수 없습니다.
- 외부에서 접속 가능한 문은 **Nginx의 80번 포트 하나뿐**입니다.

**실행 순서**
```bash
docker compose up -d --build
```

**외부 공개 방법 2가지**
1. **AWS EC2** (영구 배포): `Document/aws-ec2-deployment-guide.md` 참고 — 인스턴스 생성, Docker 설치, `.env` 세팅, `docker compose up -d --build`, 보안그룹 80번 포트 오픈까지 정리되어 있습니다.
2. **Cloudflare Tunnel** (임시/간이 공개, AWS 없이 팀원 원격 테스트용):
   ```bash
   cloudflared tunnel --url http://localhost:80
   ```
   발급된 `https://xxxx.trycloudflare.com` 주소를 안드로이드 앱의 `BASE_URL`로 교체하면 팀원이 원격에서도 접속 가능합니다. (PowerShell에서 `cloudflared`가 PATH에 없으면 설치 경로에서 `.\cloudflared.exe`로 실행)

**Cloudflare Tunnel 사용 시 주의 (이미 겪은 문제)**: 터널이 외부에서는 https로 받은 요청을 nginx에는 http로 넘겨주기 때문에, `nginx.conf`의 `X-Forwarded-Proto`를 `https`로 고정하고 `application.yml`에 `server.forward-headers-strategy: framework`를 설정해야 Swagger 로그인 테스트가 정상 동작합니다 (혼합 콘텐츠 차단 문제, 상세는 10번 트러블슈팅 참고).

---

## 7. 인증 구조 (JWT + refresh_token 회전 + 단일 세션 정책)

- **accessToken**(3시간): 매 API 요청마다 사용, 무상태(서버가 DB를 조회하지 않고 서명·만료만 검사)
- **refreshToken**(14일): accessToken 재발급 전용. DB(`refresh_token` 테이블)에 해시로 기록해 서버가 통제권을 가짐

**동작 방식**
- 로그인 성공 시 refreshToken 해시를 `refresh_token` 테이블에 저장
- `/refresh` 호출 시: 이미 폐기된 토큰이 재사용되면(탈취 의심) 해당 유저의 모든 토큰을 강제 폐기, 정상이면 기존 토큰을 폐기하고 accessToken·refreshToken을 **둘 다 새로 발급**(토큰 회전)
- 로그아웃 시: 해당 유저의 살아있는 refreshToken을 전부 폐기

**단일 세션 정책** (2026-08-25 추가): 같은 계정으로 새로 로그인하면 그 즉시 기존에 로그인되어 있던 다른 기기의 refreshToken을 전부 폐기합니다. 다만 accessToken은 무상태라 자신의 만료시간(최대 3시간)까지는 계속 유효하며, 그 accessToken으로 재발급을 시도하는 시점에야 비로소 차단됩니다. 즉시 강제 로그아웃이 필요해지면 accessToken도 매 요청 DB 조회 방식으로 바꿔야 하는데, 그러면 무상태 인증의 이점(요청마다 DB 조회 불필요)을 포기해야 합니다.

**jti(JWT ID) 클레임**: `JwtProvider.createToken()`이 매 토큰마다 `UUID.randomUUID()`를 `jti`로 심습니다. 같은 유저에게 같은 초(second)에 토큰이 두 번 발급돼도(네트워크 재시도 등) 토큰 문자열이 겹치지 않게 하기 위함입니다(과거 `refresh_token.token_hash` 유니크 제약 위반 버그의 원인이었습니다).

---

## 8. 음식 인식/재분석/예측 흐름 (FastAPI 연동)

- **recognize**: 사진 또는 텍스트로 음식을 인식. 식약처 `food_info` 테이블에서 유사 음식을 찾으면 `foodNo`와 영양정보를 반환. 못 찾으면 `foodName: null`로 응답(UI가 "검색 결과가 없어요"를 표시하도록).
- **reanalyze**: "AI로 분석하기" 선택 시 Gemini가 사진을 보고 영양정보를 추정. 이 결과는 **즉시 `custom_food`에 저장**되고 `customFoodNo`를 함께 반환합니다(프론트가 이후 확정 단계에서 이 번호를 참조하기 위함). Gemini가 숫자 대신 "없음" 같은 문자열을 줄 수 있어, FastAPI 쪽에서 숫자로 변환 안 되는 값은 전부 `null`로 정제한 뒤 내려보냅니다.
- **predict**: 확정 전 마지막 단계로, 실제 섭취량(`portion`)을 반영해 최종 예상 혈당 상승량을 계산합니다. `portion`이 의미를 갖는 건 이 단계뿐이며, 확정(`POST /api/intake-logs`) 시점에는 이미 반영된 값으로 취급합니다.
- FastAPI는 이 모든 처리에서 **DB에 쓰지 않고 읽기만** 합니다. 실제 `custom_food`/`intake_log` 저장은 전부 Spring(`RecognizeProxyService`, `IntakeLogService`)이 수행합니다.

---

## 9. DB 관리 원칙 — 마이그레이션 파일을 만들지 않는 이유

이 프로젝트는 **DB를 PM(백엔드 담당)이 직접 관리**하기로 했습니다. 그래서:
- `src/main/resources/db/migration/`에 Flyway SQL 파일을 새로 만들지 않습니다.
- 엔티티(Java 코드)를 추가/수정할 때 필요한 테이블/컬럼 변경 사항은 **코드가 아니라 이 문서(또는 PR 설명)로 텍스트 안내만** 합니다 — 실제 `ALTER TABLE` 등은 DB 담당이 직접 실행합니다.
- `application.yml`의 `jpa.hibernate.ddl-auto: validate`는 "엔티티와 실제 테이블이 일치하는지 검사만" 하고 스프링이 마음대로 테이블을 바꾸지 않도록 막아줍니다. 즉, DB 스키마가 먼저 맞춰져 있어야 서버가 정상 기동합니다.

---

## 10. 트러블슈팅 기록

이 프로젝트 진행 중 실제로 겪고 해결한 백엔드 이슈들입니다. (발표/보고서용으로 재사용 가능)

1. **`refresh_token` 유니크 제약 위반**: JWT가 userNo+발급시각(초 단위)만으로 만들어져 같은 초에 두 번 발급되면 토큰 문자열이 완전히 동일해져 해시 충돌 → `jti`(UUID) 클레임 추가로 해결.
2. **Swagger 로그인(Execute)이 Cloudflare Tunnel 환경에서 실패**: 터널이 nginx에는 항상 http로 요청을 넘겨서 Spring이 "이 서버는 http 전용"이라고 오판 → HTTPS 페이지에서 http로 요청을 보내려다 브라우저가 혼합 콘텐츠로 차단. `nginx.conf`의 `X-Forwarded-Proto: https` 고정 + `application.yml`의 `forward-headers-strategy: framework`로 해결.
3. **`/api/intake-logs/reanalyze` 500 에러 (2건)**: (1) `custom_food.sugar`/`calorie`가 NOT NULL인데 Gemini가 값을 빼먹어도 통과되던 검증 로직 → 저장 시 0으로 대체. (2) Gemini가 숫자 필드에 "없음" 같은 문자열을 반환해 Spring 역직렬화가 깨짐 → FastAPI에서 숫자 변환 실패 값을 전부 `null`로 정제.
4. **DB 커넥션이 조용히 끊기는 문제**: 학원 DB 서버가 유휴 커넥션을 임의로 끊어 간헐적 500 발생 → Hikari(`max-lifetime`/`idle-timeout`/`keepalive-time`) + FastAPI SQLAlchemy(`pool_pre_ping`/`pool_recycle`) 설정으로 해결.
5. **동시 로그인 보안 이슈**: 같은 계정이 여러 기기에서 동시 로그인 가능하던 것을 "새 로그인 시 기존 세션 강제 로그아웃" 정책으로 변경 (`refreshTokenRepository.revokeAllByUserNo()`를 로그인 시점에도 호출).
6. **홈 화면 주간 걷기 출석 표시 오류**: 오늘 미션에 실패해도 즉시 X로 표시되던 것을 "오늘은 성공 시에만 O, 실패 표시는 다음날부터"로 수정.
7. **챗봇이 대화 맥락을 기억하지 못함**: FastAPI가 매 요청을 독립적으로 처리하던 구조 → Spring이 최근 대화 기록(`ai_chat` 테이블, 텍스트 타입만 최대 3턴)을 조회해 FastAPI 요청에 함께 실어보내도록 수정.
8. **음식 사진 인식 실패 시 UI 처리**: 식약처 DB에 없는 음식(예: 특정 프랜차이즈 메뉴)은 Gemini가 이름을 추정해도 그대로 노출하지 않고 `foodName: null`로 응답해, "검색 결과가 없어요" 화면이 뜨도록 통일.

---

## 11. 백엔드 초심자를 위한 용어 각주 모음

1. **Gradle**: 프로젝트를 빌드하고 라이브러리를 자동으로 내려받는 도구.
2. **Spring Boot**: 자바로 웹 API 서버를 빠르게 만들 수 있게 해주는 프레임워크.
3. **JPA/Hibernate (ORM)**: SQL문을 직접 안 쓰고 자바 객체(엔티티)로 DB를 다루는 기술.
4. **DTO**: 클라이언트-서버가 주고받는 JSON의 모양을 정의한 클래스.
5. **Controller / Service / Repository**: 입출력(Controller) → 판단·로직(Service) → DB 접근(Repository) 순으로 책임을 나누는 계층형 구조.
6. **JWT(JSON Web Token)**: 로그인 성공 시 발급하는 디지털 출입증. 서버가 세션을 따로 저장하지 않고 토큰 자체의 서명·만료만 검사하는 "무상태 인증"에 사용.
7. **토큰 회전(rotation)**: refreshToken을 재발급할 때마다 기존 토큰은 폐기하고 새 토큰을 발급하는 보안 기법. 탈취된 토큰의 재사용을 탐지할 수 있게 해줌.
8. **리버스 프록시(Nginx)**: 외부 요청을 맨 앞에서 받아 내부 서버로 전달해주는 "문지기" 프로그램.
9. **X-Internal-Api-Key**: Spring이 FastAPI를 호출할 때만 붙이는 내부 전용 인증 헤더. 이 키를 모르면 FastAPI를 호출할 수 없음(외부에서 FastAPI에 직접 접근해도 이 헤더가 없어 거부됨).
10. **HikariCP**: Spring Boot의 기본 DB 커넥션 풀(연결을 미리 만들어두고 재사용하는 관리자).
11. **Docker / Docker Compose**: 애플리케이션을 어떤 컴퓨터에서든 동일하게 실행되도록 "컨테이너"로 포장하는 기술과, 여러 컨테이너를 한 번에 관리하는 도구.
12. **Cloudflare Tunnel**: 공유기 포트포워딩이나 고정 IP 없이도 로컬에서 띄운 서버를 외부에 공개할 수 있게 해주는 무료 터널링 서비스.
13. **Flyway**: 테이블 생성/변경 SQL을 버전별 파일로 관리하는 DB 마이그레이션 도구 (이 프로젝트는 9번 원칙에 따라 새 파일을 추가하지 않음).
14. **환경변수 / `.env`**: 비밀번호·API 키처럼 코드에 직접 적으면 안 되는 값을, 코드와 분리된 파일에 모아두고 실행 시점에 주입하는 방식.

---

## 12. 다음 단계로 넘어갈 때 참고

- 만료된 `refresh_token` 행을 주기적으로 삭제하는 배치(`@Scheduled`)를 추가하면 테이블이 무한정 커지는 것을 막을 수 있습니다 (지금은 없어도 동작에는 문제없음 — 정리는 선택 사항).
- 소셜 로그인(구글/카카오)을 붙일 때도 `AuthService.saveRefreshTokenRecord()`를 그대로 재사용할 수 있습니다. 플랫폼별 토큰 검증 로직만 추가하면 됩니다.
- `AIServer/fastapi_app/prompts/reanalyze.py`의 Gemini 프롬프트가 "100g당" 기준으로 되어 있는데, 나머지 시스템은 "1인분(serving_size) 기준"으로 통일되어 있어 실제로 오차가 생기는지 추가 확인이 필요합니다 (아직 미해결로 남겨둔 항목).
- 즉시 강제 로그아웃이 필요해지면(현재는 최대 3시간 지연), accessToken 만료시간을 줄이거나 매 요청마다 DB로 검사하는 구조로 전환을 검토해야 합니다.

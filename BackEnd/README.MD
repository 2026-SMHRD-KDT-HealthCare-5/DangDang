# 당당(DangDang) 백엔드 — 로그인/회원가입 (M1 단계)

## 목차
1. 폴더 구조 한눈에 보기
2. 실행 전 준비물
3. 실행 방법
4. API 테스트 (curl 예시)
5. ⚠️ 프론트엔드와 맞춰야 할 것 — JSON 필드 이름 표기법
6. 평소 활동량 매핑표 (주 n회 ↔ 하/중/상)
7. refresh_token 테이블 — 무상태 인증의 한계와 해결 방법
8. Q&A — UserRepository엔 findByEmail밖에 없는데 INSERT는 어떻게?
9. 백엔드 초심자를 위한 용어 각주 모음
10. 다음 단계로 넘어갈 때 참고

이번 작업 범위: **이메일 회원가입 · 로그인 · 토큰 재발급(회전) · 로그아웃(서버측 무효화)** 4개 API.
(구글/카카오 소셜 로그인은 기획서상 "확장(보류)" 항목이라 제외했습니다.)

---

## 1. 폴더 구조 한눈에 보기

```
BackEnd/
├── build.gradle.kts
├── settings.gradle.kts
└── src/main/
    ├── java/com/dangdang/
    │   ├── DangdangServerApplication.java     앱 시작점(main)
    │   ├── config/SecurityConfig.java         로그인 규칙(누가 어디에 접근 가능한지)
    │   ├── controller/AuthController.java     URL 정의 (/api/auth/...)
    │   ├── service/AuthService.java           실제 판단 로직 (회원가입/로그인/재발급/로그아웃)
    │   ├── repository/
    │   │   ├── UserRepository.java            users 테이블 접근
    │   │   └── RefreshTokenRepository.java    refresh_token 테이블 접근 [각주7]
    │   ├── entity/
    │   │   ├── User.java                      users 테이블과 매핑
    │   │   ├── RefreshToken.java              refresh_token 테이블과 매핑 [각주7]
    │   │   └── ActivityLevel.java             "주 n회" ↔ 하/중/상 매핑 [각주6]
    │   ├── dto/request, dto/response          요청/응답 JSON 모양
    │   ├── security/
    │   │   ├── JwtProvider.java               토큰 발급·검증
    │   │   ├── JwtAuthenticationFilter.java    매 요청마다 토큰 검사
    │   │   ├── TokenHasher.java               refreshToken을 해시로 변환 [각주7]
    │   │   └── CustomAuthenticationEntryPoint.java
    │   └── exception/                         에러를 공통 형식으로 변환
    └── resources/
        ├── application.yml
        └── db/migration/
            ├── V1__create_users_table.sql
            └── V2__create_refresh_token_table.sql   [각주7]
```

요청 처리 순서: **Controller → Service → Repository → DB**, 응답은 반대 순서로 돌아옵니다.
(각 파일 상단에 `[각주 A]`, `[각주 B]` ... 처럼 번호를 매겨 코드 안에 직접 개념을 설명해두었습니다.)

---

## 2. 실행 전 준비물

1. **Java 21**: `java -version`
2. **PostgreSQL** 실행 중, `dangdang` 빈 데이터베이스 존재
   ```sql
   CREATE DATABASE dangdang;
   ```
3. **`.env` 파일 만들기** [각주8]

   `spring-dotenv` 라이브러리가 `BackEnd/.env` 파일을 자동으로 읽어 환경변수로 등록해줍니다.
   터미널이나 IntelliJ Run Configuration마다 따로 값을 입력할 필요가 없습니다.

   1. `BackEnd/.env.example`을 복사해서 같은 폴더에 `.env`라는 이름으로 저장
   2. 자기 PostgreSQL 설정에 맞게 값 채우기

   | 변수명 | 설명 | 예시 |
   |---|---|---|
   | DB_USERNAME | PostgreSQL 접속 계정 | postgres |
   | DB_PASSWORD | PostgreSQL 비밀번호 | (자기가 설치할 때 정한 값) |
   | DB_PORT | PostgreSQL 포트 | 5432 |
   | JWT_SECRET | 토큰 서명용 비밀키 | `openssl rand -base64 64`로 생성 |

   `.env`는 `.gitignore`에 등록되어 있어 git에 올라가지 않습니다 — 각자 자기 PC 값만 로컬에 두는 용도입니다.
   (`.env.example`은 값이 없는 템플릿이라 git에 커밋해도 안전하고, 팀원들이 그대로 복사해서 씁니다.)

---

## 3. 실행 방법

- **IntelliJ(추천)**: `BackEnd` 폴더를 열면 Gradle 자동 임포트 → `DangdangServerApplication` 실행(▶).
- **터미널**: `gradle wrapper` 한 번 실행 후 `./gradlew bootRun`.

서버가 처음 뜰 때 Flyway가 `V1`, `V2` 두 마이그레이션을 순서대로 실행해 `users`, `refresh_token`
테이블을 자동 생성합니다. (`Successfully applied 2 migrations` 로그 확인)

**Swagger UI로 테스트하기**: 서버가 뜬 뒤 브라우저에서 http://localhost:8080/swagger-ui.html 접속.
`/api/auth/logout`처럼 로그인이 필요한 API는 먼저 `/api/auth/login`을 Swagger에서 실행해
`accessToken`을 받은 다음, 화면 오른쪽 위 **Authorize** 버튼을 눌러 그 값을 넣어두면
(`Bearer` 접두어 없이 토큰 값만) 이후 요청에 자동으로 인증 헤더가 붙습니다.

---

## 4. API 테스트 (curl 예시)

**회원가입** — activityLevel은 "주 1~3회" / "주 3~5회" / "주 5~7회" 중 하나 (6번 항목 참고)
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "nickname": "당당이",
    "email": "test@dangdang.com",
    "password": "password123",
    "gender": "Male",
    "birthDate": "1997-05-16",
    "height": 170,
    "weight": 70,
    "hba1c": 6.5,
    "activityLevel": "주 3~5회",
    "targetGlucose": 180
  }'
# -> 201 Created  { "userNo": 1 }
```

**로그인**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{ "email": "test@dangdang.com", "password": "password123" }'
# -> 200 OK  { "accessToken": "...", "refreshToken": "..." }
```

**토큰 재발급 (회전)** — 응답으로 받은 refreshToken이 새 값이므로 반드시 로컬 저장값을 덮어써야 합니다.
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{ "refreshToken": "<로그인 응답의 refreshToken>" }'
# -> 200 OK  { "accessToken": "...", "refreshToken": "..." }   (둘 다 새 토큰, 기존 refreshToken은 폐기됨)
```

**로그아웃** — 이 사용자의 refresh_token을 DB에서 전부 폐기합니다.
```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer <로그인 응답의 accessToken>"
# -> 204 No Content
```

**에러 예시** (이미 폐기/재사용된 refreshToken으로 재발급 시도)
```json
{
  "code": "AUTH_401_INVALID_TOKEN",
  "message": "유효하지 않거나 만료된 토큰입니다.",
  "timestamp": "2026-08-12T10:00:00"
}
```

---

## 5. ⚠️ 프론트엔드와 맞춰야 할 것 — JSON 필드 이름 표기법

Notion API 명세서의 body 설명(`nickname,email,password,...`)은 DB 컬럼명을 그대로 옮긴 **snake_case**
표기로 보이지만, 안드로이드에 이미 작성된 `TokenResponse.kt`(accessToken, refreshToken)는
**camelCase**입니다. 이번 구현은 **camelCase**(자바/스프링 기본값)로 통일했습니다.

바꾸고 싶다면 `application.yml`에 `spring.jackson.property-naming-strategy: SNAKE_CASE` 한 줄이면
전체 API가 일괄 변경됩니다. **반찬영(안드로이드 담당)님과 확인해주세요.**

---

## 6. 평소 활동량 매핑표 (주 n회 ↔ 하/중/상)

`users.activity_level` 컬럼은 스키마상 정수(INTEGER)로 확정되어 있어서, 화면 문구를 그대로 저장할 수
없습니다. `ActivityLevel` enum(`entity/ActivityLevel.java`)이 아래 표로 변환·검증합니다.

| 프론트 화면 문구 (요청 JSON `activityLevel` 값) | 서버 내부 등급 | DB 저장값(`activity_level`) |
|---|---|---|
| `"주 1~3회"` | 하 | `1` |
| `"주 3~5회"` | 중 | `2` |
| `"주 5~7회"` | 상 | `3` |

세 문자열 중 하나가 아니면(오타 등) `400 AUTH_400_INVALID_ACTIVITY_LEVEL`로 응답합니다.
활동량은 선택 입력이라 아예 안 보내도(`null`) 회원가입은 됩니다.

---

## 7. refresh_token 테이블 — 무상태 인증의 한계와 해결 방법

**문제**: JWT를 순수 무상태(stateless)로만 쓰면, 서버는 발급한 토큰을 "기억"하지 않으므로
1) 로그아웃해도 그 토큰을 강제로 못 끊고(만료 전까지 계속 유효), 2) 토큰이 탈취돼도 대응할 수 없고,
3) 같은 토큰이 두 군데서 동시에 쓰여도(도난 정황) 알아챌 방법이 없습니다.

**해결(하이브리드 방식)**: accessToken(수명 1시간, 실사용)은 그대로 무상태로 두고, refreshToken
(수명 14일, 재발급 전용)만 DB(`refresh_token` 테이블)에 기록을 남겨 서버가 통제권을 갖습니다.

```sql
CREATE TABLE refresh_token (
    token_no    BIGSERIAL      PRIMARY KEY,
    user_no     INTEGER        NOT NULL REFERENCES users(user_no),
    token_hash  VARCHAR(64)    NOT NULL UNIQUE,  -- 원본 토큰이 아니라 SHA-256 해시만 저장
    issued_at   TIMESTAMP      NOT NULL DEFAULT now(),
    expires_at  TIMESTAMP      NOT NULL,
    revoked     BOOLEAN        NOT NULL DEFAULT false,
    revoked_at  TIMESTAMP
);
```
(전체 파일: `db/migration/V2__create_refresh_token_table.sql`)

**동작 방식**
- **로그인 시**: refreshToken을 발급하면서 그 해시값을 이 테이블에 한 줄 저장합니다(`revoked=false`).
- **재발급(`/refresh`) 시**: 들어온 refreshToken의 해시로 이 테이블을 조회해서 —
  - 아예 없으면 → 401 (위조된 토큰)
  - `revoked=true`(이미 로그아웃했거나 이미 한 번 재발급에 쓰인 토큰)면 → **탈취 의심**으로 간주해
    그 사용자의 살아있는 토큰을 전부 폐기하고 401 (아래 "재사용 탐지" 참고)
  - 정상이면 → 그 토큰을 즉시 `revoked=true`로 바꾸고(1회용), accessToken·refreshToken을
    **둘 다 새로 발급**(=토큰 회전/rotation)해 다시 저장합니다.
- **로그아웃 시**: 이 사용자의 `revoked=false`인 행을 전부 `revoked=true`로 바꿉니다. 그 순간부터
  이 사용자의 모든 refreshToken은 재발급에 쓸 수 없게 됩니다. (accessToken 자체는 자기 만료시간인
  최대 1시간까지는 여전히 유효하지만, 그 시간 안에 재발급이 막히므로 로그인 상태가 끊깁니다.)

**"재사용 탐지"가 중요한 이유**: 정상적인 사용자는 하나의 refreshToken을 재발급에 딱 한 번만 씁니다
(쓰자마자 서버가 폐기하고 새 걸 주니까요). 그런데 폐기된(`revoked=true`) 토큰으로 또 재발급 요청이
들어온다는 건, 공격자가 과거에 훔친 토큰을 정상 사용자와 동시에 쓰고 있다는 뜻일 가능성이 높습니다.
그래서 이 경우 그 사용자의 토큰을 전부 강제로 끊어 피해를 막습니다.

원본 팀 확정 스키마(10개 테이블)에는 없던 **신규 테이블**이니, `DangDang_schema.md`나 Notion DB
개요 문서에도 반영해서 팀원들과 공유해주세요.

---

## 8. Q&A — UserRepository엔 findByEmail밖에 없는데 INSERT는 어떻게?

`UserRepository`는 `JpaRepository<User, Integer>`를 **상속**만 하고 있습니다.

```java
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
```

`JpaRepository`(스프링이 미리 만들어둔 부모 인터페이스)에 이미 `save()`, `findById()`,
`existsById()`, `deleteById()` 같은 기본 기능이 다 구현되어 있어서, **상속받는 순간 자동으로
다 쓸 수 있습니다.** 우리가 직접 코드를 작성한 `findByEmail`/`existsByEmail`은 "이메일로 찾기"처럼
JpaRepository에는 없는, 우리 서비스에만 필요한 조회 기능이라 추가로 선언한 것뿐입니다.

실제 INSERT가 일어나는 지점은 `AuthService.signup()` 안의 이 한 줄입니다.
```java
User savedUser = userRepository.save(user);   // ← 여기서 실제 INSERT INTO users ... 실행
```
`save()`는 `UserRepository.java` 파일 어디에도 안 적혀 있지만, `JpaRepository`를 상속했기 때문에
호출할 수 있는 것입니다. (같은 원리로 `RefreshTokenRepository`도 `findByTokenHash`/
`revokeAllByUserNo`만 직접 선언하고, `save()`는 상속받아 그대로 씁니다.)

---

## 9. 백엔드 초심자를 위한 용어 각주 모음

1. **Gradle**: 프로젝트를 빌드하고 라이브러리를 자동으로 내려받아주는 도구.
2. **Spring Boot**: 자바로 웹 서버(API 서버)를 빠르게 만들 수 있게 해주는 프레임워크.
3. **JPA/Hibernate (ORM)**: SQL문을 직접 안 쓰고 자바 객체(엔티티)로 DB를 다루는 기술.
4. **DTO**: 클라이언트-서버가 주고받는 JSON의 모양을 정의한 클래스.
5. **Controller / Service / Repository**: 입출력(Controller) → 판단·로직(Service) →
   DB 접근(Repository) 순으로 책임을 나누는 계층형 구조.
6. **JpaRepository 상속**: 인터페이스를 상속만 해도 save/findById 같은 기본 DB 기능이 자동으로
   생기는 Spring Data JPA의 핵심 기능. (8번 Q&A 참고)
7. **Bean Validation** (`@NotBlank`, `@Email` 등): 요청 JSON 형식을 자동으로 검사해주는 기능.
8. **BCrypt**: 비밀번호를 복호화 불가능하게 암호화(해싱)하는 표준 방식.
9. **SHA-256 해시**: 어떤 문자열이든 같은 입력이면 항상 같은 64자 결과를 내는 단방향 변환.
   refreshToken을 DB에 저장할 때 원문 대신 이 해시값만 저장합니다(7번 항목 참고).
10. **JWT(JSON Web Token)**: 로그인 성공 시 발급하는 토큰. 무상태 인증에 사용.
11. **무상태(stateless) 인증**: 서버가 세션을 따로 저장하지 않고 토큰만으로 로그인 여부를 판단하는
    방식. 장점(확장 쉬움)과 단점(강제 로그아웃 어려움, 7번 항목에서 보완)이 둘 다 있습니다.
12. **토큰 회전(rotation)**: refreshToken을 재발급할 때마다 기존 토큰은 폐기하고 새 토큰을 발급하는
    보안 기법. 탈취된 토큰의 재사용을 탐지할 수 있게 해줍니다.
13. **accessToken / refreshToken**: accessToken은 짧게(1시간) 사는 실사용 토큰, refreshToken은
    길게(14일) 사는 재발급 전용 토큰(이제는 회전되어 재사용 1회로 제한됨).
14. **Spring Security / Filter**: 컨트롤러 도달 전 "로그인 상태인지"를 검사하는 보안 프레임워크.
15. **Authentication (스프링 시큐리티)**: 로그인된 사용자 정보를 담는 객체. 컨트롤러 메서드
    파라미터에 그냥 선언하면 스프링이 자동으로 채워줍니다(`AuthController.logout()` 참고).
16. **Flyway (DB 마이그레이션)**: 테이블 생성/변경 SQL을 버전별 파일로 관리하는 도구.
17. **환경변수**: 비밀번호·비밀키처럼 코드에 직접 적으면 안 되는 값을 실행 시점에 주입하는 방식.
18. **HTTP 상태코드**: 200/201/204 성공 계열, 400 형식오류, 401 인증실패, 409 충돌(중복 등).
19. **`.env` 파일 / dotenv**: 비밀번호·API 키처럼 사람마다 다르고 git에 올리면 안 되는 값을,
    코드와 분리된 파일 하나에 모아두는 관례. `spring-dotenv` 라이브러리가 이 파일을 읽어 자동으로
    환경변수처럼 등록해줘서, 실행 방법(터미널/IntelliJ)이 달라져도 항상 같은 값을 씁니다.

---

## 10. 다음 단계로 넘어갈 때 참고

- `/api/users/me` (내 정보 조회/수정)를 만들 때는 `@LoginUser` 커스텀 어노테이션 + 리졸버를 추가하면
  편합니다. (이번 로그아웃 구현에 쓴 `Authentication authentication` 방식을 그대로 재사용해도 됩니다.)
- 소셜 로그인(구글/카카오)을 붙일 때도 로그인 성공 후 `saveRefreshTokenRecord()`를 그대로 재사용할
  수 있습니다 — `AuthService`에 플랫폼별 토큰 검증 로직만 추가하면 됩니다.
- 만료된 `refresh_token` 행을 주기적으로 지우는 배치(@Scheduled)를 추가하면 테이블이 무한정
  커지는 걸 막을 수 있습니다(지금은 남겨둬도 동작에는 문제 없음 — 정리는 선택 사항).

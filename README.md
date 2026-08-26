# 당당 (DangDang)

**AI 혈당상승량 예측 기반 식후 워킹 헬스케어 서비스 — Android 클라이언트**

> "먹은 만큼, 걸은 만큼, 그리고 함께"

먹은 음식을 입력하면 AI가 식후 혈당이 얼마나 오를지 예측하고, 그 혈당을 낮추는 데 필요한 만큼만 걸으면 되도록 맞춤 걷기 목표를 알려주는 서비스입니다. 혼자 걷기 어려우면 팀을 만들어 함께할 수도 있습니다.

이 저장소는 그중 **Android 앱** 파트입니다. (백엔드 서버는 별도 저장소 `README_backend.md` 참고)

---

## 1. 왜 만들었나요?

질병관리청 2024년 국민건강영양조사에 따르면 당뇨병 유병률은 남성 13.3%, 여성 7.8%로 전년 대비 상승했습니다. 특히 20~30대는 다른 연령대보다 당뇨병 인지율·치료율·조절률이 모두 낮게 나타났습니다(1).

혈당 관리는 중요하지만 "무엇을 얼마나 먹었을 때, 얼마나 걸어야 하는지"를 매 끼니 스스로 계산하기는 어렵습니다. 당당은 이 계산을 AI가 대신하고, 실천(걷기)까지 하나의 흐름으로 연결합니다.

(1) 질병관리청 심층보고서(2023)에서도 20~40대 젊은 연령층은 인지율·치료율이 50% 미만으로 특히 취약하다고 별도 보고되었습니다.

---

## 2. 이런 분께 추천합니다

- **2030 당뇨 전단계군**: 아직 진단 전이지만 식습관·혈당 관리가 필요한 분
- **4050 관리대상군**: 이미 혈당 관리를 하고 있고, 꾸준한 운동 습관이 필요한 분
- 헬스장이나 격한 운동보다 **일상 속 걷기** 중심의 저강도 관리를 원하는 분

---

## 3. 핵심 기능 5가지

### ① AI 혈당 예측
음식 사진을 찍거나 음식 이름을 입력하면, 공공 영양성분 데이터를 조회해 탄수화물·당·단백질·지방 등 영양 정보와 함께 **예상 식후 혈당 상승량**을 계산해줍니다.

### ② 맞춤 걷기 처방 + GPS
예측된 혈당 상승량을 낮추는 데 필요한 걷기 거리·시간을 자동으로 계산하고, 실제 걷는 동안 GPS로 실시간 경로를 추적합니다.

<p align="center"><img src="screenshots/Home_배포.png" width="260"/> <img src="screenshots/걷기_배포_진행중.png" width="260"/></p>

### ③ 챗봇 '당당이'
당뇨와 관련된 질문에 답해주는 AI 건강비서입니다. 논문·진료지침 기반으로 답변하며(RAG 방식), 약물·인슐린 관련 질문처럼 전문 의료 판단이 필요한 내용은 답변을 차단하고 병원 상담을 안내합니다.

### ④ 워킹 챌린지 (팀 걷기)
팀을 만들거나 검색해서 가입하면, 팀원들과 함께 월간 걷기 목표를 채워나갈 수 있습니다. 팀 내 순위와 전체 사용자 랭킹도 확인할 수 있어 동기부여가 됩니다.

<p align="center"><img src="screenshots/팀_만들기_배포.png" width="220"/> <img src="screenshots/팀_가입_배포.png" width="220"/> <img src="screenshots/커뮤니티_팀_챌린지_배포.png" width="220"/></p>

### ⑤ 내 정보 대시보드
주간 걷기 시간·거리, 식후 혈당 추이, 목표 달성률을 한 화면에서 확인할 수 있습니다.

<p align="center"><img src="screenshots/커뮤니티_전체_랭킹_배포.png" width="260"/></p>

---

## 4. 사용 흐름 (한 끼 기준)

1. **식전 혈당 입력** (없으면 건강 정보 기반 기본값 자동 적용)
2. **음식 입력** — 사진 촬영 또는 텍스트 입력
3. AI가 음식을 인식하고 **예상 혈당 상승량** 제시 → 맞았는지 확인(틀리면 재분석 또는 직접 입력)
4. 예측 결과에 따라 **걷기 미션**(목표 거리·칼로리) 자동 생성
5. 걷기 시작 → GPS로 실시간 추적 → 목표 달성 시 완료 처리
6. 걸은 거리는 **내 기록**과 **팀 챌린지 실적**에 모두 반영

---

## 5. 다른 서비스와 무엇이 다른가요?

| 구분 | 혈당 예측·기록 | 걷기·운동 미션 | 챗봇 서비스 |
|---|---|---|---|
| A사 (닥터다이어리) | O (수동 기록 중심) | X | △ (제한적) |
| B사 (캐시워크) | X | O (포인트 리워드) | X |
| C사 (NOOM) | X | △ (활동량 측정) | O (식습관 코칭) |
| **당당** | **O (음식사진 기반 AI 예측)** | **O (혈당관리 연계 걷기 미션)** | **O (당뇨관련 RAG 챗봇)** |

당당의 차별점은 **혈당 예측과 걷기 처방을 하나의 흐름으로 연결**했다는 점입니다. 다른 서비스들은 기록·운동·상담 중 한두 가지만 제공하지만, 당당은 "무엇을 먹었으니 얼마나 걸어야 하는지"까지 이어서 안내합니다.

---

## 6. (개발자용) 기술 스택

| 영역 | 기술 | 비고 |
|---|---|---|
| 언어 | Kotlin | |
| UI | Jetpack Compose | 선언형 UI 프레임워크(2) |
| 아키텍처 | MVVM | Model-View-ViewModel(3) |
| 네트워킹 | Retrofit2 | REST API 호출용 라이브러리 |
| DI | Hilt | 의존성 주입 프레임워크(4), `HiltModule`에서 설정 |
| 인증 처리 | OkHttp `Interceptor` / `Authenticator` | JWT 토큰 자동 첨부·재발급(5) |
| 위치 추적 | GPS | 걷기 미션 실시간 경로 추적 |
| 활동 데이터 | Health Connect API | 걸음 수·활동량 연동(구글 표준 헬스 데이터 API) |
| 지도 | KakaoMap SDK | 걷기 경로 시각화 |

(2) 기존 XML 레이아웃 대신 코틀린 코드로 UI를 선언하는 안드로이드 최신 UI 툴킷입니다.
(3) 화면(View)과 데이터·로직(Model)을 ViewModel이 중개하는 구조로, 화면 회전 등에도 데이터가 유지되고 테스트가 쉬워집니다.
(4) 객체를 필요한 곳에서 직접 생성하지 않고 프레임워크가 대신 주입해주는 패턴으로, 코드 간 결합도를 낮춥니다.
(5) Interceptor는 모든 요청에 `Authorization: Bearer {accessToken}` 헤더를 자동으로 붙이고, Authenticator는 토큰 만료(401 응답) 시 refreshToken으로 재발급받아 요청을 재시도합니다.

---

## 7. (개발자용) 서버 연동 규칙

- **클라이언트는 Spring Boot(:8080)만 호출합니다.** FastAPI(:8000, AI 추론 서버)는 클라이언트가 직접 호출하지 않으며, Spring이 내부적으로 중계합니다.
- **로컬 개발 환경**
  - 에뮬레이터: `http://10.0.2.2:8080`
  - 실기기: PC의 LAN IP + `:8080` (같은 Wi-Fi 네트워크 필요)
  - HTTPS가 아니므로 `AndroidManifest.xml`에 `usesCleartextTraffic="true"` 또는 `network-security-config` 설정 필요
- **배포 환경**: Nginx 리버스 프록시를 통한 단일 도메인 HTTPS (`baseUrl`만 배포용으로 교체하면 됨)
- **인증 방식**: 로그인 성공 시 발급받은 `accessToken`(단기)·`refreshToken`(장기)을 사용. `userNo`(사용자 식별자)는 서버가 토큰에서 추출하므로 요청 Body에 포함하지 않습니다.

> 상세 API 스펙(요청/응답 형식, 엔드포인트 목록)은 당당 API 명세서 참고 — 저장소 내 문서 링크로 교체 필요.

---

## 8. (개발자용) 프로젝트 구조

```
FrontEnd/DangDang/app/src/main/java/com/dangdang/
├── data/                     데이터 계층
│   ├── api/                  Retrofit 인터페이스 (서버 API 정의)
│   ├── network/              OkHttp Interceptor/Authenticator, Retrofit 인스턴스 설정
│   ├── repository/           Repository (ViewModel과 API 사이 중개)
│   ├── service/              백그라운드 서비스 (GPS 추적 등)
│   ├── manager/               DataStore 등 로컬 저장소 접근
│   ├── model/                 요청/응답 DTO (chat, community, home, user, walk 등 기능별로 세분화)
│   └── enums/                 서버 값과 매핑되는 열거형
├── di/                        Hilt 모듈 (의존성 주입 설정)
├── ui/
│   ├── screens/                화면(Composable) — first(온보딩)/main/navigation(홈·당당이·걷기·커뮤니티·마이페이지) 등 기능별 하위 폴더
│   ├── viewmodel/               화면별 ViewModel (screens 구조와 1:1 대응)
│   ├── navhost/                  Compose Navigation 그래프
│   └── theme/                     색상/타이포그래피 등 디자인 시스템
├── component/                  화면 간 재사용되는 공용 Composable
│   ├── page/                     화면별 전용 UI 조각 (home, walk, signup, community 등)
│   ├── chart/, guage/, map/       혈당 그래프, 게이지, 카카오맵 래퍼
│   ├── navigation/                하단/상단 내비게이션 바
│   └── button/, text/, dialog/, toggle/, errorview/ 등  범용 UI 부품
└── common/utils/                날짜 계산 등 공통 유틸리티
```

구조 원칙: `data`(서버 통신·저장)와 `ui`(화면·상태)를 분리하고, `ui` 안에서는 `screens`(화면)와 `viewmodel`(그 화면의 로직·상태)을 1:1로 대응시키는 MVVM 패턴을 따릅니다. `component`는 여러 화면에서 공통으로 쓰는 UI 조각을 모아둔 폴더로, 특정 화면 전용이면 `component/page/` 아래에, 화면 상관없이 범용이면 `component/` 바로 아래(button, text 등)에 둡니다.

**`build.gradle.kts` 주요 버전** (`app/build.gradle.kts`, `gradle/libs.versions.toml` 기준)

| 항목 | 값 |
|---|---|
| compileSdk | 37 (minor 1) |
| minSdk | 27 |
| targetSdk | 37 |
| Kotlin | 2.4.0 |
| AGP (Android Gradle Plugin) | 9.2.1 |
| Compose BOM | 2026.06.01 |
| Material3 | 1.4.0 |
| Hilt | 2.60.1 |
| Navigation Compose | 2.9.8 |
| Retrofit (converter-gson 버전 기준) | 3.0.0 |
| OkHttp logging-interceptor | 5.4.0 |
| Coil (이미지 로딩) | 2.7.0 / 3.5.0(coil3) 혼용 |
| KakaoMap SDK (v2-maps) | 2.14.0 |
| Kakao 로그인 SDK (v2-user) | 2.24.0 |
| Health Connect (connect-client) | 1.1.0 |
| Play Services Location | 21.4.0 |

`local.properties`(gitignore 대상, 각자 로컬에 생성)에서 `API_BASE_URL`, `KAKAO_NATIVE_APP_KEY`, `GoogleLoginKey`, `InquiryEmail`, `ExamplePictureUrl` 값을 읽어 `BuildConfig` 필드로 주입합니다.

---

## 9. 팀

| 이름 | 역할 |
|---|---|
| 김린아 | PM · Backend · DB |
| 반찬영 | Frontend · Android |
| 진종언 | AI · Modeling · RAG 챗봇 |
| 이정석 | 문서 작업 및 영상 편집 |

---
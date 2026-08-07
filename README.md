# 당당이 (DangDang) — Frontend

혈당 관리를 도와주는 AI 헬스케어 안드로이드 앱입니다. 식사·혈당 기록, AI 챗봇 피드백, 걷기(운동) 기록, 팀 챌린지/랭킹 커뮤니티 기능을 제공합니다.

## 기술 스택

- **언어 / UI**: Kotlin, Jetpack Compose (Material3)
- **아키텍처**: MVVM (ViewModel + Repository), Hilt(DI)
- **비동기 통신**: Retrofit2 + Gson, OkHttp Logging Interceptor
- **네비게이션**: Navigation-Compose
- **이미지 로딩**: Coil (Compose / GIF)
- **차트**: Vico (Compose)
- **지도**: Kakao Map SDK, Play Services Location
- **로그인**: 카카오 로그인(Kakao SDK), 구글 로그인(Google Identity)
- **헬스 데이터**: Health Connect, 걸음 수 측정을 위한 Foreground Service(`StepCounterService`)
- **최소/타깃 SDK**: minSdk 27 / targetSdk 37, Java/Kotlin 21

## 프로젝트 구조

```
FrontEnd/DangDang
├── app/src/main/java/com/dangdang
│   ├── Application.kt / MainActivity.kt   # 앱 진입점, API_BASE_URL 등 전역 설정
│   ├── common/utils/                      # Route(화면 경로), AppPrefs, WalkUtils 등 공통 유틸
│   ├── component/                         # 재사용 UI 컴포넌트
│   │   ├── button, toggle, text, divider, image, navigation, guage
│   │   ├── chart/                         # 혈당 추이 차트
│   │   ├── chat/                          # AI 챗봇(당당이) 관련 컴포넌트
│   │   ├── map/                           # 카카오맵 컴포넌트
│   │   └── page/                          # 화면별 전용 컴포넌트 (home, walk, community, mypage, signup)
│   ├── data/
│   │   ├── api/                           # Retrofit API 인터페이스 (UserApiService 등)
│   │   ├── model/                         # 데이터 모델 (chat, community, home, user, walk)
│   │   ├── enums/                         # 화면 상태/타입 Enum
│   │   └── repository/                    # Repository 계층
│   ├── di/                                # Hilt 모듈, 인터셉터, 세션/걸음수 매니저, StepCounterService
│   └── ui/
│       ├── navhost/                       # AppNavHost(로그인~메인 전환), MainNavHost(하단 탭 전환)
│       ├── screens/                       # 실제 화면(first: 로그인/회원가입, main, navigation: 탭별 화면)
│       ├── viewmodel/                     # 화면별 ViewModel
│       └── theme/                         # 컬러, 타이포그래피, 테마
└── app/src/main/res/                      # 이미지, 폰트(Noto Sans KR), 문자열 등 리소스
```

## 화면 흐름

1. **로그인 / 회원가입** (`AppRoute`: `login` → `signUp` → `signUpComplete`)
   - 이메일 로그인, 카카오/구글 소셜 로그인 지원
   - 자동 로그인 시 `main`으로 바로 진입 (`AppPrefs.isAutoLogin()`)
2. **메인 화면** (`main`) — 하단 네비게이션(`MainRoute`) 5개 탭으로 구성

   | 탭 | 설명 |
   |---|---|
   | 홈 (`home`) | 주간 혈당 체크, 식후 혈당 상태, 팀 챌린지 현황 요약 |
   | 당당이 (`dangdang`) | AI 챗봇과의 대화, 식단 입력·분석, 혈당 예측/피드백, 산책 추천 |
   | 걷기 (`walk`) | 카카오맵 기반 걷기(산책) 기록, 걸음 수 측정(Health Connect / Foreground Service) |
   | 커뮤니티 (`community`) | 팀 챌린지 랭킹, 팀 검색/생성(`teamMake`, `teamsearch`) |
   | 내 정보 (`mypage`) | 회원 정보 수정(`myInfoUpdate`), 로그아웃 등 |

## 네트워크

- `di/NetworkModule.kt`에서 Retrofit/OkHttp를 구성하며, `Application.API_BASE_URL`을 baseUrl로 사용합니다.
- `ApiInterceptor` + `SessionManager`가 인증 토큰 첨부 및 세션 관리를 담당합니다.
- 백엔드 연동 API는 `data/api/UserApiService.kt` 등에 정의되어 있습니다.

## 빌드 & 실행

Android Studio에서 `FrontEnd/DangDang` 디렉터리를 열어 실행하거나, 아래 Gradle Wrapper 명령을 사용합니다.

```bash
cd FrontEnd/DangDang
./gradlew assembleDebug
```

> 카카오맵 / 카카오 로그인 / Health Connect 등 실기기(또는 에뮬레이터) 권한 및 API 키 설정이 필요합니다. 관련 키는 `AndroidManifest.xml`, `Application.kt`를 참고하세요.

### 필요 권한

- `INTERNET`, `ACCESS_FINE/COARSE_LOCATION`(걷기 경로), `ACTIVITY_RECOGNITION`(걸음 수), `FOREGROUND_SERVICE`/`FOREGROUND_SERVICE_HEALTH`, `POST_NOTIFICATIONS`, `CAMERA`(프로필/식단 사진 촬영)

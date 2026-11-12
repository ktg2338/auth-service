# Auth Service

경진대회 플랫폼을 위한 인증/인가 마이크로서비스입니다. 로컬 로그인 및 OAuth2 소셜 로그인(Google, Kakao)을 지원하며, JWT 기반의 Stateless 인증 아키텍처로 설계되었습니다.

## 기술 스택

| 구성요소 | 기술 |
|----------|------|
| 언어 | Kotlin 2.2.0 |
| JDK | Java 21 |
| 프레임워크 | Spring Boot 3.5.6 |
| 보안 | Spring Security 6.x + OAuth2 Client + OIDC |
| 데이터베이스 | PostgreSQL |
| ORM | Spring Data JPA + QueryDSL |
| 캐시/세션 | Redis (Lettuce) |
| JWT | JJWT 0.12.5 |
| API 문서 | SpringDoc OpenAPI 2.8.13 |
| 모니터링 | Azure Application Insights |
| 배포 | Azure Container Apps |
| CI/CD | GitHub Actions |

## 프로젝트 구조

```
src/main/kotlin/authservice/
├── CompetitionApplication.kt              # 메인 애플리케이션
├── config/
│   ├── SecurityConfig.kt                  # Spring Security + OAuth2 설정
│   ├── JwtProperties.kt                   # JWT 속성 설정
│   ├── RedisConfig.kt                     # Redis 설정
│   ├── SwaggerConfig.kt                   # OpenAPI/Swagger 설정
│   └── QueryDslConfig.kt                  # QueryDSL 설정
├── controller/
│   └── AuthController.kt                  # 인증 API 엔드포인트
├── service/
│   ├── AuthService.kt                     # 핵심 인증 로직 (회원가입, 로그인, 토큰 갱신, 로그아웃)
│   ├── CustomOAuth2UserService.kt         # OAuth2 사용자 로딩 (Kakao)
│   ├── CustomOidcUserService.kt           # OIDC 사용자 로딩 (Google)
│   ├── CustomUserDetailsService.kt        # Spring Security UserDetailsService
│   └── RedisService.kt                    # Redis 래퍼
├── entity/
│   ├── User.kt                            # 사용자 엔티티
│   ├── OAuth2Subject.kt                   # OAuth2 제공자 매핑
│   ├── OAuth2Provider.kt                  # ENUM (GOOGLE, KAKAO)
│   └── UserRole.kt                        # ENUM (USER, ADMIN)
├── repository/
│   ├── UserRepository.kt                  # 사용자 JPA 리포지토리
│   └── OAuth2SubjectRepository.kt         # OAuth2 Subject 리포지토리
├── security/
│   ├── JwtTokenProvider.kt                # JWT 생성, 검증, 폐기
│   ├── JwtAuthenticationFilter.kt         # 쿠키에서 JWT 추출 필터
│   ├── CustomUserDetails.kt               # UserDetails 구현
│   └── OAuth2UserInfoFactory.kt           # OAuth2 사용자 정보 팩토리
├── handler/
│   ├── OAuth2SuccessHandler.kt            # OAuth2 인증 성공 핸들러
│   └── OAuth2FailureHandler.kt            # OAuth2 인증 실패 핸들러
├── dto/                                   # 요청/응답 DTO
│   ├── request/                           # LoginRequest, SignUpRequest
│   ├── response/                          # LoginResponse, ErrorResponse
│   ├── common/                            # TokenDto
│   └── oAuth2/                            # OAuth2UserInfo (Google, Kakao)
└── exception/
    ├── CustomException.kt                 # 커스텀 예외 클래스
    └── GlobalExceptionHandler.kt          # 전역 예외 핸들러
```

## 인증 흐름

### 로컬 인증

```
회원가입: POST /auth/signup → 비밀번호 BCrypt 암호화 → DB 저장
로그인:   POST /auth/login  → 자격 증명 검증 → JWT 발급 → HTTP-only 쿠키 설정
갱신:     POST /auth/refresh → Refresh Token 검증 → 새 토큰 발급 (이전 토큰 폐기)
로그아웃: POST /auth/logout  → Redis에서 토큰 폐기
```

### OAuth2 소셜 로그인

```
1. /oauth2/authorize/{provider} → 제공자(Google/Kakao)로 리다이렉트
2. 사용자 인증 후 → /login/oauth2/code/{provider}로 콜백
3. 기존 사용자 → JWT 발급 및 로그인 완료
   신규 사용자 → signup_required 응답 (프론트엔드에서 회원가입 처리)
```

### 토큰 정책

| 토큰 | 유효기간 | 저장 위치 |
|------|----------|-----------|
| Access Token | 1시간 | HTTP-only 쿠키 |
| Refresh Token | 7일 | HTTP-only 쿠키 + Redis |

- 새 로그인/갱신 시 이전 Refresh Token 자동 폐기 (동시 세션 방지)

## API 엔드포인트

### 공개 엔드포인트

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/auth/signup` | 회원가입 |
| POST | `/auth/login` | 로그인 |
| POST | `/auth/refresh` | 토큰 갱신 |
| POST | `/auth/logout` | 로그아웃 |
| GET | `/oauth2/authorize/google` | Google 로그인 |
| GET | `/oauth2/authorize/kakao` | Kakao 로그인 |
| GET | `/actuator/health` | 헬스체크 |
| GET | `/swagger-ui.html` | API 문서 |

## 시작하기

### 사전 요구사항

- Java 21+
- PostgreSQL
- Redis
- Google / Kakao OAuth2 클라이언트 자격 증명

### 환경변수

| 변수명 | 설명 | 필수 |
|--------|------|------|
| `SPRING_PROFILES_ACTIVE` | 활성 프로필 (`local`, `dev`, `prod`) | X (기본: local) |
| `DB_URL` | PostgreSQL JDBC URL | O |
| `DB_USERNAME` | 데이터베이스 사용자명 | O |
| `DB_PASSWORD` | 데이터베이스 비밀번호 | O |
| `REDIS_HOST` | Redis 호스트 | O |
| `REDIS_PORT` | Redis 포트 | O |
| `AZURE_REDIS_PASSWORD` | Azure Redis 비밀번호 | O (prod) |
| `JWT_SECRET` | JWT 서명 키 (최소 32자) | O |
| `GOOGLE_CLIENT_ID` | Google OAuth2 클라이언트 ID | O |
| `GOOGLE_CLIENT_SECRET` | Google OAuth2 클라이언트 시크릿 | O |
| `KAKAO_CLIENT_ID` | Kakao OAuth2 클라이언트 ID | O |
| `KAKAO_CLIENT_SECRET` | Kakao OAuth2 클라이언트 시크릿 | O |
| `REDIRECT_URI` | OAuth2 콜백 URI | O |
| `COOKIE_DOMAIN` | 쿠키 도메인 | O |

### 로컬 실행

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
# 포트 8081에서 실행
```

### 빌드

```bash
./gradlew bootJar    # JAR 빌드
./gradlew build      # 전체 빌드
./gradlew test       # 테스트 실행
```

### Docker

```bash
docker build -t auth-service:latest .
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL="jdbc:postgresql://..." \
  -e DB_USERNAME="user" \
  -e DB_PASSWORD="pass" \
  -e REDIS_HOST="localhost" \
  -e JWT_SECRET="your-secret-key-at-least-32-chars" \
  auth-service:latest
```

## 프로필별 설정

| 프로필 | 포트 | 용도 |
|--------|------|------|
| `local` | 8081 | 로컬 개발 (테스트 OAuth2 자격 증명 내장) |
| `prod` | 8080 | 운영 환경 (Azure Redis SSL, JSON 로깅) |

## 예외 처리

| 예외 | HTTP 상태 |
|------|-----------|
| `InvalidTokenException` | 401 Unauthorized |
| `TokenExpiredException` | 401 Unauthorized |
| `BadCredentialsException` | 401 Unauthorized |
| `DuplicateUsernameException` | 409 Conflict |
| `DuplicateEmailException` | 409 Conflict |
| `UsernameNotFoundException` | 404 Not Found |
| `RedisConnectionException` | 503 Service Unavailable |

## CI/CD

GitHub Actions 워크플로우 (`.github/workflows/deploy.yml`)가 `main` 브랜치 푸시 시 실행:

1. Gradle로 JAR 빌드
2. Docker 이미지 빌드 후 Azure Container Registry에 푸시
3. Azure Container Apps 업데이트
4. 레지스트리 정리 (최신 10개 이미지만 유지)

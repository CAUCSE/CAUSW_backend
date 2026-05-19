# 보안과 인증

`core/security/` 와 `domain/user/auth/` 가 협력해 처리하는 인증 / 인가 흐름입니다.

## 1. 핵심 구성 요소

| 구성 요소 | 위치 |
|----------|------|
| 보안 설정 | `core/security/WebSecurityConfig` |
| JWT 발급 / 검증 | `core/security/JwtTokenProvider` |
| JWT 인증 필터 | `core/security/JwtAuthenticationFilter` |
| 인가 매니저 | `core/security/CustomAuthorizationManager` |
| 인증 실패 핸들러 | `core/security/CustomAuthenticationEntryPoint` |
| Apple OAuth2 리졸버 | `core/security/AppleOAuth2AuthorizationRequestResolver` |
| OAuth2 쿠키 저장소 | `core/security/OAuth2AuthorizationRequestCookieRepository` |
| 보호 경로 정의 | `core/security/SecurityEndpoints` |
| OAuth2 사용자 서비스 | `domain/user/auth/service/CustomOAuth2UserService` |
| OAuth2 성공/실패 핸들러 | `domain/user/auth/handler/OAuth2SuccessHandler`, `OAuth2FailureHandler` |
| 보호된 사용자 상세 | `domain/user/auth/userdetails/CustomUserDetails` |
| Swagger 보안 설정 | `core/security/SwaggerSecurityConfig` |
| Web 보안 부가 설정 | `core/security/WebConfig`, `OctetStreamReadMsgConverter`, `RequestAuthorizationBinder` |

## 2. SecurityFilterChain

`core/security/WebSecurityConfig` 가 `@EnableWebSecurity` + `@EnableMethodSecurity` 를 활성화하고, 여러 개의 `SecurityFilterChain` 을 `@Order` 로 우선순위를 매기는 구조입니다.

- `securityFilterChainV2` (`@Order(1)`) — `/api/v2/**`, `/oauth2/**`, `/login/oauth2/**` 매칭
- `securityFilterChainV1` (`@Order(2)`) — 그 외 경로 매칭

공통 정책:
- **STATELESS** 세션 정책 (JWT 기반, HTTP 세션 안 씀)
- CSRF 비활성화 (REST API)
- CORS 활성화 (`app.cors.allowed-origins` 환경변수로 origin 목록 주입)
- `JwtAuthenticationFilter` 가 `UsernamePasswordAuthenticationFilter` 보다 먼저 실행

## 3. JWT 토큰

라이브러리: `io.jsonwebtoken:jjwt 0.9.x` (`JwtTokenProvider` 가 사용)

| 토큰 | 위치 | 수명 |
|------|------|------|
| Access Token | `Authorization: Bearer <token>` 헤더 | 짧음 (분 단위) |
| Refresh Token | 헤더 전달 (이전 Cookie 방식에서 마이그레이션됨) | 길음 (일/주 단위) |

발급 흐름 (대표):
1. 로그인 성공 (자체 / OAuth2)
2. `JwtTokenProvider` 가 access + refresh 발급
3. 응답 헤더 또는 본문으로 반환
4. Refresh Token 은 Redis 에 저장 (재발급 시 검증)

검증 흐름:
1. `JwtAuthenticationFilter` 가 모든 요청에서 `Authorization` 헤더 검사
2. 유효하면 `SecurityContextHolder` 에 `CustomUserDetails` 채움
3. 만료 / 위변조 시 글로벌 `ErrorCode.EXPIRED_JWT` / `INVALID_JWT` 발생

jjwt 0.9.x 는 오래된 버전입니다. 0.11.x+ 의 새 API 를 도입할 때 호환성 확인이 필요합니다.

## 4. OAuth2

지원 제공자:
- Google
- Apple (iOS / Web — `AppleOAuth2AuthorizationRequestResolver` 가 client_id, nonce 등 별도 처리)

설정 위치:
- `application-{profile}.yml` 의 `spring.security.oauth2.client.registration.*`
- 콜백 URL: `/login/oauth2/code/{provider}`
- ID Token 추가 검증: `OidcIdTokenDecoderFactory` 커스터마이즈 (`WebSecurityConfig`)

흐름:
1. 클라이언트가 `/oauth2/authorization/{provider}` 로 이동
2. 인증 후 콜백 → `CustomOAuth2UserService` 가 사용자 정보 처리 / 신규 가입
3. `OAuth2SuccessHandler` 가 자체 JWT 발급 후 응답
4. 실패 시 `OAuth2FailureHandler` 가 처리

## 5. 비밀번호

- 해시: BCrypt (`WebSecurityConfig` 의 `BCryptPasswordEncoder` Bean)
- 자체 가입 시: 형식 검증 (`UserErrorCode.INVALID_PASSWORD_REQUEST`) → BCrypt 인코딩 → 저장
- 소셜 가입자: `password` NULL (비밀번호 변경 API 호출 시 `SOCIAL_ONLY_USER_CANNOT_CHANGE_PASSWORD` 발생)

## 6. 인가 (Authorization)

### 메서드 시큐리티

```java
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/admin/users")
public ApiResponse<List<UserResponse>> listUsers() { ... }
```

- `domain/user/account/enums/user/Role` enum 의 각 값이 SpEL 표현식의 `ROLE_{Role}` 로 매핑됨
- 다중 역할: `hasAnyRole('ADMIN', 'PRESIDENT')`
- 복잡 조건은 `CustomAuthorizationManager` 활용

### URL 기반 인가

`authorizeHttpRequests` 안에서 경로/Method 별 권한 지정 (`WebSecurityConfig` 의 v1/v2 chain 내부).

`core/security/SecurityEndpoints` 가 공개 / 인증 필요 URL 목록을 관리.

## 7. CORS

```yaml
app:
  cors:
    allowed-origins: https://causw.net,http://localhost:3000
```

- `WebSecurityConfig` 가 `@Value("${app.cors.allowed-origins:http://localhost:3000}")` 로 읽음
- 운영 환경 origins 는 `.env` / GitHub Secrets 로 관리 (`docs/env_guide.md`)

## 8. 인증 정보 사용

Controller 메서드에서 인증된 사용자 정보 추출:

```java
@AuthenticationPrincipal CustomUserDetails userDetails
User user = userDetails.getUser();
```

## 9. 보안 체크리스트

- [ ] 신규 API 의 `SecurityFilterChain` 매칭 경로에 포함되었는지 확인 (v1 / v2 chain)
- [ ] 공개 API 는 `permitAll()`, 인증 필요 API 는 기본값 `authenticated()`, 역할 제한 API 는 `@PreAuthorize`
- [ ] `CustomUserDetails` 사용으로 일관된 사용자 추출
- [ ] CSRF/CORS 정책이 운영 환경에서 적절한지 확인
- [ ] 민감 정보 응답에서 마스킹 (`UuidFile` URL, 학번, 전화번호 등 — `account/util/masking/` 활용)
- [ ] JWT 만료 처리 / Refresh Token 회전(rotate) 정책 확인
- [ ] 로그에 토큰 / 비밀번호 출력 금지

## 10. 운영 주의

- JWT 시크릿(`JWT_SECRET_KEY`) 노출 시 즉시 재발급 + 모든 토큰 무효화
- Apple 키 / 인증서 갱신 일정 모니터링
- `keystore.p12` (HTTPS) 갱신 일정 모니터링

# user 도메인

위치: `app-main/src/main/java/net/causw/app/main/domain/user`

## 책임

회원의 **계정 / 인증 / 학적 정보 / 약관 동의 / 사용자 간 관계** 를 책임집니다. 사실상 다른 모든 도메인이 의존하는 가장 기본 도메인입니다.

## 서브 도메인

```
domain/user/
├── account/      # 사용자 본 계정, 가입, 프로필 정보
├── auth/         # 인증, JWT, OAuth2 로그인, 비밀번호
├── terms/        # 약관 동의
├── academic/     # 학적 상태, 학년/졸업 등
└── relation/     # 사용자 간 관계 (차단 등)
```

## account — 사용자 계정

엔티티 (`account/entity/` 하위):
- `User` — `tb_user`
  - 핵심 필드: `email`, `name`, `phoneNumber`, `password`, `studentId`, `admissionYear`, `nickname`, `department`, `graduationType`, `graduationYear`
  - 상태: `Role`, `RoleGroup`, `UserState`, `AcademicStatus`, `ProfileImageType`
  - 연관: `Set<CircleMember>` (동아리 가입), `List<VoteRecord>` (투표 이력)
- `UserInfo` — 사용자 부가 정보

주요 enum (`account/enums/` 하위):
- `Role` — `ADMIN`, `PRESIDENT`, `VICE_PRESIDENT`, `LEADER_*`, `COMMON` 등
- `RoleGroup` — 권한 그룹 (다중 Role 묶음)
- `UserState` — `AWAIT`, `ACTIVE`, `INACTIVE`, `DROP`, `DELETED` 등
- `Department` — 소속 학과
- `GraduationType` — 졸업 구분
- `ProfileImageType` — `DEFAULT_*` / `CUSTOM` / `UNSET` (`UNSET` 은 프로필 미설정 상태)

API:
- v1: `account/api/v1/` — 회원 가입 / 조회 / 수정 등 레거시
- v2: `account/api/v2/` — 신규 표준 API

서비스 (`account/service/` 하위):
- `implementation/` 에 `UserReader`, `UserWriter`, `UserValidator`, `UserInfoReader`, `UserInfoWriter`, `UserInfoCreator`, `AdmissionReader`, `AdmissionWriter`, `AdmissionLogWriter`, `AdmissionValidator`, `SocialAccountReader`, `SocialAccountWriter`, `SocialAccountLinker`, `SocialAccountUnlinkManager`, `DroppedUserIdentifierWriter`, `UserAdminActionLogWriter` 등 다양한 책임별 컴포넌트
- `service/v1/` — 레거시 서비스
- `service/dto/{request,response,result}/` — 서비스 계층 DTO (세분화됨)
- `service/mapper/` — 매퍼

util:
- `account/util/masking/` — 이메일 등 마스킹 유틸 (`EmailMasker` 등)

ErrorCode:
- `UserErrorCode` — 70+ 가지 (가입, 로그인, 상태 검증 등)
- `UserInfoErrorCode`

## auth — 인증

`domain/user/auth/` 하위 + `core/security/` 의 보안 설정이 협력합니다.

- 인증 정보 클래스: `auth/userdetails/CustomUserDetails`
- OAuth2 사용자 처리: `auth/service/CustomOAuth2UserService`
- 성공/실패 핸들러: `auth/handler/OAuth2SuccessHandler`, `OAuth2FailureHandler`
- 비밀번호 / 토큰 처리: `auth/service/implementation/` 하위 (OAuth2 리프레시 토큰 캡처 등)
- JWT 발급/검증: `core/security/JwtTokenProvider`, `JwtAuthenticationFilter`
- Apple OAuth2 특화: `core/security/AppleOAuth2AuthorizationRequestResolver`

지원 소셜 로그인:
- Google
- Apple (iOS / 웹)
- 추가 제공자는 `application-{profile}.yml` 의 `spring.security.oauth2.client.registration.*` 에서 확인

토큰 흐름:
- **Access Token** — 짧은 수명. `Authorization: Bearer ...` 헤더
- **Refresh Token** — 긴 수명. 헤더 전달 방식 (이전 Cookie 방식에서 마이그레이션됨)

ErrorCode: `AuthErrorCode` (토큰 만료, JWT 위변조, 소셜 로그인 실패 등)

보안 설정 전반: [../cross-cutting/security-and-auth.md](../cross-cutting/security-and-auth.md).

## terms — 약관 동의

`terms/entity/` 하위:
- 가입 시 약관 동의 이력 저장
- 약관 버전 관리

API:
- v2: `terms/api/v2/` — 약관 조회 / 동의 처리

ErrorCode: `TermsErrorCode`

## academic — 학적 기록

`academic/entity/userAcademicRecord/` 하위:
- `UserAcademicRecord` — 학적 신청 / 변경 이력
- 학년 / 졸업 등 정보 변경 워크플로

enum: `AcademicStatus` — 재학, 휴학, 졸업 등

API: v1, v2 모두 존재

ErrorCode: `AcademicRecordApplicationErrorCode`

## relation — 사용자 간 관계

`relation/entity/userBlock/` 하위:
- `UserBlock` — 사용자 차단 기능

API: v1, v2 모두 존재

ErrorCode: `BlockErrorCode`

비즈니스 규칙:
- 차단된 사용자가 작성한 게시글/댓글은 community 도메인에서 조회 시 필터링
- 차단 자체 알림은 발송하지 않음

## 외부 도메인과의 관계

| 사용처 | 의존 방식 |
|--------|----------|
| community | `User` 를 게시글/댓글 작성자로 참조. `UserReader` 주입 |
| campus/circle | `User` 가 `CircleMember` 로 동아리에 가입 |
| notification | 알림 수신자로 `User` 참조, `UserNotificationSetting` 보유 |
| finance | `User` 가 학생회비 납부자 |

## 주의 사항 / 트레이드오프

- v1 / v2 가 동시에 활성 — 어느 한쪽만 보고 동작을 추론하지 말 것
- `User` 엔티티가 매우 두꺼움 — 신규 필드 추가 시 다른 도메인 영향 검토 필요
- `password` 는 BCrypt 해시 (`WebSecurityConfig` 의 `BCryptPasswordEncoder` Bean)
- 소셜 전용 가입자는 `password` 가 NULL — 비밀번호 변경 API 호출 시 별도 처리 (`SOCIAL_ONLY_USER_CANNOT_CHANGE_PASSWORD`)

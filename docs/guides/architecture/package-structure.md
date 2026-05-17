# 패키지 구조 상세

`app-main` 모듈의 메인 소스(`net.causw.app.main`) 패키지 트리와 각 디렉터리의 역할입니다. `global` 모듈은 별도 §5 참조.

## 1. 최상위 트리

```
net.causw.app.main
├── CauswApplication          # @SpringBootApplication entrypoint
├── core/                     # 플랫폼 / 인프라 횡단 관심사
├── domain/                   # 비즈니스 도메인 (8개)
└── shared/                   # 도메인 간 공용 컴포넌트
```

## 2. `core/` — 플랫폼 횡단 관심사

| 패키지 | 역할 | 어떤 클래스를 찾을 수 있나 |
|--------|------|---------|
| `core/aop` | Spring AOP | `LogAspect`(`@MeasureTime` 메서드 실행 시간 측정), `annotation/MeasureTime` |
| `core/batch` | Spring Batch 잡 | 데이터 정리 등 정기 배치 잡 (예: 미사용 UUID 파일 정리) |
| `core/config` | Bean / 설정 클래스 | `async/`, `batch/`, `flyway/`, `persistence/`, `querydsl/`, `scheduling/`, `swagger/` 하위에 도메인별 `*Config` 클래스 |
| `core/datasourceProxy` | DataSource 프록시 | `ApiQueryCountListener`, `ApiQueryLoggingAspect`, `QueryContext` — 요청별 쿼리 카운트/로깅 |
| `core/favicon` | 정적 리소스 매핑 | 파비콘 응답 |
| `core/filter` | Servlet Filter | 요청/응답 전처리 |
| `core/global` | core 내부 공통 | core 내부 글로벌 설정 |
| `core/security` | Spring Security / 인증 | `WebSecurityConfig`, `JwtTokenProvider`, `JwtAuthenticationFilter`, `AppleOAuth2AuthorizationRequestResolver` 등. 상세는 [../cross-cutting/security-and-auth.md](../cross-cutting/security-and-auth.md) |

## 3. `domain/` — 비즈니스 도메인

8개의 최상위 도메인 패키지로 구분됩니다. 각 도메인은 하나 이상의 **서브 도메인** 으로 분할되어 있고, 서브 도메인 단위로 표준 레이어가 적용됩니다.

```
domain/
├── asset/
│   ├── file/                 # 파일 업로드 (UuidFile 중심)
│   └── locker/               # 사물함 관리
├── campus/
│   ├── circle/               # 동아리
│   ├── event/                # 행사
│   ├── schedule/             # 학사 일정
│   └── semester/             # 학기
├── community/
│   ├── board/                # 게시판 / 카테고리
│   ├── ceremony/             # 결혼/장례 등 경조사
│   ├── comment/              # 댓글 / 대댓글
│   ├── form/                 # 게시글 첨부 폼
│   ├── homepage/             # 메인 페이지 노출 로직
│   ├── post/                 # 게시글
│   ├── reaction/             # 좋아요 / 즐겨찾기
│   ├── report/               # 신고
│   └── vote/                 # 투표
├── etc/
│   ├── api/                  # 외부 API 호출 로그/관리
│   ├── flag/                 # 기능 플래그
│   └── textfield/            # 정책/공지 등 텍스트
├── finance/
│   └── usercouncilfee/       # 학생회비
├── integration/
│   ├── crawled/              # 외부 공지사항 크롤링
│   └── export/               # 데이터 내보내기 (Excel/CSV)
├── notification/
│   └── notification/         # 알림 발송 / 구독 / 설정
└── user/
    ├── academic/             # 학적 기록
    ├── account/              # 계정 / 회원 정보
    ├── auth/                 # 인증 / OAuth2
    ├── relation/             # 차단 등 관계
    └── terms/                # 약관 동의
```

각 도메인의 책임/주요 엔티티: [../domains/](../domains/).

### 서브 도메인 내부 표준 레이어

```
{subDomain}/
├── api/
│   ├── v1/
│   │   ├── controller/        # @RestController, /api/v1/...
│   │   ├── dto/               # 요청/응답 DTO (분리 안 된 평면 구조)
│   │   └── mapper/            # *DtoV1Mapper (MapStruct)
│   └── v2/
│       ├── controller/        # @RestController, /api/v2/...
│       ├── dto/
│       │   ├── request/
│       │   └── response/
│       └── mapper/            # *DtoMapper (MapStruct)
├── service/                   # 트랜잭션 경계 서비스 + 영속화 컴포넌트
│   ├── v1/                    # 레거시
│   ├── v2/                    # 현재 표준
│   └── (도메인별 변형 — §3.1)
├── repository/
│   ├── *Repository.java       # JpaRepository<Entity, String> 상속
│   └── query/
│       └── *QueryRepository.java   # QueryDSL JPAQueryFactory 사용
├── entity/                    # JPA Entity (BaseEntity 상속)
└── enums/                     # Role, State 등 enum
```

### 3.1 service 디렉터리의 도메인별 변형

표준 레이아웃은 위와 같지만 service 하위는 도메인마다 변형이 있습니다.

| 패턴 | 예 |
|------|-----|
| service 직속에 `implementation/` (v1/v2 와 동등 위치) | `user/account`, `user/auth` 등 |
| v2 안에 `service/v2/implementation/` | `community/post`, `asset/file`, `asset/locker` 등 |
| v2 에 `*Service.java` 직접 두고 별도 implementation 없음 | `community/report` |
| `service/v1/validators/` 같은 도메인 특화 패키지 추가 | `asset/locker` |
| `service/handler/`, `service/util/` 등 추가 보조 패키지 | `notification/notification` |

**원칙**: 새 서브 도메인을 만들 때 가장 비슷한 책임의 기존 서브 도메인의 구조를 따라가는 편이 안전합니다.

### 3.2 서브 도메인 내부 예시 — `community/post`

| 경로 | 내용 |
|------|------|
| `api/v2/controller/PostController` | `/api/v2/posts` Controller |
| `api/v2/dto/request/PostCreateRequest` | 게시글 생성 요청 DTO |
| `api/v2/mapper/PostDtoMapper` | MapStruct 매퍼 |
| `service/v2/PostService` | 트랜잭션 경계 서비스 |
| `service/v2/implementation/PostReader` | 조회 전용 Component |
| `service/v2/implementation/PostWriter` | 변경 전용 Component |
| `service/v2/implementation/PostImageManager` | 첨부 이미지 흐름 관리 |
| `repository/PostRepository` | Spring Data JPA |
| `repository/query/PostQueryRepository` | QueryDSL 동적 쿼리 |
| `entity/Post` | JPA Entity (`tb_post`) |

## 4. `shared/` — 도메인 간 공용

| 패키지 | 역할 | 어떤 클래스를 찾을 수 있나 |
|--------|------|---------|
| `shared/dto` | 공용 응답 DTO | `ApiResponse`, `PageResponse`, `ProfileImageDto`, `dto/util/dtoMapper/custom/UuidFileToUrlDtoMapper` 등 |
| `shared/entity` | JPA 베이스 클래스 | `BaseEntity`, `AuditableEntity` |
| `shared/exception` | 예외 / 핸들러 / ErrorCode | `GlobalV1ExceptionHandler`, `GlobalV2ExceptionHandler`, `BaseRunTimeV2Exception`, `BaseResponseCode`, `errorcode/*ErrorCode` |
| `shared/infra` | 외부 인프라 클라이언트 | `redis/`, `mail/`, `push/`, `firebase/`, `storage/` |
| `shared/pageable` | 페이징 응답 / 유틸 | Spring Data Pageable 래핑 |
| `shared/seed` | 초기 시드 데이터 | 더미 / 기본 데이터 |
| `shared/storage` | 파일 스토리지 추상화 | `v1/`, `v2/` 두 버전 |
| `shared/util` | 공용 유틸 | 날짜/문자열/이미지 등 |
| `shared/AbstractValidator` | Validator 추상 클래스 | `validate()` 추상 메서드 |
| `shared/ValidatorBucket` | Validator 체이닝 컨테이너 | `of().consistOf(...).validate()` 패턴 |
| `shared/StatusPolicy` | 상태 정책 | 권한/상태 기반 분기 정책 |

## 5. `global` 모듈 (`net.causw.global`)

`app-main` 외부에서도 사용 가능한 가장 기초적인 공용 코드만 포함합니다. Spring 의존성은 최소화합니다.

| 패키지 | 역할 | 어떤 클래스를 찾을 수 있나 |
|--------|------|---------|
| `global/constant` | HTTP 상태 / 메시지 / 상수 | `HttpStatusCodes`, `MessageUtil`, `StaticValue` |
| `global/exception` | 공용 예외 / ErrorCode | `ErrorCode` (enum), `BaseRuntimeException`, `BadRequestException`, `UnauthorizedException`, `ForbiddenException`, `NotFoundException`, `InternalServerException`, `ServiceUnavailableException` |
| `global/util` | 의존성 없는 유틸 | `PatternUtil`, `HashUtil` |

제약: `global` 은 `app-main` 의 기능을 의존할 수 없습니다. 도메인 로직이나 Spring 관련 코드는 `app-main` 쪽에 추가합니다.

## 6. 리소스 디렉터리

```
app-main/src/main/resources/
├── application.yml                # 공통 설정 + 활성 프로파일 분기
├── application-local.yml          # 로컬 (flyway out-of-order 허용)
├── application-dev.yml            # 개발 (Flyway 자동 비활성)
├── application-prod.yml           # 운영 (Flyway 자동 비활성)
├── db/migration/                  # Flyway SQL 마이그레이션 (V*.sql)
├── keystore.p12                   # HTTPS 인증서
└── logback-spring.xml             # 로깅 설정
```

환경별 설정 차이 / Flyway 운영은 [../../flyway_guide.md](../../flyway_guide.md), [../../env_guide.md](../../env_guide.md).

## 7. 기타 루트 디렉터리

| 경로 | 역할 |
|------|------|
| `app-main/gradle/` | Flyway 작업, 스키마 검증 등 Gradle 스크립트 (`flyway/flyway-config.gradle`, `flyway/flyway-task.gradle`, `test/schema-validation.gradle`) |
| `app-main/naver-eclipse-formatter.xml` | Naver 코딩 컨벤션 Eclipse 포맷터 |
| `lombok.config` | Lombok 설정 (`addLombokGeneratedAnnotation=true`) |
| `monitoring/` | Grafana / Prometheus 등 모니터링 관련 설정 |
| `scripts/` | 배포 스크립트 (`deploy.sh` 등) |
| `appspec.yml` | AWS CodeDeploy 명세 |
| `img/` | README 등에서 사용하는 이미지 |

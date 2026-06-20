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
| `core/batch` | Spring Batch 잡 / 리스너 | `BatchScheduler`, `listener/` 하위 `*JobCompletionNotificationListener`, `*StepListener` |
| `core/config` | Bean / 설정 클래스 | `async/`, `batch/`, `flyway/`, `persistence/`, `querydsl/`, `scheduling/`, `swagger/` 하위에 도메인별 `*Config` 클래스 |
| `core/datasourceProxy` | DataSource 프록시 | `ApiQueryCountListener`, `ApiQueryLoggingAspect`, `QueryContext` — 요청별 쿼리 카운트/로깅 |
| `core/favicon` | 정적 리소스 매핑 | 파비콘 응답 |
| `core/filter` | Servlet Filter | 요청/응답 전처리 |
| `core/security` | Spring Security / 인증 | `WebSecurityConfig`, `JwtTokenProvider`, `JwtAuthenticationFilter`, `AppleOAuth2AuthorizationRequestResolver` 등. 상세는 [../cross-cutting/security-and-auth.md](../cross-cutting/security-and-auth.md) |

## 3. `domain/` — 비즈니스 도메인

8개의 최상위 도메인 패키지로 구분됩니다. 각 도메인은 하나 이상의 **서브 도메인** 으로 분할되어 있고, 서브 도메인 단위로 표준 레이어가 적용됩니다.

```
domain/
├── admin/
│   └── audit/                # 관리자 감사 로그
├── asset/
│   ├── file/                 # 파일 업로드 (UuidFile 중심)
│   └── locker/                # 사물함 관리
├── campus/
│   └── schedule/               # 학사 일정
├── community/
│   ├── board/                 # 게시판 / 카테고리
│   ├── ceremony/               # 결혼/장례 등 경조사
│   ├── comment/                # 댓글 / 대댓글
│   ├── form/                   # 게시글 첨부 폼
│   ├── post/                   # 게시글
│   ├── reaction/                # 좋아요 / 즐겨찾기
│   ├── report/                  # 신고
│   └── vote/                    # 투표
├── etc/
│   ├── flag/                   # 기능 플래그
│   └── textfield/                # 정책/공지 등 텍스트
├── integration/
│   └── crawled/                  # 외부 공지사항 크롤링
├── notification/
│   └── notification/              # 알림 발송 / 구독 / 설정
└── user/
    ├── academic/                  # 학적 기록
    ├── account/                    # 계정 / 회원 정보
    ├── auth/                        # 인증 / OAuth2
    ├── relation/                     # 차단 등 관계
    └── terms/                        # 약관 동의
```

### 서브 도메인 내부 표준 레이어

```
{subDomain}/
├── api/
│   └── v2/
│       ├── controller/        # @RestController, /api/v2/...
│       ├── dto/
│       │   ├── request/
│       │   └── response/
│       └── mapper/            # *DtoMapper (MapStruct)
├── service/                   # 트랜잭션 경계 서비스 + 영속화 컴포넌트
│   ├── dto/
│   ├── implementation/        # Reader / Writer / Validator 등
│   └── (도메인별 변형 — §3.1)
├── repository/
│   ├── *Repository.java       # JpaRepository<Entity, String> 상속
│   └── query/
│       └── *QueryRepository.java   # QueryDSL JPAQueryFactory 사용
├── entity/                    # JPA Entity (BaseEntity 상속)
└── enums/                     # Role, State 등 enum
```

API 경로는 `api/v2/` 로 버저닝되어 있지만(향후 확장을 고려한 디렉터리 명명), v1 은 모두 제거되어 현재 공존하는 버전은 v2 뿐입니다. 서비스 레이어는 버전 디렉터리 없이 `service/` 바로 아래에 위치합니다.

### 3.1 service 디렉터리의 도메인별 변형

표준 레이아웃은 위와 같지만 service 하위는 도메인마다 변형이 있습니다.

| 패턴 | 예 |
|------|-----|
| `service/implementation/` 직속 | 대부분의 서브 도메인 (`user/account`, `community/post`, `asset/file`, `asset/locker` 등) |
| 별도 implementation 없이 `service/`에 `*Service.java` 직접 | `community/report` |
| `service/util/`, `service/mapper/`, `service/listener/`, `service/event/` 등 보조 패키지 추가 | `notification/notification`, `community/post` |
| `repository/projection/` 으로 native query 결과 매핑 추가 | `community/report` |

**원칙**: 새 서브 도메인을 만들 때 가장 비슷한 책임의 기존 서브 도메인의 구조를 따라가는 편이 안전합니다.

### 3.2 서브 도메인 내부 예시 — `community/post`

| 경로 | 내용 |
|------|------|
| `api/v2/controller/PostController` | `/api/v2/posts` Controller |
| `api/v2/dto/request/PostCreateRequest` | 게시글 생성 요청 DTO |
| `api/v2/mapper/PostDtoMapper` | MapStruct 매퍼 |
| `service/PostService` | 트랜잭션 경계 서비스 |
| `service/implementation/PostReader` | 조회 전용 Component |
| `service/implementation/PostWriter` | 변경 전용 Component |
| `service/util/PostCursorManager` | 커서 기반 페이징 유틸 |
| `repository/PostRepository` | Spring Data JPA |
| `repository/query/PostQueryRepository` | QueryDSL 동적 쿼리 |
| `entity/Post` | JPA Entity (`tb_post`) |

## 4. `shared/` — 도메인 간 공용

| 패키지 | 역할 | 어떤 클래스를 찾을 수 있나 |
|--------|------|---------|
| `shared/dto` | 공용 응답 DTO | `ApiResponse`, `PageResponse`, `dto/util/dtoMapper/custom/UuidFileToUrlDtoMapper` 등 |
| `shared/entity` | JPA 베이스 클래스 | `BaseEntity`, `AuditableEntity` |
| `shared/exception` | 예외 / 핸들러 / ErrorCode | `GlobalV2ExceptionHandler`, `BaseRunTimeV2Exception`, `BaseResponseCode`, `errorcode/*ErrorCode` |
| `shared/infra` | 외부 인프라 클라이언트 | `redis/`, `mail/`, `push/`, `firebase/`, `storage/` |
| `shared/pageable` | 페이징 응답 / 유틸 | `PageableFactory` |
| `shared/seed` | 초기 시드 데이터 | `UserSeeder`, `BoardSeeder`, `PostSeeder` 등 더미 데이터 |
| `shared/storage` | 파일 스토리지 추상화 | `StorageClient` 인터페이스 + `S3StorageClient`, `LocalStorageClient` 구현체 |
| `shared/util` | 공용 유틸 | 날짜/문자열/이미지 등 |
| `shared/AbstractValidator` | Validator 추상 클래스 | `validate()` 추상 메서드 |

## 5. `global` 모듈 (`net.causw.global`)

`app-main` 외부에서도 사용 가능한 가장 기초적인 공용 코드만 포함합니다. Spring 의존성은 최소화합니다.

| 패키지 | 역할 | 어떤 클래스를 찾을 수 있나 |
|--------|------|---------|
| `global/constant` | HTTP 상태 / 메시지 / 상수 | `HttpStatusCodes`, `MessageUtil`, `StaticValue` |
| `global/exception` | 공용 예외 / ErrorCode | `ErrorCode` (enum), `BaseRuntimeException`, `BadRequestException`, `UnauthorizedException`, `ForbiddenException`, `NotFoundException`, `InternalServerException`, `ServiceUnavailableException` |
| `global/util` | 의존성 없는 유틸 | `PatternUtil`, `HashUtil` |

제약: `global` 은 `app-main` 의 기능을 의존할 수 없습니다. 도메인 로직이나 Spring 관련 코드는 `app-main` 쪽에 추가합니다.

`global/exception` 의 예외들은 `core/security`, 일부 `domain/*/service` 의 보조 유틸 등 cross-cutting 코드에서 여전히 직접 사용되며, `shared/exception/GlobalV2ExceptionHandler` 가 함께 처리합니다. 상세: [../conventions/exception.md](../conventions/exception.md).

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
| `img/` | 문서에서 사용하는 이미지 |

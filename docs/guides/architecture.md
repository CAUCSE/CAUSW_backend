# 아키텍처

Spring Boot 4.0 / Java 25 기반 멀티 모듈 Gradle 프로젝트입니다. 코드 구조는 **도메인 중심 레이어드(Layered by Domain)** 패턴을 따릅니다 (`api → service → repository → entity`).

> 이 문서는 자주 바뀌지 않는 구조 원칙만 다룹니다. 도메인/서브도메인 목록처럼 PR 한 번에도 바뀔 수 있는 항목은 일부러 자세히 나열하지 않으니, 정확한 현황은 코드(`domain/` 디렉터리)를 직접 확인하세요.

## 1. 모듈 구성

루트 `settings.gradle` 기준 멀티 모듈 구성입니다.

```
CAUSW_backend (root)
├── global       # 다른 모듈에서도 공용 사용 가능한 유틸/상수/공통 예외
└── app-main     # Spring Boot 애플리케이션 본체 (entrypoint 포함)
```

| 모듈 | 패키지 prefix | 역할 |
|------|----------------|------|
| `global` | `net.causw.global` | 의존성을 최소화한 공용 유틸 (`util`, `constant`), HTTP 상수, 공용 예외 (`ErrorCode`, `BaseRuntimeException` 계열) |
| `app-main` | `net.causw.app.main` | Spring Boot 진입점(`CauswApplication`), 모든 도메인/설정/보안 코드 |

- `app-main` 은 `implementation project(':global')` 로 단방향 의존 (`global → app-main` 역방향 금지)
- 두 모듈 모두 Java 25, 공통 Lombok 의존성 적용
- `global` 은 Spring 의존성을 최소화함 — 도메인 로직이나 Spring 코드는 `app-main` 에 추가

## 2. 레이어 표준

서브 도메인(예: `community/post`, `user/account`)마다 다음 레이어를 표준으로 따릅니다.

```
domain/{domain}/{subDomain}/
├── api/
│   └── v2/                # 현재 표준 API (v1 은 모두 제거됨. v2 디렉터리명은 향후 확장 고려)
│       ├── controller/     # @RestController
│       ├── dto/{request,response}/
│       └── mapper/         # *DtoMapper (MapStruct)
├── service/
│   ├── dto/                # 서비스 계층 input/output DTO
│   ├── mapper/              # 필요 시 매퍼
│   └── implementation/      # Reader / Writer 등 도메인 동작 컴포넌트
├── repository/
│   ├── *Repository.java     # JpaRepository 상속
│   └── query/                # QueryDSL 동적 쿼리
├── entity/                  # JPA Entity (BaseEntity 상속)
└── enums/                    # 도메인 상태/타입 enum
```

서브 도메인마다 일부 변형이 있습니다 (예: `implementation` 없이 `service/`에 `*Service.java` 직접 두는 경우 등) — 상세는 [conventions/service-layer.md](./conventions/service-layer.md) §1 참조.

### 레이어별 책임

- **API (Controller)** — HTTP 경로 매핑, 입력 검증(`@Valid`), 인증 정보 추출(`@AuthenticationPrincipal`), DTO ↔ Service 변환 호출, `ApiResponse` 래핑
- **Service** — 트랜잭션 경계, 비즈니스 흐름 조립. 실제 영속화 로직은 `implementation` 의 `*Reader`/`*Writer` 등 컴포넌트가 담당
- **Repository** — Spring Data JPA + QueryDSL. 단순 CRUD 는 `JpaRepository`, 동적 조건은 `repository/query/*QueryRepository`
- **Entity** — `BaseEntity` 를 상속받는 JPA 엔티티. UUID PK + `created_at` / `updated_at` 자동 관리

레이어별 코딩 규약: [conventions/](./conventions/).

## 3. 의존성 흐름

```
HTTP Request
   ↓
Controller (api/v2)
   ↓  Mapper (DTO → ServiceInput)
Service (트랜잭션 경계)
   ↓
Reader / Writer / Validator (implementation)
   ↓
Repository (JpaRepository / QueryRepository)
   ↓
Entity (JPA)
```

원칙:
- Controller → Service → Repository → Entity (단방향)
- 다른 도메인의 데이터가 필요하면 그 도메인의 Reader 를 주입 (Repository 직접 호출 X)
- `shared/` 와 `core/` 는 어디서든 의존 가능. 반대로 `shared/`/`core/` 가 특정 도메인을 의존해서는 안 됨

## 4. 패키지 맵 (`app-main`)

```
net.causw.app.main/
├── CauswApplication   # @SpringBootApplication entrypoint
├── core/              # 플랫폼/인프라 횡단 관심사
├── domain/            # 비즈니스 도메인 (§5)
└── shared/            # 도메인 간 공용 컴포넌트
```

| 패키지 | 역할 |
|--------|------|
| `core/aop` | Spring AOP (`@MeasureTime` 메서드 실행 시간 측정) |
| `core/batch` | Spring Batch 잡 / 리스너 |
| `core/config` | Bean/설정 클래스 (`async`, `batch`, `flyway`, `persistence`, `querydsl`, `scheduling`, `swagger` 등 하위 `*Config`) |
| `core/datasourceProxy` | DataSource 프록시 — 요청별 쿼리 카운트/로깅 |
| `core/filter` | Servlet Filter |
| `core/security` | Spring Security/JWT/OAuth2. 상세: [cross-cutting/security-and-auth.md](./cross-cutting/security-and-auth.md) |
| `shared/dto` | 공용 응답 DTO (`ApiResponse`, `PageResponse`) |
| `shared/entity` | JPA 베이스 클래스 (`BaseEntity`, `AuditableEntity`) |
| `shared/exception` | 예외/핸들러/ErrorCode 표준. 상세: [conventions/exception.md](./conventions/exception.md) |
| `shared/infra` | 외부 인프라 클라이언트 (`redis`, `mail`, `push`, `firebase`, `storage`) |
| `shared/pageable` | 페이징 응답/유틸 |
| `shared/storage` | 파일 스토리지 추상화 — `StorageClient` 인터페이스 + `S3StorageClient`/`LocalStorageClient` |
| `shared/AbstractValidator` | Validator 추상 클래스 (`validate()`) |
| `global/exception` | `app-main` 밖에서도 쓰는 기초 예외 — `core/security` 등 일부 cross-cutting 코드가 직접 사용, `GlobalV2ExceptionHandler` 가 함께 처리 |

## 5. 도메인 (변경 빈도 높음 — 참고용)

현재 최상위 도메인: `admin`, `asset`, `campus`, `community`, `etc`, `integration`, `notification`, `user`. 각 도메인은 1개 이상의 서브 도메인으로 나뉘고, 서브 도메인 단위로 §2 표준 레이어가 적용됩니다.

도메인/서브 도메인은 추가·제거가 잦으므로 정확한 목록은 `app-main/src/main/java/net/causw/app/main/domain/` 을 직접 확인하세요. 표준 레이어를 따르는 구체적인 예시(`community/post`):

| 경로 | 내용 |
|------|------|
| `api/v2/controller/PostController` | `/api/v2/posts` Controller |
| `service/PostService` | 트랜잭션 경계 서비스 |
| `service/implementation/PostReader` / `PostWriter` | 조회/변경 전용 Component |
| `repository/PostRepository`, `repository/query/PostQueryRepository` | JPA / QueryDSL |
| `entity/Post` | JPA Entity (`tb_post`) |

## 6. 외부 의존성 (요약)

`app-main/build.gradle` 기준 주요 라이브러리 — 정확한 버전은 빌드 파일 참조.

| 분류 | 의존성 |
|------|---------|
| Web / ORM | `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, MySQL, QueryDSL |
| Auth | `spring-boot-starter-security`, `spring-boot-starter-oauth2-client`, `jjwt` |
| Mail / Push | `spring-boot-starter-mail`, `firebase-admin` |
| Cache / Storage | `spring-boot-starter-data-redis`, `spring-cloud-starter-aws` |
| Batch / Migration | `spring-boot-starter-batch`, `flyway-core`, `flyway-mysql` |
| API Doc / Mapper | `springdoc-openapi-starter-webmvc-ui`, MapStruct |
| 모니터링 | `spring-boot-starter-actuator`, `micrometer-registry-prometheus` |
| 환경변수 | `spring-dotenv` (`.env` 파일 로딩) |

일부 의존성은 다소 오래된 메이저 버전(`spring-cloud-aws` 2.x, `jjwt` 0.9.x, MapStruct 1.4.x) 이라 신규 API 도입 전 호환성 점검 필요.

## 7. 빌드 / 실행

```bash
./gradlew clean build          # 빌드
./gradlew :app-main:bootRun    # 로컬 실행 (application-local.yml + .env 로딩)
./gradlew test                 # 테스트
```

- 실행 시 `Asia/Seoul` 타임존을 명시적으로 설정 (`CauswApplication#init`)
- `.env` 사용법: [../env_guide.md](../env_guide.md) / Flyway 운영: [../flyway_guide.md](../flyway_guide.md)
- 리소스 디렉터리(`application-*.yml`, `db/migration/`)와 기타 루트 디렉터리 구성은 코드 트리에서 직접 확인 — 문서로 중복 관리하지 않음

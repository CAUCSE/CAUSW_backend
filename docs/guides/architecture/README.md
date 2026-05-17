# 아키텍처 개요

Spring Boot 3.2 / Java 17 기반의 멀티 모듈 Gradle 프로젝트입니다. 루트 README 는 헥사고날(Ports and Adapters) 을 표방하지만 실제 코드 구조는 **도메인 중심 레이어드(Layered by Domain)** 에 가깝습니다.

디렉터리 트리 상세는 [package-structure.md](./package-structure.md).

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

- `app-main` 은 build.gradle 에서 `implementation project(':global')` 로 의존
- **단방향 의존**: `app-main → global` (역방향 금지)
- 두 모듈 모두 Java 17, 공통 Lombok 의존성 적용

## 2. 헥사고날 아키텍처 표방과 실제

루트 `readme.md` 는 헥사고날(Ports and Adapters)을 표방하지만, 코드에서 ports/adapters 인터페이스 분리가 엄격하게 적용되어 있지는 않습니다.

| 헥사고날 개념 | 실제 코드에서의 매핑 |
|---------------|----------------------|
| Adapter (Web) | `domain/{domain}/{sub}/api/v{n}/controller` |
| Adapter (Persistence) | `domain/{domain}/{sub}/repository` (Spring Data JPA 인터페이스를 직접 사용) |
| Application | `domain/{domain}/{sub}/service/v{n}` 의 `*Service` |
| Domain | `domain/{domain}/{sub}/entity`, `enums`, 그리고 `service/implementation`(또는 `service/v2/implementation`)의 도메인 동작 컴포넌트 |
| Port (인터페이스) | 일부 `service/v1` 에 한정적으로 존재. 모든 도메인에 일관 적용된 형태는 아님 |

→ 새 코드를 짤 때는 루트 README 의 헥사고날 설명보다 실제 코드의 레이어 패턴(`api → service → repository → entity`)을 기준으로 삼는 편이 정확합니다.

## 3. 4계층 레이어 (서브 도메인 내부 표준)

각 도메인 하위 서브 도메인은 다음 레이어를 표준으로 따릅니다. 단 도메인마다 변형(특히 service 하위 디렉터리 구성)이 있으므로 작업 전 해당 서브 도메인의 실제 트리를 한 번 확인하는 편이 안전합니다.

```
domain/{domain}/{subDomain}/
├── api/
│   ├── v1/                # 레거시 API
│   └── v2/                # 현재 표준 API
├── service/
│   ├── v1/                # 레거시 서비스
│   ├── v2/                # 현재 표준 서비스
│   ├── dto/               # 서비스 계층 input/output DTO
│   ├── mapper/            # 필요 시 매퍼
│   └── implementation/    # Reader / Writer 등 도메인 동작 컴포넌트
├── repository/
│   ├── *Repository.java   # JpaRepository 상속
│   └── query/             # QueryDSL 동적 쿼리
├── entity/                # JPA Entity (BaseEntity 상속)
└── enums/                 # 도메인 상태/타입 enum
```

### 도메인별 변형 (실제 예)

| 패턴 | 예 |
|------|-----|
| `service/implementation/` 가 service 직속 (v1/v2 와 동등) | `user/account`, `user/auth` |
| `service/v2/implementation/` 으로 v2 안에 위치 | `community/post`, `asset/file`, `asset/locker`, `notification/notification` |
| `service/v2/` 에 `*Service.java` 만 있고 별도 implementation 디렉터리 없음 | `community/report` |
| `service/v1/validators/` 처럼 도메인 특화 패키지가 더 있음 | `asset/locker` |

→ 표준은 위 트리지만, 신규 서브 도메인을 만들 때는 비슷한 책임의 기존 서브 도메인 구조를 참고하는 편이 안전합니다.

### 레이어별 책임

- **API (Controller)** — HTTP 경로 매핑, 입력 검증(`@Valid`), 인증 정보 추출(`@AuthenticationPrincipal`), DTO ↔ Service 변환 호출, `ApiResponse` 래핑
- **Service** — 트랜잭션 경계, 비즈니스 흐름 조립. 실제 영속화 로직은 `implementation` 의 `*Reader`/`*Writer` 등 컴포넌트가 담당
- **Repository** — Spring Data JPA + QueryDSL. 단순 CRUD 는 `JpaRepository`, 동적 조건은 `repository/query/*QueryRepository`
- **Entity** — `BaseEntity` 를 상속받는 JPA 엔티티. UUID PK + `created_at` / `updated_at` 자동 관리

레이어별 코딩 규약: [../conventions/](../conventions/).

## 4. 의존성 흐름

```
HTTP Request
   ↓
Controller (api/v{n})
   ↓  Mapper (DTO → ServiceInput)
Service (트랜잭션 경계)
   ↓
Reader / Writer / Validator (implementation)
   ↓
Repository (JpaRepository / QueryRepository)
   ↓
Entity (JPA)
```

### 의존성 방향 원칙

- Controller → Service → Repository → Entity (단방향)
- 다른 도메인의 데이터가 필요하면 그 도메인의 Reader 를 주입 (Repository 직접 호출 X)
- `shared/` 와 `core/` 는 어디서든 의존 가능. 반대로 `shared/` / `core/` 가 특정 도메인을 의존해서는 안 됩니다.

## 5. `app-main` 최상위 패키지

```
net.causw.app.main/
├── CauswApplication          # @SpringBootApplication
├── core/                     # 인프라/플랫폼 횡단 관심사
│   ├── aop/                  # @MeasureTime 등 AOP
│   ├── batch/                # Spring Batch 잡
│   ├── config/               # JPA/Async/Querydsl/Swagger/Scheduling/Flyway/Batch/Persistence 설정
│   ├── datasourceProxy/      # DataSource 프록시 (쿼리 카운팅/로깅)
│   ├── favicon/              # 정적 리소스 처리
│   ├── filter/               # Servlet Filter
│   ├── global/               # core 내부 공통
│   └── security/             # Spring Security, JWT, OAuth2
├── domain/                   # 8개 비즈니스 도메인
│   ├── user, community, campus, finance, notification, integration, asset, etc
└── shared/                   # 도메인 간 공용 컴포넌트
    ├── dto/                  # ApiResponse, PageResponse
    ├── entity/               # BaseEntity, AuditableEntity
    ├── exception/            # GlobalExceptionHandler, ErrorCode
    ├── infra/                # Redis, Mail, S3 등 인프라 클라이언트
    ├── pageable/             # 페이징 유틸
    ├── seed/                 # 초기 시드 데이터
    ├── storage/              # 파일 스토리지 추상화 (v1/v2)
    ├── util/                 # 공용 유틸
    ├── AbstractValidator     # Validator 추상 클래스
    ├── ValidatorBucket       # Validator 체이닝 컨테이너
    └── StatusPolicy          # 상태 정책
```

자세한 항목 설명은 [package-structure.md](./package-structure.md).

## 6. 외부 의존성 (요약)

`app-main/build.gradle` 기준 주요 라이브러리.

| 분류 | 의존성 |
|------|---------|
| Web | `spring-boot-starter-web` |
| ORM | `spring-boot-starter-data-jpa`, MySQL, QueryDSL (openfeign 포크) |
| Auth | `spring-boot-starter-security`, `spring-boot-starter-oauth2-client`, `jjwt` |
| Validation | `spring-boot-starter-validation` |
| Mail / Push | `spring-boot-starter-mail`, `firebase-admin` |
| Cache / Storage | `spring-boot-starter-data-redis`, `spring-cloud-starter-aws` |
| Batch | `spring-boot-starter-batch` |
| Migration | `flyway-core`, `flyway-mysql` |
| API Doc | `springdoc-openapi-starter-webmvc-ui` |
| Mapper | MapStruct |
| Logging | `logback-discord-appender`, `logback-json-classic` |
| Excel / CSV | Apache POI, Apache Commons CSV |
| 모니터링 | `spring-boot-starter-actuator`, `micrometer-registry-prometheus` |
| 환경변수 | `spring-dotenv` (`.env` 파일 로딩) |

정확한 버전은 빌드 파일을 참조하세요.

일부 의존성은 다소 오래된 메이저 버전(spring-cloud-aws 2.x, jjwt 0.9.x, MapStruct 1.4.x) 이라 신규 API 도입 전 호환성 점검이 필요합니다.

## 7. 빌드 / 실행

```bash
# 빌드
./gradlew clean build

# 로컬 실행 (application-local.yml + .env 로딩)
./gradlew :app-main:bootRun

# 테스트
./gradlew test
```

- 실행 시 `Asia/Seoul` 타임존을 명시적으로 설정 (`CauswApplication#init`)
- 활성 프로파일 로깅: `local` / `dev` / `prod`
- `.env` 사용법: [../../env_guide.md](../../env_guide.md) / Flyway 운영: [../../flyway_guide.md](../../flyway_guide.md)

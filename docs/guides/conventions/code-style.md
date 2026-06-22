# 코드 스타일

자바 코드 포맷, Lombok 사용, 명명 규약, 패키지 구조 가이드입니다.

## 1. 자바 / Spring 버전

- Java 17
- Spring Boot 3.2.x
- jakarta.* 네임스페이스 사용 (Spring Boot 3 이상)

## 2. 코드 포맷터: Naver Eclipse + Spotless

- 설정: `app-main/build.gradle` 의 `spotless { java { ... } }` 블록
- 포맷터 XML: `app-main/naver-eclipse-formatter.xml`

```groovy
spotless {
    java {
        target '**/*.java'
        lineEndings 'UNIX'
        importOrder("\\#", "java", "javax", "org", "net", "com", "")
        removeUnusedImports()
        eclipse().configFile('naver-eclipse-formatter.xml')
    }
}
```

### 적용 / 검증

```bash
# 포맷 자동 적용
./gradlew spotlessApply

# 포맷 위반 검사
./gradlew spotlessCheck
```

### Import 순서

```
1. static import (\\#)
2. java.*
3. javax.*
4. org.*
5. net.*
6. com.*
7. 그 외
```

각 그룹 사이는 빈 줄. 그룹 내부는 알파벳 순서.

### 들여쓰기 / 줄바꿈

- Tab 인덴트 (Naver 설정)
- 줄 끝: LF
- 라인 최대 길이는 Naver 포맷터 설정에 따름

## 3. Lombok

전역 설정: 프로젝트 루트 `lombok.config`

```
config.stopBubbling = true
lombok.addLombokGeneratedAnnotation = true
```

→ Lombok 생성 코드에 `@lombok.Generated` 가 붙어 Jacoco 등에서 자동 제외됩니다.

### 권장 어노테이션 조합

**Entity / JPA**
```java
@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(...)
public class Post extends BaseEntity { ... }
```

**DTO**

신규 불변 DTO 는 `.gemini/styleguide.md` Rule 68 에 따라 `record` 를 기본으로 사용합니다.

```java
public record PostCreateRequest(String content, String boardId) {}
```

기존 Lombok class 형태로 작성할 때는 모순 없이 다음 조합:

```java
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PostCreateRequest { ... }
```

**Service / Component**
```java
@Service           // 또는 @Component, @Repository
@RequiredArgsConstructor
public class PostService { ... }
```

### 지양 / 주의

- 엔티티에 `@Setter` 는 지양 (변경 메서드 명시적 정의)
- `@Data` 는 사용하지 않음 (equals/hashCode 자동 생성이 위험)
- `@AllArgsConstructor` 의 접근 제어자는 가능한 한 `PRIVATE`

## 4. 명명 규약

### 클래스 / 인터페이스

| 종류 | 패턴 | 예시 |
|------|------|------|
| Entity | PascalCase 단수 | `Post`, `User`, `CrawledNotice` |
| Controller | `{Entity}Controller`, `{Entity}AdminController` (관리자) | `PostController`, `ReportAdminController` |
| Service | `{Entity}Service` | `PostService` |
| Reader/Writer | `{Entity}Reader`, `{Entity}Writer` | `PostReader`, `PostWriter` |
| Validator | `{Domain}Validator` 또는 `{Rule}Validator` | `UserValidator`, `AdmissionValidator` |
| Manager / Creator / Linker 등 | 책임을 명확히 드러내는 동사 기반 명사 | `PostImageManager`, `UserInfoCreator`, `SocialAccountLinker` |
| Repository | `{Entity}Repository`, `{Entity}QueryRepository` | `PostRepository`, `PostQueryRepository` |
| Mapper | `{Entity}DtoMapper` | `PostDtoMapper` |
| ErrorCode | `{Domain}ErrorCode` | `PostErrorCode` |
| DTO (Request) | `{Action}{Entity}Request` | `PostCreateRequest` |
| DTO (Response) | `{Entity}Response`, `{Action}{Entity}Response` | `PostResponse`, `PostCreateResponse` |
| Service DTO | `*Command`, `*Query`, `*Result` | `PostCreateCommand`, `PostListQuery`, `PostDetailResult` |
| Event | `{Domain}{Action}Event` | `UserSignedUpEvent` |

### 변수 / 메서드 / 필드

- camelCase
- 조회 메서드: `findBy*` (Optional/Collection), `getBy*` (없으면 예외)
- 변경 메서드: `create*`, `update*`, `delete*`(soft delete), `restore*`
- 검증 메서드: `validate*`
- Boolean 필드 / 메서드: `is*`, `has*`, `can*`

### 패키지

- 모두 소문자 lowercase (camelCase 금지 — 자바 패키지 컨벤션)
- 단어 분리는 점(.) 으로 (`net.causw.app.main.domain.user.account`)
- 단수형 (`post`, `user`, `comment` — 복수형 X)

## 5. 어노테이션 정렬

- 클래스 위: 한 줄에 하나씩 세로로
- 메서드 / 필드: 짧은 어노테이션은 한 줄에 두 개까지 허용
- `@Override`, `@Deprecated` 는 최상단

## 6. 패키지 구조 원칙

디렉터리 전체 트리: [../architecture.md](../architecture.md).

1. **도메인 우선** — 기능을 먼저 도메인에 매핑한 후 그 안에서 레이어로 분리
2. **서브 도메인 단위 레이어 적용** — `api / service / repository / entity / enums`
3. **API 버전 관리는 디렉터리로** — `api/v2` (v1 은 모두 제거되어 현재는 단일 버전). 서비스 레이어는 버전 디렉터리 없이 `service/` 바로 아래
4. **공용은 `shared/`** — 두 개 이상의 도메인이 사용하는 경우만
5. **인프라 / 횡단 관심사는 `core/`** — 보안, 배치, AOP, 설정 등

## 7. 주석 / 문서화

- 공개 API (Controller) 는 Swagger `@Operation` / `@Schema` 로 문서화
- 코드 주석은 **WHY 만**. WHAT 은 코드 자체로 표현 (좋은 메서드명, 좋은 변수명)
- TODO 주석은 가능한 한 이슈로 옮기고, 부득이하면 `// TODO(이름): 사유 (이슈 번호)`
- Javadoc 은 라이브러리화된 공용 유틸 / 복잡한 도메인 규칙에 한정

## 8. 로깅

- SLF4J + Logback (`logback-spring.xml` + `logback-json-classic`)
- Lombok 의 `@Slf4j` 활용
- 레벨 정책:
  - `DEBUG` — 메서드 실행 시간, 상세 흐름
  - `INFO` — 정상 흐름 중 의미 있는 이벤트
  - `WARN` — 4xx 클라이언트 에러 / 외부 의존성 일시 실패
  - `ERROR` — 5xx / 복구 불가 상황 + 스택트레이스

관측성 상세: [../cross-cutting/observability.md](../cross-cutting/observability.md).

## 9. 의존성 주입

- 생성자 주입만 (`@RequiredArgsConstructor` + `private final`)
- `@Autowired` 필드/세터 주입 지양
- 순환 의존이 발생하면 도메인 분리를 다시 검토 (필요 시 이벤트 활용)

## 10. 테스트

- JUnit 5 + Spring Boot Test (Gradle 의 `useJUnitPlatform()`)
- 보안 테스트: `@WithMockCustomUser` 커스텀 어노테이션
- 통합 테스트 시 H2 인메모리 (`runtimeOnly 'com.h2database:h2'`)
- 테스트 클래스 명명 (`.gemini/styleguide.md` Rule 149~150):
  - 단위 테스트: `{ClassName}Test`
  - 통합 테스트: `{ClassName}IntegrationTest`
- 테스트 메서드명: `given_when_then` 형태 (Given-When-Then 패턴)
- **`@DisplayName` 은 반드시 한글로 작성** (`.gemini/styleguide.md` Rule 153)
- assertion 은 AssertJ (`assertThat(...).isEqualTo(...)`) 를 기본으로 사용 (JUnit `assertEquals` / `assertTrue` 지양)
- 테스트 데이터는 Fixture 클래스로 재사용

## 11. Git / 커밋

루트 [CONTRIBUTING.md](../../../CONTRIBUTING.md) 참조.

```
(타입): (제목)
```

| 타입 | 의미 |
|------|------|
| feat | 신규 기능 |
| fix | 버그 수정 |
| refactor | 동작 변경 없는 구조 개선 |
| docs | 문서 |
| test | 테스트 |
| chore | 환경 변수, 빌드 등 |
| infra | 인프라 |
| style | 코드 포맷 |

- 브랜치 명명: `(타입)/(개발 내용)` 권장
- 머지 전략: Squash merge (브랜치 정리 필요)

### `db-change` 라벨 (필수)

`.gemini/styleguide.md` Rule 178~180 에 따라, 아래 변경 중 하나라도 포함된 PR 은 **반드시 `db-change` 라벨** 을 추가해야 합니다. Flyway 마이그레이션 CI 가 라벨을 기준으로 실행됩니다.

- Flyway 마이그레이션 스크립트 추가/수정 (`app-main/src/main/resources/db/migration/**`)
- JPA Entity 클래스의 테이블/컬럼 구조 변경 (`@Entity`, `@Table`, `@Column` 등)
- `ddl-auto` 또는 DB 스키마에 영향을 주는 설정 변경

라벨이 누락된 채로 머지하면 마이그레이션이 실행되지 않아 운영 장애로 이어질 수 있으니, PR 생성 시점에 본인 / 리뷰어가 함께 확인합니다.

## 12. 사전 점검 명령어

```bash
# 포맷
./gradlew spotlessApply

# 빌드 (테스트 포함)
./gradlew clean build

# 테스트만
./gradlew test

# Flyway 마이그레이션 검증
./gradlew flywayInfo
./gradlew flywayValidate
```

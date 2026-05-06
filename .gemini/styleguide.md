# styleguide.md

## 0. 적용 원칙
- 이 문서는 Java, Spring Boot, JPA, QueryDSL 코드에 반드시 적용한다.
- AI 자동완성, 코드 생성, PR 리뷰는 본 문서를 최우선 기준으로 사용한다.
- 규칙 충돌 시 더 엄격한 규칙을 따른다.
- 불명확한 항목은 임의로 추정하지 말고 팀 합의 문서를 먼저 확인한다.

## 1. 아키텍처 계층 규칙
- Layered Architecture를 따른다: `Controller -> Service -> Implementation -> Repository`
- `Controller`는 요청/응답 변환과 라우팅만 담당한다.
- 비즈니스 로직은 반드시 `Service` 계층에 둔다.
- `Service`는 유즈케이스 단위 트랜잭션 경계를 관리한다.
- `Implementation`은 단일 책임 로직을 수행하고 Repository/Shared에 의존한다.
- 같은 도메인 내부에서는 `Implementation -> Implementation` 호출을 허용한다.
- `Implementation -> Implementation` 호출은 단일 책임 조합 목적으로만 사용하고, 순환 의존을 절대 만들지 않는다.
- `Controller -> Repository` 직접 호출을 절대 금지한다.
- 하위 계층이 상위 계층을 참조하는 레이어 역전을 절대 금지한다.
- 인접하지 않은 하위 계층 직접 참조를 금지한다.

### Anti-pattern vs Preferred
- Never Do:
```java
@RestController
public class UserController {
    @PostMapping("/users")
    public User create(@RequestBody UserRequest request) {
        return userRepository.save(User.from(request));
    }
}
```

- Do:
```java
@RestController
public class UserController {
    @PostMapping("/users")
    public UserResponse create(@RequestBody @Valid UserRequest request) {
        return userService.createUser(request);
    }
}
```

## 2. 패키지 의존성 규칙
- 최상위 의존성 방향을 유지한다: `core <- shared <- domain`
- `core`는 어떤 내부 패키지도 의존하지 않는다.
- `shared`는 `core`만 의존하고 `domain` 의존을 금지한다.
- `domain`은 `core`, `shared`를 의존할 수 있다.
- 도메인 간 직접 의존은 최소화하고 필요한 경우 `service -> implementation` 경유만 허용한다.
- 패키지/모듈 간 순환 의존을 절대 금지한다.

## 3. Service/Implementation 분리 규칙
- `Service`는 오케스트레이션(검증, 흐름 제어, 조합)에 집중한다.
- `Implementation`은 세분화된 단일 책임 로직만 가진다.
- `Service`는 Repository를 직접 의존하지 않고, 반드시 Implementation 계층을 통해서만 접근한다.
- Implementation 클래스는 의도가 드러나는 접미사를 사용한다.
    - 예시 1(조회): `{Domain}Reader` — `@Component + @Transactional(readOnly = true)`, `orElseThrow(ErrorCode::toBaseException)`
    - 예시 2(쓰기/수정/삭제): `{Domain}Writer` — `@Component + @Transactional`
    - 예시 3(비즈니스 규칙 검증): `{Domain}Validator` — `@Component + @Transactional(readOnly = true)`
- 하나의 Service가 다중 책임을 가지면 Service 또는 Implementation으로 분리한다.

## 4. 트랜잭션/검증/DTO 규칙
- `@Transactional`은 Service 메서드 단위 선언을 기본으로 한다.
- 조회 전용은 `@Transactional(readOnly = true)`를 사용한다.
- Controller나 Repository에 트랜잭션 경계를 두지 않는다.
- 단순 형식 검증은 DTO에서 처리한다.
- 도메인/비즈니스 검증은 Service에서 처리한다.
- 불변 DTO는 `record`를 기본으로 사용한다.
- API 계층 DTO와 Service 계층 DTO를 반드시 분리한다.
- API 요청/응답 DTO를 Service 계층으로 직접 전달하지 않는다.
- Service 계층은 별도 Service DTO 또는 Command/Result 모델을 사용한다.
- 엔티티를 API 응답으로 직접 노출하지 않는다.

## 5. DB/JPA/QueryDSL 규칙
- 동적 쿼리는 반드시 QueryDSL로 작성한다.
- 문자열 결합으로 JPQL/SQL을 만드는 코드를 절대 작성하지 않는다.
- N+1 가능성이 있으면 Fetch Join 또는 동등한 최적화를 반드시 적용한다.
- 조회 API는 페이징을 기본 적용한다.
- 조회 전용 쿼리는 Reader/Query 전용 컴포넌트로 분리한다.

### Anti-pattern vs Preferred
- Never Do:
```java
String jpql = "select u from User u where 1=1";
if (name != null) {
    jpql += " and u.name = '" + name + "'";
}
return em.createQuery(jpql, User.class).getResultList();
```

- Do:
```java
QUser user = QUser.user;
BooleanBuilder predicate = new BooleanBuilder();
if (name != null) {
    predicate.and(user.name.eq(name));
}
return queryFactory.selectFrom(user).where(predicate).fetch();
```

## 6. Entity 컨벤션
- 테이블명은 반드시 `tb_` 접두사를 사용한다. 예: `tb_user`, `tb_post`
- 소프트 삭제는 `deletedAt` 필드를 사용한다.
- 정적 팩토리 메서드 `of(...)`를 사용하고, `@Builder`는 `@Builder(access = AccessLevel.PROTECTED)`로 제한한다.
- Entity를 API 응답으로 직접 노출하지 않는다.

### 소프트 삭제 조회 규칙
- Reader/Repository 조회 메서드는 기본적으로 삭제되지 않은 데이터만 반환한다 (`isDeleted = false`, 혹은 `deletedAt = null` 조건 기본 적용).
- 삭제된 데이터를 포함해야 하는 경우, 메서드명에 `IncludeDeleted` 접미사를 붙인다.
  - 예시: `findById(id)` → 삭제되지 않은 것만 조회
  - 예시: `findByIdIncludeDeleted(id)` → 삭제 여부 무관하게 조회
- 삭제 여부 조건 없이 전체를 조회하는 메서드는 반드시 `IncludeDeleted` 접미사로 명시해 의도를 드러낸다.

## 7. 예외/로깅 규칙
- `Exception`, `RuntimeException`을 직접 던지지 않는다.
- 예외는 `XxxErrorCode implements BaseResponseCode` 형태의 Custom ErrorCode를 사용한다.
- 예외 발생은 `ErrorCode::toBaseException` 메서드 레퍼런스로 처리한다. 예: `PostErrorCode.NOT_FOUND::toBaseException`
- 예외는 숨기거나 무시하지 않는다.
- 로깅은 반드시 SLF4J(`log.info`, `log.error`, etc)를 사용한다.
- `System.out.println`은 절대 사용하지 않는다.
- 에러 로그는 반드시 예외 객체와 핵심 식별자(id, key)를 포함한다.
- 민감정보(토큰, 비밀번호, 개인정보 원문) 로깅을 금지한다.

### Anti-pattern vs Preferred
- Never Do:
```java
System.out.println("failed: " + userId);
throw new RuntimeException("error");
```

- Do:
```java
log.error("User fetch failed. userId={}", userId, ex);
throw new UserNotFoundException(userId);
```


## 8. 네이밍 규칙
- 패키지명은 소문자만 사용한다.
- 클래스/인터페이스명은 PascalCase를 사용한다.
- 메서드/변수/파라미터는 lowerCamelCase를 사용한다.
- 상수는 UPPER_SNAKE_CASE를 사용한다.
- 메서드는 동사로 시작한다.
- 정적 팩토리 메서드는 단일 인자 `from`, 다중 인자 `of`를 우선 사용한다.
- 설정 파일은 kebab-case를 사용한다. 예: `application-prod.yml`

## 9. 테스트 규칙
- 테스트 클래스명은 목적에 맞게 작성한다.
    - 단위 테스트: `{ClassName}Test`
    - 통합 테스트: `{ClassName}IntegrationTest`
- 테스트 메서드는 Given-When-Then 패턴을 사용한다.
- 테스트 메서드명은 `given_when_then` 형태를 따른다.
- `@DisplayName`은 한글로 반드시 작성한다.
- assertion은 AssertJ를 기본으로 사용한다.
- 테스트 코드는 `given -> when -> then` 구조를 유지한다.
- 테스트 데이터는 Fixture 클래스로 재사용한다.

### Anti-pattern vs Preferred
- Never Do:
```java
assertEquals("홍길동", user.getName());
assertTrue(users.size() == 3);
```

- Do:
```java
assertThat(user.getName()).isEqualTo("홍길동");
assertThat(users).hasSize(3);
```

## 10. 포맷팅/자동화 규칙
- Naver Java Coding Convention을 따른다.
- Spotless 검사 통과를 머지 전 필수 조건으로 둔다.
- 커밋 전 `./gradlew spotlessCheck`를 실행한다.
- 포맷 수정은 기능 변경과 분리한 커밋으로 관리한다.


## 11. `db-change` 라벨
- PR에 다음 중 하나라도 해당하는 변경이 포함된 경우, `db-change` 라벨이 필요하다.
- 라벨이 누락되어 있으면 리뷰 코멘트로 반드시 안내한다.

### 라벨이 필요한 변경 사항
- Flyway 마이그레이션 스크립트 추가/수정 (`**/db/migration/**`)
- JPA Entity 클래스의 테이블/컬럼 구조 변경 (`@Entity`, `@Table`, `@Column` 등)
- `ddl-auto` 또는 DB 스키마에 영향을 주는 설정 변경

### 리뷰 시 안내 예시
> 이 PR에는 DB 스키마 변경이 포함되어 있습니다. Flyway 마이그레이션 CI가 실행되려면 `db-change` 라벨을 추가해주세요.

## 12. PR 리뷰 체크리스트 (AI 필수)
- Controller에 비즈니스 로직이 없는가?
- Layer 의존성 위반(`controller -> repository`, 역의존, 순환의존)이 없는가?
- `Implementation -> Implementation` 호출이 필요한 범위로만 사용되고 순환 의존이 없는가?
- 동적 쿼리가 QueryDSL로 작성되었는가?
- N+1 가능 지점에 Fetch Join/최적화가 반영되었는가?
- Service에 트랜잭션 경계가 선언되었는가?
- API DTO와 Service DTO가 분리되어 있는가?
- Custom Exception을 사용했는가?
- `System.out.println`이 제거되었는가?
- SLF4J 로그가 적절한 레벨과 컨텍스트를 포함하는가?
- 테스트 명명/구조/GWT 패턴을 따르는가?
- DB 스키마 영향 변경에 `db-change` 라벨이 추가되었는가?

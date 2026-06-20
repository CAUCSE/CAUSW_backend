# 영속화 컨벤션

JPA Entity, Repository, QueryDSL, Flyway 마이그레이션 작성 규칙입니다.

## 1. Entity 베이스 클래스

모든 JPA 엔티티는 `shared/entity/BaseEntity` 를 상속받습니다.

```java
@Getter
@NoArgsConstructor
@MappedSuperclass
@EntityListeners(value = {AuditingEntityListener.class})
public class BaseEntity extends AuditableEntity {
    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false, unique = true)
    private String id;
}
```

- **PK**: `String id` + Hibernate `@UuidGenerator` (UUID 자동 생성)
- **상속**: `AuditableEntity` 에서 `created_at`, `updated_at` 자동 관리 (`@CreatedDate`, `@LastModifiedDate`)
- **JPA Auditing**: `@EntityListeners(AuditingEntityListener.class)` 적용
- 활성화 설정: `core/config/persistence/JpaConfig` 의 `@EnableJpaAuditing`

## 2. Entity 작성 규칙

대표 패턴 (예: `community/post/entity/Post`):

```java
@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_post", indexes = {
    @Index(name = "board_id_index", columnList = "board_id"),
    @Index(name = "user_id_index", columnList = "user_id"),
    @Index(name = "post_cursor_index", columnList = "created_at, id")
})
public class Post extends BaseEntity {

    @Lob
    @Column(columnDefinition = "TEXT", name = "content", nullable = false)
    private String content;

    @ManyToOne(targetEntity = User.class)
    @JoinColumn(name = "user_id", nullable = false)
    private User writer;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public static Post of(String content, User writer, Board board) {
        return Post.builder()
            .content(content)
            .writer(writer)
            .board(board)
            .build();
    }
}
```

### 정적 팩토리 메서드 (필수, 신규 코드)

`.gemini/styleguide.md` Rule 104 — **"정적 팩토리 메서드 `of(...)` 를 사용한다"** 를 따릅니다. 신규 엔티티는 외부에서 `@Builder` 를 직접 호출하지 않고, 도메인 의미가 드러나는 `of(...)` / `from(...)` 정적 메서드를 통해 생성합니다.

- 인자가 단일이면 `from(...)`, 다수면 `of(...)` 우선 사용 (Rule 144)
- `@Builder(access = AccessLevel.PROTECTED)` 로 Builder 노출 범위를 제한해 외부 직접 호출 차단
- 이미 적용된 위치 예: `community/report` 의 일부 엔티티 및 service Command/Result, `community/post/service/util/PostCursorManager`. 현재 다수의 레거시 엔티티는 외부에서 직접 `@Builder` 호출하는 패턴이지만, 신규 코드는 반드시 정적 팩토리 방식으로 작성합니다.

### 명명 규약

| 대상 | 규칙 |
|------|------|
| 클래스명 | PascalCase 단수형 (`Post`, `User`, `CrawledNotice`) |
| 테이블명 | `tb_{snake_case_단수}` (`tb_post`, `tb_crawled_notice`) |
| 컬럼명 | `snake_case` (`deleted_at`, `created_at`) |
| 인덱스명 | `{columnList}_index` |

### Lombok 어노테이션 조합

- `@Getter` — 모든 필드에 getter
- `@Builder(access = AccessLevel.PROTECTED)` — Builder 노출 범위 제한
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)` — JPA 요구
- `@AllArgsConstructor(access = AccessLevel.PRIVATE)` — 외부에서 직접 생성 차단
- **`@Setter` 는 지양**. 변경 메서드(`updateContent(...)`) 를 명시적으로 정의. (단, 일부 레거시 엔티티는 `@Setter` 사용 중)

### JPA 매핑

- `@Entity`, `@Table(name = "...", indexes = {...})` 명시
- 인덱스는 엔티티 어노테이션으로 선언
- 컬럼은 모두 `@Column(name = "...", nullable = ..., unique = ...)` 명시
- 외래 키 관계: `@ManyToOne` + `@JoinColumn` (단방향 권장). `@OneToMany` 는 컬렉션 캐시 / N+1 위험 인지하고 사용
- `FetchType` 기본은 LAZY. EAGER 는 명시적인 사유가 있을 때만
- 큰 텍스트는 `@Lob` + `@Column(columnDefinition = "TEXT")`
- 기본값은 `@ColumnDefault("...")` + 자바 필드 초기값 양쪽에 명시

## 3. 소프트 삭제

### 신규 엔티티: `deletedAt` (필수)

`.gemini/styleguide.md` Rule 103 — **"소프트 삭제는 `deletedAt` 필드를 사용한다"** 를 따릅니다. 신규 엔티티는 `LocalDateTime deletedAt` 컬럼을 사용합니다.

```java
@Column(name = "deleted_at")
private LocalDateTime deletedAt;

public void softDelete() {
    this.deletedAt = LocalDateTime.now();
}

public boolean isDeleted() {
    return this.deletedAt != null;
}
```

조회 / 삭제 규칙 (`.gemini/styleguide.md` Rule 107~112):
- Reader/Repository 조회 메서드는 기본적으로 `deletedAt IS NULL` 조건 적용
- 삭제된 데이터까지 포함해야 하는 메서드는 `IncludeDeleted` 접미사 (예: `findByIdIncludeDeleted(id)`)
- QueryDSL: `.where(entity.deletedAt.isNull())`

### 현재 코드 다수: `isDeleted` (Boolean) — 레거시

현재 코드베이스의 대다수 엔티티 (`Post`, `Comment`, `ChildComment`, `Board`, `Form` 등) 는 아직 `Boolean isDeleted` 필드를 사용합니다. `User` 만 `deletedAt` 으로 이전된 상태입니다.

```java
@Column(name = "is_deleted")
@Builder.Default
@ColumnDefault("false")
private Boolean isDeleted = false;
```

레거시 패턴을 마주칠 때:
- Repository / QueryRepository 에서 `isDeleted = false` 조건을 추가 (메서드 명명 예: `findByIdAndIsDeletedFalse`)
- Writer 의 `softDelete(...)` 에서 `post.setIsDeleted(true)` 호출
- 신규 메서드를 추가할 때는 동일 엔티티의 기존 컨벤션을 따르되, **새 엔티티는 styleguide 의 `deletedAt` 방식으로 작성**합니다.

### cascade 주의

`cascade = CascadeType.REMOVE` 가 걸린 자식 엔티티가 있을 경우 soft delete 만으로는 cascade 되지 않으므로 자식도 별도 처리가 필요합니다.

## 4. Repository

### 기본 패턴

위치: `domain/{domain}/{sub}/repository/`

```java
public interface PostRepository extends JpaRepository<Post, String> {

    Optional<Post> findByIdAndIsDeletedFalse(String id);

    @Modifying
    @Query("update Post p set p.isDeleted = true where p.board.id = :boardId")
    void softDeleteAllByBoardId(@Param("boardId") String boardId);
}
```

규칙:
- `JpaRepository<Entity, String>` 상속 (PK 가 String 인 점에 유의)
- 단순 조회는 메서드 이름 기반 (`findByIdAndIsDeletedFalse`)
- 변경 쿼리는 `@Modifying` + JPQL `@Query`
- 동적 조건이 필요하면 QueryDSL 로 분리 (§5)

### Native Projection (선택)

위치: `repository/projection/`

복잡한 조회 결과를 매핑할 때 인터페이스 기반 projection 또는 native query projection 클래스를 사용합니다 (예: `community/report` 의 `ReportedPostNativeProjection`, `ReportedCommentNativeProjection`).

## 5. QueryDSL Custom

위치: `domain/{domain}/{sub}/repository/query/{Entity}QueryRepository`

```java
@Repository
@RequiredArgsConstructor
public class PostQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Page<Post> findByCondition(PostListCondition cond, Pageable pageable) {
        BooleanBuilder where = new BooleanBuilder();
        where.and(post.isDeleted.isFalse());
        if (cond.boardId() != null) where.and(post.board.id.eq(cond.boardId()));
        if (cond.keyword() != null) where.and(post.content.contains(cond.keyword()));

        List<Post> content = queryFactory.selectFrom(post)
            .where(where)
            .orderBy(post.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        long total = queryFactory.select(post.count()).from(post).where(where).fetchOne();
        return new PageImpl<>(content, pageable, total);
    }
}
```

규칙:
- `@Repository` + `@RequiredArgsConstructor` + `JPAQueryFactory` 주입
- 동적 조건은 `BooleanBuilder` 또는 `BooleanExpression`
- 페이징은 `Pageable` 받아서 `offset`/`limit` 적용 후 `PageImpl` 반환
- 정적 import: `static net.causw.app.main.domain.community.post.entity.QPost.post;`

### QueryDSL 설정

- 라이브러리: openfeign 포크 (`io.github.openfeign.querydsl:querydsl-jpa`)
- Q클래스 생성 경로: `build/generated/querydsl` (`app-main/build.gradle` 의 generated source 설정)
- Bean 등록: `core/config/querydsl/QuerydslConfig` 의 `JPAQueryFactory` Bean

## 6. Flyway 마이그레이션

위치: `app-main/src/main/resources/db/migration/V*.sql`

```bash
# 마이그레이션 파일 생성 (CamelCase desc 필수)
./gradlew flywayCreate -Pdesc=AddCrawledNoticeUpdateFields

# 결과: db/migration/V20260517123456__AddCrawledNoticeUpdateFields.sql
```

규칙:
- 파일 명명: `V{yyyyMMddHHmmss}__{CamelCaseDescription}.sql`
- 환경별 정책:
  - Local: 부팅 시 자동 적용 (`spring.flyway.out-of-order: true`)
  - Dev / Prod: 부팅 자동 비활성. GitHub Actions CD 파이프라인에서 `flywayMigrate` 실행
- 롤백 SQL 은 별도 forward 마이그레이션으로 작성 (Flyway 유료 라이선스 미사용)

운영 상세: [../../flyway_guide.md](../../flyway_guide.md).

### Entity ↔ 스키마 검증

`app-main/gradle/test/schema-validation.gradle` 의 `SchemaValidationTest` 가 Flyway 적용 후 JPA 메타데이터와 실제 스키마를 비교합니다.
- 평소엔 `test { exclude '**/SchemaValidationTest.class' }` 로 제외되어 있음
- 스키마 변경 시 별도로 실행해서 일관성 확인

## 7. ID 전략과 연관관계 주의

- 모든 엔티티 PK 는 String UUID. 연관관계 FK 컬럼은 `{관계명}_id` (예: `board_id`, `user_id`)
- 단방향 연관관계 권장 (`@ManyToOne`). 양방향이 필요하면 `mappedBy` 명시 + N+1 / 직렬화 무한참조 주의
- 자식 컬렉션은 `@OneToMany` + `cascade` / `orphanRemoval` 정책을 명시. soft delete 흐름과 충돌하지 않도록 점검

## 8. 체크리스트 (신규 엔티티 추가 시)

- [ ] `BaseEntity` 상속, `@Entity` + `@Table(name = "tb_...")`
- [ ] 모든 `@Column` 에 name / nullable / unique 명시
- [ ] 적절한 인덱스 정의
- [ ] 변경 메서드는 명시적 메서드로 (가능하면 `@Setter` 미사용)
- [ ] 소프트 삭제 컬럼 필요 여부 검토 (신규 엔티티는 `deletedAt`, 기존 엔티티 수정은 그 엔티티의 기존 패턴 유지)
- [ ] 정적 팩토리 메서드 `of(...)` / `from(...)` 정의 (Builder 외부 노출 금지)
- [ ] Flyway 마이그레이션 작성 (`./gradlew flywayCreate -Pdesc=...`)
- [ ] Repository / 필요 시 QueryRepository 작성
- [ ] Reader / Writer 컴포넌트 추가 (`implementation/`)

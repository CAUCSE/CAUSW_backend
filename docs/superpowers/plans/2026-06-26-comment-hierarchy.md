# Comment Hierarchy Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the separate `ChildComment` model with a one-depth self-referencing `Comment` model while preserving current API behavior.

**Architecture:** `Comment` becomes the only persisted comment entity. Root comments have `parentComment = null`; replies point at their root parent and are blocked from having children by service validation. Compatibility controllers and DTO names remain initially, but all active logic reads and writes `tb_comment`.

**Tech Stack:** Java 17, Spring Boot 3.2, Spring Data JPA, QueryDSL, Flyway, Gradle, JUnit 5, Mockito, AssertJ.

---

## File Structure

- Modify: `app-main/src/main/java/net/causw/app/main/domain/community/comment/entity/Comment.java`
- Delete later in the task series: `app-main/src/main/java/net/causw/app/main/domain/community/comment/entity/ChildComment.java`
- Modify: `app-main/src/main/java/net/causw/app/main/domain/community/comment/entity/LikeComment.java`
- Delete later in the task series: `app-main/src/main/java/net/causw/app/main/domain/community/comment/entity/LikeChildComment.java`
- Modify: `app-main/src/main/java/net/causw/app/main/domain/community/comment/repository/CommentRepository.java`
- Delete later in the task series: `app-main/src/main/java/net/causw/app/main/domain/community/comment/repository/ChildCommentRepository.java`
- Modify: `app-main/src/main/java/net/causw/app/main/domain/community/comment/repository/LikeCommentRepository.java`
- Delete later in the task series: `app-main/src/main/java/net/causw/app/main/domain/community/comment/repository/LikeChildCommentRepository.java`
- Modify: `app-main/src/main/java/net/causw/app/main/domain/community/comment/repository/LikeCommentQueryRepository.java`
- Modify: `app-main/src/main/java/net/causw/app/main/domain/community/comment/service/CommentService.java`
- Convert or delete: `app-main/src/main/java/net/causw/app/main/domain/community/comment/service/ChildCommentService.java`
- Modify: `app-main/src/main/java/net/causw/app/main/domain/community/comment/service/implementation/CommentReader.java`
- Modify: `app-main/src/main/java/net/causw/app/main/domain/community/comment/service/implementation/CommentWriter.java`
- Modify: `app-main/src/main/java/net/causw/app/main/domain/community/comment/service/implementation/CommentMetaReader.java`
- Modify: `app-main/src/main/java/net/causw/app/main/domain/community/comment/service/implementation/CommentMapper.java`
- Convert or delete child-specific implementation classes under `app-main/src/main/java/net/causw/app/main/domain/community/comment/service/implementation/`
- Modify: `app-main/src/main/java/net/causw/app/main/domain/community/comment/util/CommentValidator.java`
- Convert or delete: `app-main/src/main/java/net/causw/app/main/domain/community/comment/util/ChildCommentValidator.java`
- Modify: comment API DTOs and mappers under `app-main/src/main/java/net/causw/app/main/domain/community/comment/api/v2/`
- Modify: report, block, notification, and post count integrations that import `ChildComment`
- Add: `app-main/src/main/resources/db/migration/V*_UnifyCommentHierarchy.sql`
- Modify tests under `app-main/src/test/java/net/causw/app/main/domain/community/comment/`
- Modify affected report, block, notification, and post tests.

---

### Task 1: Schema Migration

**Files:**
- Create: `app-main/src/main/resources/db/migration/V*_UnifyCommentHierarchy.sql`

- [ ] **Step 1: Create the Flyway migration file**

Run:

```bash
./gradlew flywayCreate -Pdesc=UnifyCommentHierarchy
```

Expected: one new SQL file under `app-main/src/main/resources/db/migration`.

- [ ] **Step 2: Add migration SQL**

Add SQL that:

```sql
ALTER TABLE tb_comment
    ADD COLUMN parent_comment_id varchar(255) NULL;

INSERT INTO tb_comment (
    id,
    created_at,
    updated_at,
    content,
    is_anonymous,
    is_deleted,
    post_id,
    user_id,
    parent_comment_id
)
SELECT
    cc.id,
    cc.created_at,
    cc.updated_at,
    cc.content,
    cc.is_anonymous,
    cc.is_deleted,
    pc.post_id,
    cc.user_id,
    cc.parent_comment_id
FROM tb_child_comment cc
JOIN tb_comment pc ON pc.id = cc.parent_comment_id
WHERE NOT EXISTS (
    SELECT 1
    FROM tb_comment existing
    WHERE existing.id = cc.id
);

INSERT INTO tb_like_comment (
    id,
    created_at,
    updated_at,
    comment_id,
    user_id
)
SELECT
    lcc.id,
    lcc.created_at,
    lcc.updated_at,
    lcc.child_comment_id,
    lcc.user_id
FROM tb_like_child_comment lcc
WHERE NOT EXISTS (
    SELECT 1
    FROM tb_like_comment existing
    WHERE existing.id = lcc.id
);

ALTER TABLE tb_comment
    ADD CONSTRAINT fk_comment_parent_comment
        FOREIGN KEY (parent_comment_id) REFERENCES tb_comment (id);

CREATE INDEX idx_comment_post_parent_created_at
    ON tb_comment (post_id, parent_comment_id, created_at);

CREATE INDEX idx_comment_parent_created_at
    ON tb_comment (parent_comment_id, created_at);
```

- [ ] **Step 3: Commit**

```bash
git add app-main/src/main/resources/db/migration
git commit -m "feat: 댓글 계층형 구조 마이그레이션 추가"
```

---

### Task 2: Comment Entity Self Reference

**Files:**
- Modify: `app-main/src/main/java/net/causw/app/main/domain/community/comment/entity/Comment.java`
- Test: `app-main/src/test/java/net/causw/app/main/domain/community/comment/entity/CommentTest.java`

- [ ] **Step 1: Write failing entity tests**

Create tests proving:

- `Comment.ofRoot(...)` creates a root comment with no parent.
- `Comment.ofReply(...)` creates a reply with the same post as its parent.
- `isReply()` returns true only for replies.

- [ ] **Step 2: Run test and verify RED**

```bash
./gradlew :app-main:test --tests "net.causw.app.main.domain.community.comment.entity.CommentTest"
```

Expected: fails because the new factory methods and `isReply()` do not exist.

- [ ] **Step 3: Implement entity changes**

Add self-reference fields, root/reply factories, `isReply()`, and change `childCommentList` to `List<Comment>`.

- [ ] **Step 4: Run test and verify GREEN**

```bash
./gradlew :app-main:test --tests "net.causw.app.main.domain.community.comment.entity.CommentTest"
```

Expected: pass.

- [ ] **Step 5: Commit**

```bash
git add app-main/src/main/java/net/causw/app/main/domain/community/comment/entity/Comment.java app-main/src/test/java/net/causw/app/main/domain/community/comment/entity/CommentTest.java
git commit -m "feat: 댓글 엔티티 자기참조 구조 추가"
```

---

### Task 3: Unified Comment Repositories

**Files:**
- Modify: `app-main/src/main/java/net/causw/app/main/domain/community/comment/repository/CommentRepository.java`
- Modify: `app-main/src/main/java/net/causw/app/main/domain/community/comment/service/implementation/CommentReader.java`
- Test: `app-main/src/test/java/net/causw/app/main/domain/community/comment/service/implementation/CommentReaderTest.java`

- [ ] **Step 1: Write failing reader tests**

Add tests for:

- `getComments(postId, pageable)` loads only root comments.
- Replies are attached by parent ID.
- Deleted comments are still loadable for rendering when using the existing list behavior.

- [ ] **Step 2: Run test and verify RED**

```bash
./gradlew :app-main:test --tests "net.causw.app.main.domain.community.comment.service.implementation.CommentReaderTest"
```

Expected: fails because repository methods still query `ChildComment`.

- [ ] **Step 3: Replace repository methods**

Use `Comment` queries:

- root list: `c.post.id = :postId AND c.parentComment IS NULL`
- reply batch: `c.parentComment.id IN :parentCommentIds`
- not-deleted lookup: `findByIdAndIsDeletedFalse`

- [ ] **Step 4: Run test and verify GREEN**

```bash
./gradlew :app-main:test --tests "net.causw.app.main.domain.community.comment.service.implementation.CommentReaderTest"
```

Expected: pass.

- [ ] **Step 5: Commit**

```bash
git add app-main/src/main/java/net/causw/app/main/domain/community/comment/repository/CommentRepository.java app-main/src/main/java/net/causw/app/main/domain/community/comment/service/implementation/CommentReader.java app-main/src/test/java/net/causw/app/main/domain/community/comment/service/implementation/CommentReaderTest.java
git commit -m "feat: 댓글 조회를 단일 테이블 기반으로 변경"
```

---

### Task 4: Root and Reply Service Flow

**Files:**
- Modify: `app-main/src/main/java/net/causw/app/main/domain/community/comment/service/dto/CommentCreateCommand.java`
- Modify: `app-main/src/main/java/net/causw/app/main/domain/community/comment/service/CommentService.java`
- Modify: `app-main/src/main/java/net/causw/app/main/domain/community/comment/util/CommentValidator.java`
- Test: `app-main/src/test/java/net/causw/app/main/domain/community/comment/service/CommentServiceTest.java`

- [ ] **Step 1: Write failing service tests**

Add tests for:

- Creating root comment with `parentCommentId = null` publishes `PostCommentCreatedEvent`.
- Creating reply with `parentCommentId` publishes `CommentChildCommentCreatedEvent`.
- Creating reply under reply throws `COMMENT_NOT_FOUND` or the chosen domain error.

- [ ] **Step 2: Run test and verify RED**

```bash
./gradlew :app-main:test --tests "net.causw.app.main.domain.community.comment.service.CommentServiceTest"
```

Expected: fails because command and service do not support `parentCommentId`.

- [ ] **Step 3: Implement service flow**

Change creation logic:

- If `parentCommentId == null`, read post by `postId` and call root factory.
- If `parentCommentId != null`, read parent comment, inherit parent post, validate parent is root, and call reply factory.
- Use existing permission validation against the resolved post.

- [ ] **Step 4: Run test and verify GREEN**

```bash
./gradlew :app-main:test --tests "net.causw.app.main.domain.community.comment.service.CommentServiceTest"
```

Expected: pass.

- [ ] **Step 5: Commit**

```bash
git add app-main/src/main/java/net/causw/app/main/domain/community/comment/service/dto/CommentCreateCommand.java app-main/src/main/java/net/causw/app/main/domain/community/comment/service/CommentService.java app-main/src/main/java/net/causw/app/main/domain/community/comment/util/CommentValidator.java app-main/src/test/java/net/causw/app/main/domain/community/comment/service/CommentServiceTest.java
git commit -m "feat: 댓글 작성 흐름을 루트와 답글로 통합"
```

---

### Task 5: Compatibility Child Comment API

**Files:**
- Modify: `app-main/src/main/java/net/causw/app/main/domain/community/comment/service/ChildCommentService.java`
- Modify: `app-main/src/main/java/net/causw/app/main/domain/community/comment/api/v2/controller/ChildCommentController.java`
- Modify child comment DTO mappers as needed.
- Test: `app-main/src/test/java/net/causw/app/main/domain/community/comment/service/ChildCommentServiceTest.java`

- [ ] **Step 1: Write failing compatibility tests**

Tests should prove `/child-comments` service methods delegate to unified comment operations and return child DTO-compatible results.

- [ ] **Step 2: Run test and verify RED**

```bash
./gradlew :app-main:test --tests "net.causw.app.main.domain.community.comment.service.ChildCommentServiceTest"
```

Expected: fails because the service still depends on `ChildComment`.

- [ ] **Step 3: Convert `ChildCommentService` into a wrapper**

Use `CommentService` for create, update, delete, like, and unlike. Keep public method signatures until controller compatibility is removed.

- [ ] **Step 4: Run test and verify GREEN**

```bash
./gradlew :app-main:test --tests "net.causw.app.main.domain.community.comment.service.ChildCommentServiceTest"
```

Expected: pass.

- [ ] **Step 5: Commit**

```bash
git add app-main/src/main/java/net/causw/app/main/domain/community/comment/service/ChildCommentService.java app-main/src/main/java/net/causw/app/main/domain/community/comment/api/v2/controller/ChildCommentController.java app-main/src/test/java/net/causw/app/main/domain/community/comment/service/ChildCommentServiceTest.java
git commit -m "feat: 대댓글 API를 통합 댓글 서비스로 연결"
```

---

### Task 6: Unified Likes and Metadata

**Files:**
- Modify: `app-main/src/main/java/net/causw/app/main/domain/community/comment/repository/LikeCommentRepository.java`
- Modify: `app-main/src/main/java/net/causw/app/main/domain/community/comment/repository/LikeCommentQueryRepository.java`
- Modify: `app-main/src/main/java/net/causw/app/main/domain/community/comment/service/implementation/CommentMetaReader.java`
- Modify: `app-main/src/main/java/net/causw/app/main/domain/community/comment/service/implementation/CommentMapper.java`
- Test: comment service and mapper tests.

- [ ] **Step 1: Write failing tests**

Add tests proving reply likes use `LikeComment` and reply like counts appear in child response DTOs.

- [ ] **Step 2: Run focused tests and verify RED**

```bash
./gradlew :app-main:test --tests "net.causw.app.main.domain.community.comment.service.CommentServiceTest"
```

Expected: fails because reply likes still use `LikeChildComment`.

- [ ] **Step 3: Replace child-like readers with comment-like readers**

Use comment IDs for both root comments and replies. Remove active `LikeChildCommentReader` and `LikeChildCommentWriter` dependencies from service logic.

- [ ] **Step 4: Run focused tests and verify GREEN**

```bash
./gradlew :app-main:test --tests "net.causw.app.main.domain.community.comment.service.CommentServiceTest"
```

Expected: pass.

- [ ] **Step 5: Commit**

```bash
git add app-main/src/main/java/net/causw/app/main/domain/community/comment app-main/src/test/java/net/causw/app/main/domain/community/comment
git commit -m "feat: 댓글 좋아요를 단일 모델로 통합"
```

---

### Task 7: Report, Block, Notification, and Post Count Integration

**Files:**
- Modify report service and repository files under `app-main/src/main/java/net/causw/app/main/domain/community/report/`
- Modify block service files under `app-main/src/main/java/net/causw/app/main/domain/user/relation/`
- Modify notification listener files under `app-main/src/main/java/net/causw/app/main/domain/notification/notification/`
- Modify post count queries under `app-main/src/main/java/net/causw/app/main/domain/community/post/`
- Test affected service tests.

- [ ] **Step 1: Write failing integration tests**

Add or update tests proving:

- Reply report resolves a `Comment` target.
- Reply block resolves a `Comment` target.
- Reply notification reads the reply as `Comment`.
- Post comment count counts root and reply comments from `tb_comment`.

- [ ] **Step 2: Run focused tests and verify RED**

```bash
./gradlew :app-main:test --tests "net.causw.app.main.domain.community.report.service.ReportAdminServiceTest" --tests "net.causw.app.main.domain.notification.notification.service.listener.CommentNotificationListenerTest"
```

Expected: fails on `ChildComment` dependencies or stale query expectations.

- [ ] **Step 3: Convert integrations to `Comment`**

Keep external enum/scope names for compatibility if changing them would break persisted data or API contracts. Internally use `CommentReader`.

- [ ] **Step 4: Run focused tests and verify GREEN**

```bash
./gradlew :app-main:test --tests "net.causw.app.main.domain.community.report.service.ReportAdminServiceTest" --tests "net.causw.app.main.domain.notification.notification.service.listener.CommentNotificationListenerTest"
```

Expected: pass.

- [ ] **Step 5: Commit**

```bash
git add app-main/src/main/java/net/causw/app/main/domain/community/report app-main/src/main/java/net/causw/app/main/domain/user/relation app-main/src/main/java/net/causw/app/main/domain/notification/notification app-main/src/main/java/net/causw/app/main/domain/community/post app-main/src/test/java/net/causw/app/main/domain/community/report app-main/src/test/java/net/causw/app/main/domain/notification
git commit -m "feat: 답글 연동 기능을 통합 댓글 기반으로 변경"
```

---

### Task 8: Remove Active ChildComment Types

**Files:**
- Delete or stop compiling `ChildComment` entity, repository, like entity, child-specific readers/writers/query repositories.
- Modify fixtures under `app-main/src/test/java/net/causw/app/main/util/ObjectFixtures.java`

- [ ] **Step 1: Compile to expose stale references**

```bash
./gradlew :app-main:compileJava :app-main:compileTestJava
```

Expected: failures identify remaining `ChildComment` references.

- [ ] **Step 2: Remove or convert stale references**

Delete files that have no compatibility purpose and update imports to `Comment`.

- [ ] **Step 3: Compile again**

```bash
./gradlew :app-main:compileJava :app-main:compileTestJava
```

Expected: pass.

- [ ] **Step 4: Commit**

```bash
git add app-main/src/main/java app-main/src/test/java
git commit -m "feat: 대댓글 전용 도메인 타입 제거"
```

---

### Task 9: Final Verification

**Files:**
- No planned code changes unless verification exposes failures.

- [ ] **Step 1: Run focused comment tests**

```bash
./gradlew :app-main:test --tests "net.causw.app.main.domain.community.comment.*"
```

Expected: pass.

- [ ] **Step 2: Run compile and formatting**

```bash
./gradlew :app-main:compileJava :app-main:compileTestJava :app-main:spotlessCheck
```

Expected: pass.

- [ ] **Step 3: Run broader tests if compile changed shared integrations**

```bash
./gradlew :app-main:test
```

Expected: pass, or report unrelated existing failures with exact test names.

- [ ] **Step 4: Commit verification fixes if needed**

```bash
git add <fixed-files>
git commit -m "feat: 댓글 계층형 구조 검증 보완"
```

---

## Self-Review

- Spec coverage: the plan covers schema migration, entity conversion, repository/service/API compatibility, likes, report/block/notification/post integrations, stale type removal, and verification.
- Placeholder scan: no task uses TBD/TODO. Each task has concrete files, commands, and expected outcomes.
- Type consistency: the unified target type is always `Comment`; child-comment compatibility remains only at API/service boundary until cleanup.

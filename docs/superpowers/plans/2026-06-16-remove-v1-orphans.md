# Remove V1 Orphans Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove orphaned v1 logic after v1 API endpoint removal while moving still-used runtime code to v2 or non-versioned packages.

**Architecture:** Break direct v2/non-versioned imports from `api.v1`, `service.v1`, and `repository.v1` first, then delete v1 packages once static references are gone. Preserve behavior by moving small shared DTOs/utilities and by replacing entity methods that accept API DTOs with service DTOs or primitive parameters.

**Tech Stack:** Java 17, Spring Boot 3.2, Gradle, JPA, MapStruct, Spotless.

---

## File Map

- Modify: `app-main/src/main/java/net/causw/app/main/domain/user/auth/service/AuthService.java` to import the moved password generator.
- Move: `app-main/src/main/java/net/causw/app/main/domain/user/account/service/v1/PasswordGenerator.java` to `app-main/src/main/java/net/causw/app/main/domain/user/auth/service/implementation/PasswordGenerator.java`.
- Create: `app-main/src/main/java/net/causw/app/main/domain/user/account/api/v2/dto/response/UserFcmTokenResponse.java`.
- Modify: `app-main/src/main/java/net/causw/app/main/domain/user/account/api/v2/controller/UserController.java` to return the v2 FCM response.
- Modify: `app-main/src/main/java/net/causw/app/main/domain/user/account/service/UserNotificationService.java` to map FCM responses without v1 mapper.
- Modify: `app-main/src/main/java/net/causw/app/main/domain/user/account/entity/user/User.java` to remove v1 API DTO imports.
- Modify: `app-main/src/main/java/net/causw/app/main/domain/community/form/entity/Form.java` and `app-main/src/main/java/net/causw/app/main/domain/community/form/entity/FormQuestion.java` to remove v1 API DTO imports.
- Modify: shared runtime services importing v1 notification/block/query types after focused reference scans.
- Delete: `app-main/src/main/java/net/causw/app/main/domain/**/api/v1`, `service/v1`, and `repository/v1` files that have zero non-test references.

## Tasks

### Task 1: Commit Plan

**Files:**
- Create: `docs/superpowers/plans/2026-06-16-remove-v1-orphans.md`

- [ ] **Step 1: Add the plan file**

Use the content in this file.

- [ ] **Step 2: Commit**

```bash
git add -f docs/superpowers/plans/2026-06-16-remove-v1-orphans.md
git commit -m "문서: v1 고아 로직 제거 계획 추가"
```

### Task 2: Move Small V2-Used Types

**Files:**
- Move: `app-main/src/main/java/net/causw/app/main/domain/user/account/service/v1/PasswordGenerator.java`
- Create: `app-main/src/main/java/net/causw/app/main/domain/user/account/api/v2/dto/response/UserFcmTokenResponse.java`
- Modify: `app-main/src/main/java/net/causw/app/main/domain/user/auth/service/AuthService.java`
- Modify: `app-main/src/main/java/net/causw/app/main/domain/user/account/api/v2/controller/UserController.java`
- Modify: `app-main/src/main/java/net/causw/app/main/domain/user/account/service/UserNotificationService.java`

- [ ] **Step 1: Move `PasswordGenerator` package**

Change package to:

```java
package net.causw.app.main.domain.user.auth.service.implementation;
```

Update `AuthService` import to:

```java
import net.causw.app.main.domain.user.auth.service.implementation.PasswordGenerator;
```

- [ ] **Step 2: Add v2 FCM response**

```java
package net.causw.app.main.domain.user.account.api.v2.dto.response;

import java.util.Set;

import lombok.Builder;

@Builder
public record UserFcmTokenResponse(
	Set<String> fcmTokens
) {
}
```

- [ ] **Step 3: Replace FCM mappings**

In `UserNotificationService`, return `UserFcmTokenResponse` and construct it from `User#getFcmTokenEntities()`.

- [ ] **Step 4: Update v2 controller imports and return types**

Replace `UserFcmTokenResponseDto` with `UserFcmTokenResponse`.

- [ ] **Step 5: Verify no direct v2 import from v1 remains**

Run:

```bash
rg "api\\.v1|service\\.v1|repository\\.v1" app-main/src/main/java/net/causw/app/main/domain/*/*/api/v2 app-main/src/main/java/net/causw/app/main/domain/*/*/service/v2 app-main/src/main/java/net/causw/app/main/domain/user/auth/service/AuthService.java -g '*.java'
```

Expected: no output.

- [ ] **Step 6: Commit**

```bash
git add app-main/src/main/java/net/causw/app/main/domain/user/auth/service/AuthService.java app-main/src/main/java/net/causw/app/main/domain/user/auth/service/implementation/PasswordGenerator.java app-main/src/main/java/net/causw/app/main/domain/user/account/api/v2/controller/UserController.java app-main/src/main/java/net/causw/app/main/domain/user/account/api/v2/dto/response/UserFcmTokenResponse.java app-main/src/main/java/net/causw/app/main/domain/user/account/service/UserNotificationService.java
git add -u app-main/src/main/java/net/causw/app/main/domain/user/account/service/v1/PasswordGenerator.java
git commit -m "리팩터: v2 사용 v1 유틸과 응답 DTO 이동"
```

### Task 3: Remove Entity Dependencies On V1 API DTOs

**Files:**
- Modify: `app-main/src/main/java/net/causw/app/main/domain/user/account/entity/user/User.java`
- Modify: `app-main/src/main/java/net/causw/app/main/domain/community/form/entity/Form.java`
- Modify: `app-main/src/main/java/net/causw/app/main/domain/community/form/entity/FormQuestion.java`
- Modify callers found by `rg "User\\.from\\(|User\\.createGraduate\\(|Form\\.create|FormQuestion\\.create" app-main/src/main/java`

- [ ] **Step 1: Replace `User.from(UserCreateRequestDto, String)`**

If no production caller remains, delete the method. If a caller remains, introduce a service DTO in the caller package and keep the entity method independent of API DTOs.

- [ ] **Step 2: Replace `User.createGraduate(GraduatedUserCommand, String)`**

Move `GraduatedUserCommand` to `service/dto/request` or inline parameters at the service boundary, then update `User`.

- [ ] **Step 3: Replace form creation DTO coupling**

Change `Form.createPostForm`, `Form.createCircleApplicationForm`, `FormQuestion.createObjectiveQuestion`, and `FormQuestion.createSubjectQuestion` so they no longer accept v1 request DTOs.

- [ ] **Step 4: Verify entity imports**

Run:

```bash
rg "api\\.v1" app-main/src/main/java/net/causw/app/main/domain/*/*/entity -g '*.java'
```

Expected: no output.

- [ ] **Step 5: Commit**

```bash
git add app-main/src/main/java/net/causw/app/main/domain/user/account/entity/user/User.java app-main/src/main/java/net/causw/app/main/domain/community/form/entity/Form.java app-main/src/main/java/net/causw/app/main/domain/community/form/entity/FormQuestion.java
git commit -m "리팩터: 엔티티의 v1 API DTO 의존 제거"
```

### Task 4: Move Shared Runtime Services Out Of V1 Packages

**Files:**
- Modify files listed by:

```bash
rg "service\\.v1|api\\.v1|repository\\.v1" app-main/src/main/java/net/causw/app/main/domain -g '*.java' | rg -v '/api/v1/|/service/v1/|/repository/v1/'
```

- [ ] **Step 1: Move or replace block service dependency**

Move `UserBlockEntityService` behavior to `domain/user/relation/service/v2/implementation` or use an existing v2 implementation if present.

- [ ] **Step 2: Move or replace notification dependency**

Move still-used notification push/log behavior from `notification/notification/service/v1` to `notification/notification/service/implementation` or existing non-versioned service packages.

- [ ] **Step 3: Rename query repository if still used**

If `UserInfoQueryV1Repository` is still required, rename it to a non-versioned or v2 query repository and replace `UserInfoSearchConditionDto` with a service query DTO.

- [ ] **Step 4: Verify non-v1 package imports**

Run:

```bash
rg "service\\.v1|api\\.v1|repository\\.v1" app-main/src/main/java/net/causw/app/main/domain -g '*.java' | rg -v '/api/v1/|/service/v1/|/repository/v1/'
```

Expected: no output.

- [ ] **Step 5: Commit**

```bash
git add app-main/src/main/java/net/causw/app/main/domain
git commit -m "리팩터: 공유 런타임 서비스의 v1 패키지 의존 제거"
```

### Task 5: Delete Orphaned V1 Packages

**Files:**
- Delete zero-reference v1 files under `app-main/src/main/java/net/causw/app/main/domain`
- Delete corresponding obsolete v1 tests under `app-main/src/test/java/net/causw/app/main/domain` only when their production target was deleted.

- [ ] **Step 1: List remaining v1 files**

Run:

```bash
find app-main/src/main/java/net/causw/app/main/domain -type f \( -path '*/api/v1/*' -o -path '*/service/v1/*' -o -path '*/repository/v1/*' \) | sort
```

- [ ] **Step 2: Delete remaining orphaned v1 files**

Use `git rm` for files that have no remaining production reference.

- [ ] **Step 3: Compile**

Run:

```bash
./gradlew :app-main:compileJava
```

Expected: build succeeds.

- [ ] **Step 4: Commit**

```bash
git add app-main/src/main/java app-main/src/test/java
git commit -m "삭제: 고아 v1 로직 제거"
```

### Task 6: Format And Final Verification

**Files:**
- Modify formatting-only changes generated by Spotless if needed.

- [ ] **Step 1: Run formatting check**

```bash
./gradlew :app-main:spotlessCheck
```

Expected: build succeeds.

- [ ] **Step 2: Apply formatting if needed**

```bash
./gradlew :app-main:spotlessApply
```

- [ ] **Step 3: Run focused tests**

```bash
./gradlew :app-main:test --continue
```

Expected: build succeeds or only unrelated pre-existing failures are reported with evidence.

- [ ] **Step 4: Commit verification fixes if any**

```bash
git add app-main/src/main/java app-main/src/test/java
git commit -m "정리: v1 로직 제거 후 포맷과 검증 보정"
```

## Self-Review

- Spec coverage: the plan covers moved v2-used DTO/util, entity dependency cleanup, shared runtime service movement, v1 package deletion, formatting, and verification.
- Placeholder scan: no implementation step relies on a vague placeholder; the only discovery steps use exact commands because the referenced files are determined by static analysis at execution time.
- Type consistency: `UserFcmTokenResponse` is the v2 replacement for `UserFcmTokenResponseDto`; `PasswordGenerator` remains the same component class after package movement.

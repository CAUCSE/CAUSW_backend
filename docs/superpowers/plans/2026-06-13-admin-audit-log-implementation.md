# Admin Audit Log Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build common admin audit logging backed by `tb_admin_audit_log`, migrate existing user admin action logs into it, and expose `GET /api/v2/admin/audit-logs`.

**Architecture:** Add a new `domain/admin/audit` feature area for cross-domain admin audit storage and viewing. User admin actions are written to `AdminAuditLog` through a common writer, existing `tb_user_admin_action_log` rows are backfilled, and `AdminAuditLogQueryRepository` reads only the common audit table.

**Tech Stack:** Java 17, Spring Boot MVC, Spring Security `@PreAuthorize`, Spring Data `Page`, JPA, QueryDSL, Flyway, Jackson JSON serialization, JUnit 5, Mockito, AssertJ.

---

## File Map

- Create Flyway migration in `app-main/src/main/resources/db/migration`: create `tb_admin_audit_log`, add indexes, and backfill existing `tb_user_admin_action_log` rows.
- Create `app-main/src/main/java/net/causw/app/main/domain/admin/audit/entity/AdminAuditLog.java`: common admin audit log entity.
- Create `app-main/src/main/java/net/causw/app/main/domain/admin/audit/enums/AdminAuditLogCategory.java`: audit category enum, first value `USER`.
- Create `app-main/src/main/java/net/causw/app/main/domain/admin/audit/api/v2/dto/request/AdminAuditLogRequest.java`: query parameter DTO.
- Create `app-main/src/main/java/net/causw/app/main/domain/admin/audit/api/v2/dto/response/AuditActorResponse.java`: actor DTO.
- Create `app-main/src/main/java/net/causw/app/main/domain/admin/audit/api/v2/dto/response/AuditTargetResponse.java`: target DTO.
- Create `app-main/src/main/java/net/causw/app/main/domain/admin/audit/api/v2/dto/response/AdminAuditLogResponse.java`: common API response DTO.
- Create `app-main/src/main/java/net/causw/app/main/domain/admin/audit/service/dto/AdminAuditLogCondition.java`: service/query condition with `String actionType`.
- Create `app-main/src/main/java/net/causw/app/main/domain/admin/audit/service/dto/AdminAuditLogItem.java`: repository result item with snapshot fields and `String metadataJson`.
- Create `app-main/src/main/java/net/causw/app/main/domain/admin/audit/service/dto/AdminAuditLogCreateCommand.java`: common write command with `String actionType` and `Map<String, Object> metadata`.
- Create `app-main/src/main/java/net/causw/app/main/domain/admin/audit/repository/AdminAuditLogRepository.java`: JPA repository for writes.
- Create `app-main/src/main/java/net/causw/app/main/domain/admin/audit/repository/AdminAuditLogQueryRepository.java`: QueryDSL adapter over `AdminAuditLog`.
- Create `app-main/src/main/java/net/causw/app/main/domain/admin/audit/service/implementation/AdminAuditLogWriter.java`: common writer.
- Create `app-main/src/main/java/net/causw/app/main/domain/admin/audit/service/AdminAuditLogService.java`: validates and normalizes requests.
- Create `app-main/src/main/java/net/causw/app/main/domain/admin/audit/api/v2/mapper/AdminAuditLogMapper.java`: request/response mapper.
- Create `app-main/src/main/java/net/causw/app/main/domain/admin/audit/api/v2/controller/AdminAuditLogController.java`: admin endpoint.
- Modify `app-main/src/main/java/net/causw/app/main/domain/user/account/service/implementation/UserAdminActionLogWriter.java`: delegate user admin action logging to `AdminAuditLogWriter` or replace usage with the common writer.
- Modify `app-main/src/main/java/net/causw/app/main/domain/user/account/service/UserAdminService.java`: ensure user admin action changes write common admin audit logs.
- Create `app-main/src/test/java/net/causw/app/main/domain/admin/audit/api/v2/mapper/AdminAuditLogMapperTest.java`: mapper behavior tests.
- Create `app-main/src/test/java/net/causw/app/main/domain/admin/audit/service/AdminAuditLogServiceTest.java`: service validation/normalization tests.
- Create `app-main/src/test/java/net/causw/app/main/domain/admin/audit/service/implementation/AdminAuditLogWriterTest.java`: writer command/entity mapping tests.
- Create `app-main/src/test/java/net/causw/app/main/domain/admin/audit/repository/AdminAuditLogQueryRepositoryTest.java`: query filter and ordering tests over `tb_admin_audit_log`.
- Create `app-main/src/test/java/net/causw/app/main/domain/admin/audit/api/v2/controller/AdminAuditLogControllerTest.java`: controller response and authorization tests.

## Task 1: Schema And Entity

**Files:**
- Create: Flyway migration under `app-main/src/main/resources/db/migration`
- Create: `app-main/src/main/java/net/causw/app/main/domain/admin/audit/entity/AdminAuditLog.java`
- Create: `app-main/src/main/java/net/causw/app/main/domain/admin/audit/repository/AdminAuditLogRepository.java`

- [ ] **Step 1: Create Flyway migration**

Create a forward migration that:
- Creates `tb_admin_audit_log`.
- Adds columns: `id`, `created_at`, `updated_at`, `category`, `action_type`, `action_description`, `actor_user_id`, `actor_email`, `actor_name`, `actor_student_id`, `target_type`, `target_id`, `target_email`, `target_name`, `target_student_id`, `summary`, `metadata_json`.
- Uses `TEXT` for `metadata_json` unless the project DB compatibility decision explicitly allows `JSON`.
- Adds indexes for `created_at`, `(category, action_type, created_at)`, `(actor_email, created_at)`, `(actor_name, created_at)`, `(actor_student_id, created_at)`, `(target_email, created_at)`, `(target_name, created_at)`, `(target_student_id, created_at)`.
- Backfills existing `tb_user_admin_action_log` rows into `tb_admin_audit_log`.
- During backfill, set `category = 'USER'`, `target_type = 'USER'`, copy action type as its enum name, derive action description from `DROP`, `RESTORE`, and `ROLE_CHANGE`, and build summary using the API summary rules.
- During backfill, join the user table where possible to fill actor/target name and student id. Leave missing snapshot values nullable.
- During backfill, serialize `beforeState`, `afterState`, `beforeRoles`, `afterRoles`, and `reason` into a JSON object string in `metadata_json`.
- Leaves `tb_user_admin_action_log` in place as deprecated data.

- [ ] **Step 2: Implement entity and repository**

Implement `AdminAuditLog` as a `BaseEntity` entity mapped to `tb_admin_audit_log`.

Entity requirements:
- `category`: `AdminAuditLogCategory` with `@Enumerated(EnumType.STRING)`
- `actionType`: `String`
- `actionDescription`: `String`
- actor snapshot fields: user id, email, name, student id
- target snapshot fields: type, id, email, name, student id
- `summary`: `String`
- `metadataJson`: `String` mapped to `metadata_json` with `TEXT` column definition

Implement `AdminAuditLogRepository extends JpaRepository<AdminAuditLog, String>`.

- [ ] **Step 3: Compile**

Run: `./gradlew :app-main:compileJava`

Expected: PASS.

## Task 2: Common Writer

**Files:**
- Test: `app-main/src/test/java/net/causw/app/main/domain/admin/audit/service/implementation/AdminAuditLogWriterTest.java`
- Create: `app-main/src/main/java/net/causw/app/main/domain/admin/audit/service/dto/AdminAuditLogCreateCommand.java`
- Create: `app-main/src/main/java/net/causw/app/main/domain/admin/audit/service/implementation/AdminAuditLogWriter.java`
- Modify: `app-main/src/main/java/net/causw/app/main/domain/user/account/service/implementation/UserAdminActionLogWriter.java`
- Modify: `app-main/src/main/java/net/causw/app/main/domain/user/account/service/UserAdminService.java`

- [ ] **Step 1: Write writer tests**

Create tests that verify:
- `DROP` user action writes category `USER`, action type `DROP`, actor snapshot, target snapshot, summary, and metadata.
- `RESTORE` user action writes before/after state and role metadata.
- `ROLE_CHANGE` user action writes before/after role metadata.
- metadata is serialized into a JSON object string containing `beforeState`, `afterState`, `beforeRoles`, `afterRoles`, and `reason`.

- [ ] **Step 2: Implement create command and writer**

Implement `AdminAuditLogCreateCommand` with common fields and metadata:
- `AdminAuditLogCategory category`
- `String actionType`
- `String actionDescription`
- actor snapshot fields
- target snapshot fields
- `String summary`
- `Map<String, Object> metadata`

Implement `AdminAuditLogWriter#write(AdminAuditLogCreateCommand command)`.

Writer requirements:
- Annotate writer with `@Component`, `@RequiredArgsConstructor`, and `@Transactional`.
- Depend on `AdminAuditLogRepository` and Jackson `ObjectMapper`.
- Serialize command metadata to `metadataJson`.
- Save only `AdminAuditLog`, not `UserAdminActionLog`.

- [ ] **Step 3: Move user action log writes to common writer**

Replace the user admin action persistence path so new `DROP`, `RESTORE`, and `ROLE_CHANGE` events are stored in `tb_admin_audit_log`.

Preserve the current service-level capture of before/after state and roles.

Do not make the common admin audit package depend on `UserAdminActionType`. If the user account package still uses that enum, convert it to `actionType.name()` before building `AdminAuditLogCreateCommand`.

- [ ] **Step 4: Run writer and user admin service tests**

Run:
- `./gradlew :app-main:test --tests "net.causw.app.main.domain.admin.audit.service.implementation.AdminAuditLogWriterTest"`
- `./gradlew :app-main:test --tests "net.causw.app.main.domain.user.account.service.UserAdminServiceTest"`

Expected: PASS.

## Task 3: Mapper Contract

**Files:**
- Test: `app-main/src/test/java/net/causw/app/main/domain/admin/audit/api/v2/mapper/AdminAuditLogMapperTest.java`
- Create: API response DTOs, category enum, service item DTO, mapper.

- [ ] **Step 1: Write mapper tests**

Create tests that verify:
- `DROP` maps category/action/description/actor/target/metadata/summary.
- `RESTORE` creates restore summary.
- `ROLE_CHANGE` creates role change summary.
- actor and target responses include email, name, and student id snapshots.
- stored `metadataJson` is deserialized into response `metadata`.

- [ ] **Step 2: Implement mapper DTOs and mapper**

Implement:
- `AdminAuditLogCategory.USER`
- `AuditActorResponse`
- `AuditTargetResponse`
- `AdminAuditLogResponse`
- `AdminAuditLogItem`
- `AdminAuditLogMapper`

Mapper requirements:
- `AdminAuditLogItem.actionType` is `String`, not `UserAdminActionType`.
- `AdminAuditLogItem` includes actor/target name and student id snapshot fields.
- `AdminAuditLogItem` includes `String metadataJson`.
- `AdminAuditLogMapper` uses `ObjectMapper` to deserialize `metadataJson` into `Map<String, Object>`.
- `AdminAuditLogMapper` passes stored `summary` and `actionDescription` through instead of rebuilding them from a user-specific enum.

- [ ] **Step 3: Run mapper test**

Run: `./gradlew :app-main:test --tests "net.causw.app.main.domain.admin.audit.api.v2.mapper.AdminAuditLogMapperTest"`

Expected: PASS.

## Task 4: Service Validation And Normalization

**Files:**
- Test: `app-main/src/test/java/net/causw/app/main/domain/admin/audit/service/AdminAuditLogServiceTest.java`
- Create: `AdminAuditLogRequest`, `AdminAuditLogCondition`, `AdminAuditLogService`

- [ ] **Step 1: Write service tests**

Create tests that verify:
- `from > to` throws `BaseRunTimeV2Exception` with `GlobalErrorCode.BAD_REQUEST`.
- blank keyword becomes `null` before repository call.
- blank action type becomes `null` before repository call.
- unsupported action type throws `BaseRunTimeV2Exception` with `GlobalErrorCode.BAD_REQUEST`.
- `null` category still calls repository with `null`, meaning all currently available categories.
- valid action type is passed to repository as a normalized uppercase `String`, not `UserAdminActionType`.

- [ ] **Step 2: Implement service**

Implement:
- `AdminAuditLogRequest`
- `AdminAuditLogCondition`
- `AdminAuditLogService#getAuditLogs(AdminAuditLogRequest, Pageable)`
- validation with `GlobalErrorCode.BAD_REQUEST.toBaseException()`

Service requirements:
- `AdminAuditLogCondition.actionType` is `String`.
- Normalize blank `keyword` and blank `actionType` to `null`.
- Normalize non-blank `actionType` to uppercase.
- For `category == null` or `category == USER`, allow `DROP`, `RESTORE`, and `ROLE_CHANGE`.
- Throw `GlobalErrorCode.BAD_REQUEST.toBaseException()` for unsupported action types.

- [ ] **Step 3: Run service test**

Run: `./gradlew :app-main:test --tests "net.causw.app.main.domain.admin.audit.service.AdminAuditLogServiceTest"`

Expected: PASS.

## Task 5: Query Repository And Controller

**Files:**
- Create: `app-main/src/main/java/net/causw/app/main/domain/admin/audit/repository/AdminAuditLogQueryRepository.java`
- Create: `app-main/src/main/java/net/causw/app/main/domain/admin/audit/api/v2/controller/AdminAuditLogController.java`
- Test: `app-main/src/test/java/net/causw/app/main/domain/admin/audit/repository/AdminAuditLogQueryRepositoryTest.java`
- Test: `app-main/src/test/java/net/causw/app/main/domain/admin/audit/api/v2/controller/AdminAuditLogControllerTest.java`
- Modify only if required by compilation: imports generated by earlier files.

- [ ] **Step 1: Write query repository tests**

Create tests that persist `AdminAuditLog` rows and verify:
- `from` and `to` filter by `createdAt`.
- `category` filters rows.
- `actionType` filters rows by string value.
- `keyword` searches actor email/name/student id and target email/name/student id.
- results are ordered by `createdAt.desc()`.
- rows that exist only in `tb_user_admin_action_log` are not returned.

- [ ] **Step 2: Implement QueryDSL repository**

Implement `AdminAuditLogQueryRepository#findAuditLogs(AdminAuditLogCondition, Pageable)`:
- Query `QAdminAuditLog.adminAuditLog`.
- Apply optional `from`, `to`, `category`, `actionType`, and `keyword`.
- Keyword searches actor email/name/student id and target email/name/student id.
- Order by `createdAt.desc()`.
- Return `Page<AdminAuditLogItem>` using `PageableExecutionUtils`.
- Do not import or query `QUserAdminActionLog`.

- [ ] **Step 3: Write controller tests**

Create tests that verify:
- an admin can call `GET /api/v2/admin/audit-logs` and receives `ApiResponse<PageResponse<AdminAuditLogResponse>>`.
- a non-admin principal is forbidden by `@PreAuthorize("@security.hasRole(@Role.ADMIN)")`.
- request parameters bind to `AdminAuditLogRequest` and `Pageable`.

- [ ] **Step 4: Implement controller**

Implement `AdminAuditLogController`:
- `@RequestMapping("/api/v2/admin/audit-logs")`
- `@PreAuthorize("@security.hasRole(@Role.ADMIN)")`
- `GET` method with `@ParameterObject AdminAuditLogRequest` and `@ParameterObject @PageableDefault(page = 0, size = 10) Pageable`
- return `ApiResponse.success(PageResponse.from(service.getAuditLogs(request, pageable).map(mapper::toResponse)))`

- [ ] **Step 5: Run repository and controller tests**

Run:
- `./gradlew :app-main:test --tests "net.causw.app.main.domain.admin.audit.repository.AdminAuditLogQueryRepositoryTest"`
- `./gradlew :app-main:test --tests "net.causw.app.main.domain.admin.audit.api.v2.controller.AdminAuditLogControllerTest"`

Expected: PASS.

- [ ] **Step 6: Compile**

Run: `./gradlew :app-main:compileJava`

Expected: PASS.

## Task 6: Verification

**Files:**
- All files created or modified above.

- [ ] **Step 1: Run focused tests**

Run:
- `./gradlew :app-main:test --tests "net.causw.app.main.domain.admin.audit.api.v2.mapper.AdminAuditLogMapperTest"`
- `./gradlew :app-main:test --tests "net.causw.app.main.domain.admin.audit.service.AdminAuditLogServiceTest"`
- `./gradlew :app-main:test --tests "net.causw.app.main.domain.admin.audit.service.implementation.AdminAuditLogWriterTest"`
- `./gradlew :app-main:test --tests "net.causw.app.main.domain.admin.audit.repository.AdminAuditLogQueryRepositoryTest"`
- `./gradlew :app-main:test --tests "net.causw.app.main.domain.admin.audit.api.v2.controller.AdminAuditLogControllerTest"`
- `./gradlew :app-main:test --tests "net.causw.app.main.domain.user.account.service.UserAdminServiceTest"`

Expected: PASS.

- [ ] **Step 2: Run formatting check**

Run: `./gradlew :app-main:spotlessCheck`

Expected: PASS.

- [ ] **Step 3: Validate Flyway and review diff**

Run:
- `./gradlew flywayValidate`
- `git status --short`
- `git diff --check`

Expected:
- Flyway validation passes.
- only intended implementation files plus docs are modified.
- no whitespace errors.

This plan includes schema changes, so the PR needs the `db-change` label.

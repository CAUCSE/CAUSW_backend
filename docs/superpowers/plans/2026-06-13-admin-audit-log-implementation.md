# Admin Audit Log Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build `GET /api/v2/admin/audit-logs` so admins can list existing user admin action logs through a common audit log response shape.

**Architecture:** Add a new `domain/admin/audit` feature area for cross-domain admin audit viewing. The first data source is `UserAdminActionLog`, queried through `AdminAuditLogQueryRepository` and mapped to common API DTOs by `AdminAuditLogMapper`.

**Tech Stack:** Java 17, Spring Boot MVC, Spring Security `@PreAuthorize`, Spring Data `Page`, QueryDSL, MapStruct-style component mapper, JUnit 5, Mockito, AssertJ.

---

## File Map

- Create `app-main/src/main/java/net/causw/app/main/domain/admin/audit/enums/AdminAuditLogCategory.java`: audit category enum, first value `USER`.
- Create `app-main/src/main/java/net/causw/app/main/domain/admin/audit/api/v2/dto/request/AdminAuditLogRequest.java`: query parameter DTO.
- Create `app-main/src/main/java/net/causw/app/main/domain/admin/audit/api/v2/dto/response/AuditActorResponse.java`: actor DTO.
- Create `app-main/src/main/java/net/causw/app/main/domain/admin/audit/api/v2/dto/response/AuditTargetResponse.java`: target DTO.
- Create `app-main/src/main/java/net/causw/app/main/domain/admin/audit/api/v2/dto/response/AdminAuditLogResponse.java`: common API response DTO.
- Create `app-main/src/main/java/net/causw/app/main/domain/admin/audit/service/dto/AdminAuditLogCondition.java`: service/query condition.
- Create `app-main/src/main/java/net/causw/app/main/domain/admin/audit/service/dto/AdminAuditLogItem.java`: repository result item.
- Create `app-main/src/main/java/net/causw/app/main/domain/admin/audit/repository/AdminAuditLogQueryRepository.java`: QueryDSL adapter over `UserAdminActionLog`.
- Create `app-main/src/main/java/net/causw/app/main/domain/admin/audit/service/AdminAuditLogService.java`: validates and normalizes requests.
- Create `app-main/src/main/java/net/causw/app/main/domain/admin/audit/api/v2/mapper/AdminAuditLogMapper.java`: request/response mapper.
- Create `app-main/src/main/java/net/causw/app/main/domain/admin/audit/api/v2/controller/AdminAuditLogController.java`: admin endpoint.
- Create `app-main/src/test/java/net/causw/app/main/domain/admin/audit/api/v2/mapper/AdminAuditLogMapperTest.java`: mapper behavior tests.
- Create `app-main/src/test/java/net/causw/app/main/domain/admin/audit/service/AdminAuditLogServiceTest.java`: service validation/normalization tests.

## Task 1: Mapper Contract

**Files:**
- Test: `app-main/src/test/java/net/causw/app/main/domain/admin/audit/api/v2/mapper/AdminAuditLogMapperTest.java`
- Create: API response DTOs, category enum, service item DTO, mapper.

- [ ] **Step 1: Write failing mapper tests**

Create tests that instantiate `AdminAuditLogMapper` and verify:
- `DROP` maps category/action/description/actor/target/metadata/summary.
- `RESTORE` creates restore summary.
- `ROLE_CHANGE` creates role change summary.

- [ ] **Step 2: Run mapper test and verify RED**

Run: `./gradlew :app-main:test --tests "net.causw.app.main.domain.admin.audit.api.v2.mapper.AdminAuditLogMapperTest"`

Expected: FAIL because mapper and DTO classes do not exist.

- [ ] **Step 3: Implement minimal mapper DTOs and mapper**

Implement:
- `AdminAuditLogCategory.USER`
- `AuditActorResponse`
- `AuditTargetResponse`
- `AdminAuditLogResponse`
- `AdminAuditLogItem`
- `AdminAuditLogMapper`

- [ ] **Step 4: Run mapper test and verify GREEN**

Run: `./gradlew :app-main:test --tests "net.causw.app.main.domain.admin.audit.api.v2.mapper.AdminAuditLogMapperTest"`

Expected: PASS.

## Task 2: Service Validation And Normalization

**Files:**
- Test: `app-main/src/test/java/net/causw/app/main/domain/admin/audit/service/AdminAuditLogServiceTest.java`
- Create: `AdminAuditLogRequest`, `AdminAuditLogCondition`, `AdminAuditLogService`

- [ ] **Step 1: Write failing service tests**

Create tests that verify:
- `from > to` throws `BaseRunTimeV2Exception` with `GlobalErrorCode.BAD_REQUEST`.
- blank keyword becomes `null` before repository call.
- `null` category still calls repository with `null`, meaning all currently available categories.

- [ ] **Step 2: Run service test and verify RED**

Run: `./gradlew :app-main:test --tests "net.causw.app.main.domain.admin.audit.service.AdminAuditLogServiceTest"`

Expected: FAIL because service/request/condition classes do not exist.

- [ ] **Step 3: Implement minimal service**

Implement:
- `AdminAuditLogRequest`
- `AdminAuditLogCondition`
- `AdminAuditLogService#getAuditLogs(AdminAuditLogRequest, Pageable)`
- validation with `GlobalErrorCode.BAD_REQUEST.toBaseException()`

- [ ] **Step 4: Run service test and verify GREEN**

Run: `./gradlew :app-main:test --tests "net.causw.app.main.domain.admin.audit.service.AdminAuditLogServiceTest"`

Expected: PASS.

## Task 3: Query Repository And Controller

**Files:**
- Create: `AdminAuditLogQueryRepository`
- Create: `AdminAuditLogController`
- Modify only if required by compilation: imports generated by earlier files.

- [ ] **Step 1: Implement QueryDSL repository**

Implement `AdminAuditLogQueryRepository#findAuditLogs(AdminAuditLogCondition, Pageable)`:
- Query `QUserAdminActionLog.userAdminActionLog`.
- Apply optional `from`, `to`, `category`, `actionType`, `keyword`.
- For non-`USER` category return `Page.empty(pageable)`.
- For unsupported action type return `Page.empty(pageable)`.
- Order by `createdAt.desc()`.
- Return `Page<AdminAuditLogItem>` using `PageableExecutionUtils`.

- [ ] **Step 2: Implement controller**

Implement `AdminAuditLogController`:
- `@RequestMapping("/api/v2/admin/audit-logs")`
- `@PreAuthorize("@security.hasRole(@Role.ADMIN)")`
- `GET` method with `@ParameterObject AdminAuditLogRequest` and `@ParameterObject @PageableDefault(page = 0, size = 10) Pageable`
- return `ApiResponse.success(PageResponse.from(service.getAuditLogs(request, pageable).map(mapper::toResponse)))`

- [ ] **Step 3: Compile**

Run: `./gradlew :app-main:compileJava`

Expected: PASS.

## Task 4: Verification

**Files:**
- All files created above.

- [ ] **Step 1: Run focused tests**

Run:
- `./gradlew :app-main:test --tests "net.causw.app.main.domain.admin.audit.api.v2.mapper.AdminAuditLogMapperTest"`
- `./gradlew :app-main:test --tests "net.causw.app.main.domain.admin.audit.service.AdminAuditLogServiceTest"`

Expected: PASS.

- [ ] **Step 2: Run formatting check**

Run: `./gradlew :app-main:spotlessCheck`

Expected: PASS.

- [ ] **Step 3: Review git diff**

Run: `git status --short` and `git diff --check`.

Expected:
- only intended implementation files plus existing user-requested docs changes are modified.
- no whitespace errors.

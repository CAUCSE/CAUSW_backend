# Admin Audit Log Additional Events Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add first-scope admin audit log events for locker, admission certification, and academic record change admin actions, and expose `LOCKER` and `ACADEMIC` categories through the audit log filter.

**Architecture:** Reuse the existing `admin/audit` domain and generic `AdminAuditLogWriter`. Add small domain-specific audit log writer components near locker, admission, and academic services so services keep orchestration boundaries and metadata construction stays local to each domain.

**Tech Stack:** Java 17, Spring Boot 3.2, JUnit 5, Mockito, AssertJ, Gradle, Spotless.

---

### Task 1: Expand Audit Category And Action Filter

**Files:**
- Modify: `app-main/src/main/java/net/causw/app/main/domain/admin/audit/enums/AdminAuditLogCategory.java`
- Modify: `app-main/src/main/java/net/causw/app/main/domain/admin/audit/service/AdminAuditLogService.java`
- Test: `app-main/src/test/java/net/causw/app/main/domain/admin/audit/service/AdminAuditLogServiceTest.java`

- [ ] **Step 1: Write failing service tests**

Add tests proving `LOCKER` accepts `ASSIGN`, `EXTEND`, `RELEASE`, `ENABLE`, `DISABLE`, `RELEASE_EXPIRED`, and `ACADEMIC` accepts `ADMISSION_ACCEPT`, `ADMISSION_REJECT`, `ACADEMIC_RECORD_ACCEPT`, `ACADEMIC_RECORD_REJECT`. Also add one cross-category rejection test.

- [ ] **Step 2: Run RED**

Run:
```bash
./gradlew :app-main:test --tests "net.causw.app.main.domain.admin.audit.service.AdminAuditLogServiceTest"
```
Expected: compilation/test failure because categories/actions are not yet supported.

- [ ] **Step 3: Implement category/action support**

Add `LOCKER` and `ACADEMIC` to `AdminAuditLogCategory`, then replace the USER-only condition with category-specific allowed action sets.

- [ ] **Step 4: Run GREEN**

Run the same test class and confirm it passes.

### Task 2: Record Locker Admin Audit Logs

**Files:**
- Create: `app-main/src/main/java/net/causw/app/main/domain/asset/locker/service/v2/implementation/LockerAdminAuditLogWriter.java`
- Modify: `app-main/src/main/java/net/causw/app/main/domain/asset/locker/service/v2/LockerAdminService.java`
- Test: `app-main/src/test/java/net/causw/app/main/domain/asset/locker/service/v2/LockerAdminServiceTest.java`

- [ ] **Step 1: Write failing tests**

Update successful locker admin action tests to verify the domain audit writer is called for `ASSIGN`, `EXTEND`, `RELEASE`, `ENABLE`, `DISABLE`, and `RELEASE_EXPIRED`.

- [ ] **Step 2: Run RED**

Run:
```bash
./gradlew :app-main:test --tests "net.causw.app.main.domain.asset.locker.service.v2.LockerAdminServiceTest"
```
Expected: compilation/test failure because `LockerAdminAuditLogWriter` is not injected or called.

- [ ] **Step 3: Implement locker audit writer and service calls**

Create `LockerAdminAuditLogWriter` that builds `AdminAuditLogCreateCommand` with category `LOCKER`, target type `LOCKER`, actor snapshots from admin, target snapshots from locker user when present, and metadata including locker id/number/location and relevant expire dates. Inject it into `LockerAdminService` and call it only after successful mutation calls.

- [ ] **Step 4: Run GREEN**

Run the same locker service test class and confirm it passes.

### Task 3: Record Admission And Academic Record Admin Audit Logs

**Files:**
- Create: `app-main/src/main/java/net/causw/app/main/domain/user/account/service/implementation/AdmissionAdminAuditLogWriter.java`
- Create: `app-main/src/main/java/net/causw/app/main/domain/user/academic/service/implementation/AcademicRecordAdminAuditLogWriter.java`
- Modify: `app-main/src/main/java/net/causw/app/main/domain/user/account/service/AdmissionAdminService.java`
- Modify: `app-main/src/main/java/net/causw/app/main/domain/user/academic/service/AcademicRecordAdminService.java`
- Test: `app-main/src/test/java/net/causw/app/main/domain/user/account/service/AdmissionAdminServiceTest.java`
- Test: `app-main/src/test/java/net/causw/app/main/domain/user/academic/service/AcademicRecordAdminServiceTest.java`

- [ ] **Step 1: Write failing tests**

Verify successful admission approval/rejection calls `ADMISSION_ACCEPT` and `ADMISSION_REJECT`. Verify successful academic record change approval/rejection calls `ACADEMIC_RECORD_ACCEPT` and `ACADEMIC_RECORD_REJECT`. Failure paths must not write audit logs.

- [ ] **Step 2: Run RED**

Run:
```bash
./gradlew :app-main:test --tests "net.causw.app.main.domain.user.account.service.AdmissionAdminServiceTest" --tests "net.causw.app.main.domain.user.academic.service.AcademicRecordAdminServiceTest"
```
Expected: compilation/test failure because audit writers are not injected or called.

- [ ] **Step 3: Implement admission and academic audit writers**

Both writers should delegate to `AdminAuditLogWriter`, use category `ACADEMIC`, use target type `USER`, snapshot admin/target user identifiers, and include metadata for requested academic status, student id, admission year, department, graduation year, application id, and reject reason where applicable.

- [ ] **Step 4: Run GREEN**

Run the same two service test classes and confirm they pass.

### Task 4: Final Verification

**Files:**
- All modified Java files.

- [ ] **Step 1: Run focused audit and domain tests**

Run:
```bash
./gradlew :app-main:test --tests "net.causw.app.main.domain.admin.audit.service.AdminAuditLogServiceTest" --tests "net.causw.app.main.domain.asset.locker.service.v2.LockerAdminServiceTest" --tests "net.causw.app.main.domain.user.account.service.AdmissionAdminServiceTest" --tests "net.causw.app.main.domain.user.academic.service.AcademicRecordAdminServiceTest"
```

- [ ] **Step 2: Run formatting check**

Run:
```bash
./gradlew :app-main:spotlessCheck
```

- [ ] **Step 3: Review diff**

Run:
```bash
git diff --stat
git diff --check
```

- [ ] **Step 4: Report**

Summarize changed files, tests run, and note no DB/Flyway migration is needed because categories/actions are stored as enum/string in existing audit table.

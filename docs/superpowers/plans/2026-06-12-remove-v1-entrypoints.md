# Remove v1 API Entrypoints Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [x]`) syntax for tracking.

**Goal:** Remove externally reachable `/api/v1/**` endpoints while preserving internal services that are still used by v2, batch, export, crawler, and shared flows.

**Architecture:** Delete v1 controller registrations and remove the v1-only web infrastructure that existed to support those routes. Keep internal `service.v1` classes unless compile-time reference checks prove they became unreachable and are only part of the deleted entrypoint surface.

**Tech Stack:** Java 17, Spring Boot, Spring Security, Spring MVC, Springdoc OpenAPI, Gradle.

---

## File Structure

- Delete: all 21 controller files under `app-main/src/main/java/net/causw/app/main/domain/**/api/v1/controller`.
- Modify: `app-main/src/main/java/net/causw/app/main/core/security/WebSecurityConfig.java` to remove the v1 security chain and v1 CORS source.
- Delete if unused: `app-main/src/main/java/net/causw/app/main/core/security/SecurityEndpoints.java`.
- Keep unless unused after compile: `app-main/src/main/java/net/causw/app/main/core/security/RequestAuthorizationBinder.java`, `app-main/src/main/java/net/causw/app/main/core/security/CustomAuthorizationManager.java`.
- Modify: `app-main/src/main/java/net/causw/app/main/core/security/CustomAuthenticationEntryPoint.java` so it only writes the v2 `ApiResponse` body.
- Modify: `app-main/src/main/java/net/causw/app/main/core/config/swagger/SwaggerConfig.java` to remove `v1` and `admin-v1` groups.
- Modify: `app-main/src/main/java/net/causw/app/main/core/filter/RequestLoggingFilter.java` to log `/api/v2` requests only.
- Modify: `app-main/src/main/java/net/causw/app/main/core/datasourceProxy/ApiQueryLoggingAspect.java` to profile `/api/v2` requests only.
- Delete if unused: `app-main/src/main/java/net/causw/app/main/shared/exception/GlobalV1ExceptionHandler.java`.
- Delete if unused: `app-main/src/main/java/net/causw/app/main/core/global/annotation/V1Api.java`.
- Delete: `app-main/src/test/java/net/causw/app/main/infrastructure/security/SecurityEndpointsTest.java`.
- Modify: `app-main/src/test/java/net/causw/app/main/infrastructure/security/WebSecurityConfigTest.java` to keep v2 assertions and remove v1 endpoint-list assertions/imports.

## Task 1: Remove v1 Security Endpoint Tests

**Files:**
- Delete: `app-main/src/test/java/net/causw/app/main/infrastructure/security/SecurityEndpointsTest.java`
- Modify: `app-main/src/test/java/net/causw/app/main/infrastructure/security/WebSecurityConfigTest.java`

- [x] **Step 1: Delete the v1 endpoint-list test**

Delete `app-main/src/test/java/net/causw/app/main/infrastructure/security/SecurityEndpointsTest.java`. This test validates `SecurityEndpoints`, which exists only for the v1 security chain.

- [x] **Step 2: Reduce `WebSecurityConfigTest` to v2 security behavior**

In `WebSecurityConfigTest.java`, remove these static imports:

```java
import static net.causw.app.main.core.security.SecurityEndpoints.SecurityEndpoint;
import static org.assertj.core.api.Assertions.assertThat;
```

Remove imports that support the v1 parameterized endpoint tests:

```java
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;

import net.causw.app.main.core.security.CustomAuthorizationManager;
import net.causw.app.main.core.security.SecurityEndpoints;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.domain.user.auth.service.v1.SecurityService;
import net.causw.app.main.util.WithMockCustomUser;
```

Change the `@Import` annotation from:

```java
@Import({CustomAuthorizationManager.class, SecurityService.class, WebSecurityConfig.class,
	CustomAuthenticationEntryPoint.class})
```

to:

```java
@Import({WebSecurityConfig.class, CustomAuthenticationEntryPoint.class})
```

Remove the full `AuthorizeHttpRequestsTest` nested class. Keep `V2ApiSecurityTest`, including its `/api/v2/auth/**`, `/api/v2/admin/**`, `/api/v2/terms`, and `/api/v2/users/password-change` tests.

- [x] **Step 3: Run the focused security test and confirm the expected failure**

Run:

```bash
./gradlew :app-main:test --tests net.causw.app.main.infrastructure.security.WebSecurityConfigTest
```

Expected before implementation is either compile failure due to removed imports not yet complete, or test failure due to still-existing v1 security chain behavior. Do not proceed until the remaining test source contains only v2 security assertions.

## Task 2: Remove v1 Web Security Infrastructure

**Files:**
- Modify: `app-main/src/main/java/net/causw/app/main/core/security/WebSecurityConfig.java`
- Delete: `app-main/src/main/java/net/causw/app/main/core/security/SecurityEndpoints.java`
- Delete if unused after compile: `app-main/src/main/java/net/causw/app/main/core/security/RequestAuthorizationBinder.java`
- Keep unless unused after compile: `app-main/src/main/java/net/causw/app/main/core/security/CustomAuthorizationManager.java`

- [x] **Step 1: Remove v1 constructor dependency**

In `WebSecurityConfig.java`, remove this field:

```java
private final CustomAuthorizationManager authorizationManager;
```

- [x] **Step 2: Remove `securityFilterChainV1`**

Delete the entire `securityFilterChainV1(HttpSecurity http)` bean method, including its `@Bean` and `@Order(2)` annotations.

- [x] **Step 3: Remove v1 CORS source**

Delete the entire `corsConfigurationSourceV1()` bean method.

- [x] **Step 4: Delete `SecurityEndpoints`**

Delete `app-main/src/main/java/net/causw/app/main/core/security/SecurityEndpoints.java`.

- [x] **Step 5: Check whether `RequestAuthorizationBinder` is now unused**

Run:

```bash
rg -n "RequestAuthorizationBinder" app-main/src/main/java app-main/src/test/java
```

Expected after `securityFilterChainV1` removal: only `RequestAuthorizationBinder.java` itself appears. If so, delete `app-main/src/main/java/net/causw/app/main/core/security/RequestAuthorizationBinder.java`.

- [x] **Step 6: Check whether `CustomAuthorizationManager` is now unused**

Run:

```bash
rg -n "CustomAuthorizationManager" app-main/src/main/java app-main/src/test/java
```

Expected after test cleanup and `securityFilterChainV1` removal: only `CustomAuthorizationManager.java` itself appears. If so, delete `app-main/src/main/java/net/causw/app/main/core/security/CustomAuthorizationManager.java`. If other production code still references it, keep it.

## Task 3: Remove v1 Error Response and API Documentation Paths

**Files:**
- Modify: `app-main/src/main/java/net/causw/app/main/core/security/CustomAuthenticationEntryPoint.java`
- Modify: `app-main/src/main/java/net/causw/app/main/core/config/swagger/SwaggerConfig.java`
- Modify: `app-main/src/main/java/net/causw/app/main/core/filter/RequestLoggingFilter.java`
- Modify: `app-main/src/main/java/net/causw/app/main/core/datasourceProxy/ApiQueryLoggingAspect.java`

- [x] **Step 1: Make `CustomAuthenticationEntryPoint` v2-only**

Remove these imports from `CustomAuthenticationEntryPoint.java`:

```java
import java.time.LocalDateTime;

import net.causw.app.main.shared.exception.BaseResponseCode;
```

Replace the branching response logic:

```java
String requestPath = request.getRequestURI();
boolean isV2 = isV2Path(requestPath);

if (isV2) {
	AuthErrorCode authErrorCode = mapToAuthErrorCode(errorCode);
	setV2Response(response, authErrorCode);
} else {
	setV1Response(response, errorCode, message);
}
```

with:

```java
AuthErrorCode authErrorCode = mapToAuthErrorCode(errorCode);
setV2Response(response, authErrorCode);
```

Remove the `isV2Path` method and the `setV1Response` method.

Change the `setV2Response` method signature from:

```java
private void setV2Response(
	HttpServletResponse response,
	BaseResponseCode authErrorCode) throws IOException {
```

to:

```java
private void setV2Response(
	HttpServletResponse response,
	AuthErrorCode authErrorCode) throws IOException {
```

- [x] **Step 2: Remove v1 Swagger groups**

In `SwaggerConfig.java`, delete the entire `v1Api()` bean method and the entire `v1AdminApi()` bean method. Keep `allApi()`, `v2Api()`, `v2AdminApi()`, `customOpenAPI()`, and `refreshBearerSwaggerIndexTransformer()`.

- [x] **Step 3: Update request logging to v2 only**

In `RequestLoggingFilter.java`, update the class comment first line from:

```java
 * /api/v1, /api/v2 경로의 모든 HTTP 요청을 가로채 MDC(Mapped Diagnostic Context)에
```

to:

```java
 * /api/v2 경로의 모든 HTTP 요청을 가로채 MDC(Mapped Diagnostic Context)에
```

Update the `shouldNotFilter` comment from:

```java
 * - /api/v1, /api/v2 외 경로: 정적 리소스 등 API 외 요청
```

to:

```java
 * - /api/v2 외 경로: 정적 리소스 등 API 외 요청
```

Replace:

```java
return !(uri.toLowerCase().startsWith("/api/v1") || uri.toLowerCase().startsWith("/api/v2"));
```

with:

```java
return !uri.toLowerCase().startsWith("/api/v2");
```

- [x] **Step 4: Update query logging to v2 only**

In `ApiQueryLoggingAspect.java`, replace:

```java
if (!path.startsWith("/api/v1") && !path.startsWith("/api/v2")) {
	return joinPoint.proceed();
}
```

with:

```java
if (!path.startsWith("/api/v2")) {
	return joinPoint.proceed();
}
```

## Task 4: Delete v1 Controllers and v1 Controller Advice

**Files:**
- Delete: `app-main/src/main/java/net/causw/app/main/domain/asset/file/api/v1/controller/StorageController.java`
- Delete: `app-main/src/main/java/net/causw/app/main/domain/asset/locker/api/v1/controller/LockerV1Controller.java`
- Delete: `app-main/src/main/java/net/causw/app/main/domain/campus/circle/api/v1/controller/CircleController.java`
- Delete: `app-main/src/main/java/net/causw/app/main/domain/campus/event/api/v1/controller/EventController.java`
- Delete: `app-main/src/main/java/net/causw/app/main/domain/campus/schedule/api/v1/controller/CalendarController.java`
- Delete: `app-main/src/main/java/net/causw/app/main/domain/campus/semester/api/v1/controller/SemesterController.java`
- Delete: `app-main/src/main/java/net/causw/app/main/domain/community/board/api/v1/controller/BoardV1Controller.java`
- Delete: `app-main/src/main/java/net/causw/app/main/domain/community/ceremony/api/v1/controller/CeremonyV1Controller.java`
- Delete: `app-main/src/main/java/net/causw/app/main/domain/community/comment/api/v1/controller/ChildCommentV1Controller.java`
- Delete: `app-main/src/main/java/net/causw/app/main/domain/community/comment/api/v1/controller/CommentV1Controller.java`
- Delete: `app-main/src/main/java/net/causw/app/main/domain/community/form/api/v1/controller/FormController.java`
- Delete: `app-main/src/main/java/net/causw/app/main/domain/community/post/api/v1/controller/PostV1Controller.java`
- Delete: `app-main/src/main/java/net/causw/app/main/domain/community/report/api/v1/controller/ReportController.java`
- Delete: `app-main/src/main/java/net/causw/app/main/domain/community/vote/api/v1/controller/VoteController.java`
- Delete: `app-main/src/main/java/net/causw/app/main/domain/etc/api/v1/controller/CommonController.java`
- Delete: `app-main/src/main/java/net/causw/app/main/domain/finance/usercouncilfee/api/v1/controller/UserCouncilFeeController.java`
- Delete: `app-main/src/main/java/net/causw/app/main/domain/notification/notification/api/v1/controller/NotificationLogV1Controller.java`
- Delete: `app-main/src/main/java/net/causw/app/main/domain/user/academic/api/v1/controller/UserAcademicRecordApplicationController.java`
- Delete: `app-main/src/main/java/net/causw/app/main/domain/user/account/api/v1/controller/UserBlockV1Controller.java`
- Delete: `app-main/src/main/java/net/causw/app/main/domain/user/account/api/v1/controller/UserController.java`
- Delete: `app-main/src/main/java/net/causw/app/main/domain/user/account/api/v1/controller/UserInfoV1Controller.java`
- Delete if unused: `app-main/src/main/java/net/causw/app/main/shared/exception/GlobalV1ExceptionHandler.java`
- Delete if unused: `app-main/src/main/java/net/causw/app/main/core/global/annotation/V1Api.java`

- [x] **Step 1: Delete the 21 v1 controller files**

Delete exactly the controller files listed in this task's file list. Do not delete sibling DTO, mapper, or service files in this step.

- [x] **Step 2: Confirm there are no remaining v1 controller mappings**

Run:

```bash
rg -n '@RequestMapping\\("/api/v1|@GetMapping\\("/api/v1|@PostMapping\\("/api/v1|@PutMapping\\("/api/v1|@DeleteMapping\\("/api/v1' app-main/src/main/java
```

Expected: no production controller mapping output for `/api/v1`.

- [x] **Step 3: Check `V1Api` usage**

Run:

```bash
rg -n "V1Api|GlobalV1ExceptionHandler" app-main/src/main/java app-main/src/test/java
```

Expected after controller deletion: only `V1Api.java` and `GlobalV1ExceptionHandler.java` appear. If so, delete both files.

## Task 5: Compile-Guided Cleanup of API v1 DTOs and Mappers

**Files:**
- Delete only files proven unused by compiler errors and reference search under `app-main/src/main/java/net/causw/app/main/domain/**/api/v1/dto`
- Delete only files proven unused by compiler errors and reference search under `app-main/src/main/java/net/causw/app/main/domain/**/api/v1/mapper`
- Keep v1 DTOs/mappers referenced by internal services, export services, seeders, or tests that remain in scope

- [x] **Step 1: Run compile after controller and infrastructure removal**

Run:

```bash
./gradlew :app-main:compileJava
```

Expected: failures may identify imports from deleted controllers, `V1Api`, `SecurityEndpoints`, `RequestAuthorizationBinder`, or unused v1 API DTOs/mappers that are now disconnected.

- [x] **Step 2: For each compile error, verify references before deleting more code**

For every failed class name, run:

```bash
rg -n "ClassName" app-main/src/main/java app-main/src/test/java
```

Replace `ClassName` with the exact simple class name from the compiler error. Delete a DTO or mapper only when the only references are from deleted files or from the file itself. Keep it when active services, batch jobs, export services, seeders, or tests still import it.

- [x] **Step 3: Repeat compile until Java source compiles**

Run:

```bash
./gradlew :app-main:compileJava
```

Expected final result: `BUILD SUCCESSFUL`.

## Task 6: Verification and Final Search

**Files:**
- Verify all touched files

- [x] **Step 1: Run focused security tests**

Run:

```bash
./gradlew :app-main:test --tests net.causw.app.main.infrastructure.security.WebSecurityConfigTest
```

Expected: `BUILD SUCCESSFUL`.

- [x] **Step 2: Search for remaining public v1 paths**

Run:

```bash
rg -n '"/api/v1|/api/v1|api/v1' app-main/src/main/java app-main/src/test/java app-main/src/main/resources docs
```

Expected: no active route, security, Swagger, logging, or test references. Historical database migrations or design docs may mention v1 and do not require deletion.

- [x] **Step 3: Search for remaining v1 controllers**

Run:

```bash
find app-main/src/main/java/net/causw/app/main/domain -path '*/api/v1/controller/*.java' -print
```

Expected: no output.

- [x] **Step 4: Review git diff**

Run:

```bash
git diff --stat
git diff -- app-main/src/main/java/net/causw/app/main/core/security/WebSecurityConfig.java app-main/src/main/java/net/causw/app/main/core/security/CustomAuthenticationEntryPoint.java app-main/src/main/java/net/causw/app/main/core/config/swagger/SwaggerConfig.java app-main/src/main/java/net/causw/app/main/core/filter/RequestLoggingFilter.java app-main/src/main/java/net/causw/app/main/core/datasourceProxy/ApiQueryLoggingAspect.java app-main/src/test/java/net/causw/app/main/infrastructure/security/WebSecurityConfigTest.java
```

Expected: changes match the design and do not alter v2 controller behavior.

- [x] **Step 5: Commit implementation**

Run:

```bash
git add app-main/src/main/java app-main/src/test/java docs/superpowers/plans/2026-06-12-remove-v1-entrypoints.md
git commit -m "refactor: remove v1 api entrypoints"
```

Expected: implementation commit is created after compile and focused tests pass.

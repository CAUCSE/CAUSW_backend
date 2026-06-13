# Admin Audit Log Page Design

## Goal

Create a separate admin audit log page/API that lets administrators review important administrator actions. The first implementation will expose the existing user management audit logs, while keeping the external API contract compatible with a later migration to a common `AdminAuditLog` table.

## Scope

### In Scope For The First Version

- Add an admin-only audit log list API.
- Return existing `UserAdminActionLog` records through a common audit log response shape.
- Support page-level filters:
  - date/time range
  - audit category
  - action type
  - administrator or target user email keyword
  - pagination
- Include enough detail for the page table and row detail view:
  - actor information
  - target information
  - action category/type
  - summary
  - metadata for before/after state, before/after roles, and reason
  - created time
- Design the API so the later common audit log table can replace the repository implementation without changing the frontend contract.

### Out Of Scope For The First Version

- Capturing new audit events for admissions, reports, boards, posts, comments, or schedule management.
- Creating and backfilling a common `tb_admin_audit_log` table.
- CSV or Excel export.
- Audit log detail API by ID, unless the frontend later needs a dedicated detail route.
- Retention, archival, or deletion policy.

## Recommended Approach

Use a B-to-C migration path:

1. Start with a common audit log API contract.
2. Adapt existing `tb_user_admin_action_log` rows into that common response.
3. Later introduce `tb_admin_audit_log` and backfill old user action logs.
4. Switch the query repository from the legacy table to the common table while keeping the same API request/response.

This avoids a large schema migration now while preventing the first page from being coupled to only user account actions.

## API Design

### Endpoint

`GET /api/v2/admin/audit-logs`

Authorization:

- `@PreAuthorize("@security.hasRole(@Role.ADMIN)")`

Response wrapper:

- `ApiResponse<PageResponse<AdminAuditLogResponse>>`

### Request Query Parameters

Create `AdminAuditLogRequest`.

Fields:

- `LocalDateTime from`
  - Optional.
  - Inclusive lower bound for `createdAt`.
- `LocalDateTime to`
  - Optional.
  - Inclusive upper bound for `createdAt`.
- `AdminAuditLogCategory category`
  - Optional.
  - First version supports `USER`.
  - If omitted, return all categories available in the current data source. In the first version this is equivalent to `USER`.
- `String actionType`
  - Optional.
  - First version accepts `DROP`, `RESTORE`, `ROLE_CHANGE`.
  - Keep this as `String` at the API/service boundary so future categories can have different action types without forcing one enum across all domains.
- `String keyword`
  - Optional.
  - Searches `adminUserEmail` and `targetUserEmail`.
- `Pageable pageable`
  - Use `@PageableDefault(page = 0, size = 10)`.
  - Default sort should be newest first by `createdAt`.

Validation:

- If both `from` and `to` exist, `from` must not be after `to`.
- Blank `keyword` should be treated as no filter.
- Unknown `category` should fail through enum binding.
- Unsupported `actionType` for the current source should return an empty page, not a server error.

## Response Design

Create `AdminAuditLogResponse`.

Fields:

- `String id`
- `AdminAuditLogCategory category`
- `String actionType`
- `String actionDescription`
- `AuditActorResponse actor`
- `AuditTargetResponse target`
- `String summary`
- `Map<String, Object> metadata`
- `LocalDateTime createdAt`

`AuditActorResponse`:

- `String userId`
- `String email`

`AuditTargetResponse`:

- `String type`
  - First version uses `USER`.
- `String id`
- `String email`

First version `metadata` keys for user actions:

- `beforeState`
- `afterState`
- `beforeRoles`
- `afterRoles`
- `reason`

Example summary rules:

- `DROP`: `{adminEmail} dropped user {targetEmail}`
- `RESTORE`: `{adminEmail} restored user {targetEmail}`
- `ROLE_CHANGE`: `{adminEmail} changed roles for user {targetEmail}`

The frontend can render compact table columns from top-level fields and open row detail using `metadata`.

## Backend Components

### Controller

Package:

- `net.causw.app.main.domain.admin.audit.api.v2.controller`

Class:

- `AdminAuditLogController`

Responsibility:

- Bind request parameters.
- Enforce admin authorization.
- Call `AdminAuditLogService`.
- Wrap result with `ApiResponse` and `PageResponse`.

### API DTOs

Package:

- `net.causw.app.main.domain.admin.audit.api.v2.dto`

Classes:

- `AdminAuditLogRequest`
- `AdminAuditLogResponse`
- `AuditActorResponse`
- `AuditTargetResponse`

### Service

Package:

- `net.causw.app.main.domain.admin.audit.service`

Class:

- `AdminAuditLogService`

Responsibility:

- Validate date range.
- Normalize blank keyword.
- Call the current query adapter.
- Return page of service results or API-ready DTOs, following existing project style.

### Query Adapter

First version package:

- `net.causw.app.main.domain.admin.audit.repository`

Class:

- `AdminAuditLogQueryRepository`

Responsibility:

- Query `UserAdminActionLog` with QueryDSL.
- Apply optional conditions.
- Project rows into common audit log query results.
- Return newest first.

The adapter name should avoid `UserAdminActionLogQueryRepository` because its role is to serve the common audit log API. Internally it may still use `QUserAdminActionLog`.

### Mapper

Package:

- `net.causw.app.main.domain.admin.audit.api.v2.mapper`

Class:

- `AdminAuditLogMapper`

Responsibility:

- Convert query/service result into `AdminAuditLogResponse`.
- Build `summary`.
- Build user-action `metadata`.

## Data Flow

1. Admin opens the audit log page.
2. Frontend calls `GET /api/v2/admin/audit-logs`.
3. Controller binds filters and pageable.
4. Service validates and normalizes request.
5. Query repository reads `tb_user_admin_action_log`.
6. Mapper converts each row into common audit log response.
7. API returns `PageResponse`.

## Future Common Table Design

When expanding from B to C, add `tb_admin_audit_log`.

Suggested columns:

- `id`
- `created_at`
- `updated_at`
- `category`
- `action_type`
- `actor_user_id`
- `actor_email`
- `target_type`
- `target_id`
- `target_email`
- `summary`
- `metadata_json`

Suggested indexes:

- `(created_at)`
- `(category, action_type, created_at)`
- `(actor_email, created_at)`
- `(target_email, created_at)`

Migration steps:

1. Create `tb_admin_audit_log`.
2. Write new admin events to the common table.
3. Backfill existing `tb_user_admin_action_log` into `tb_admin_audit_log`.
4. Switch `AdminAuditLogQueryRepository` to query the common table.
5. Keep the existing endpoint and response contract unchanged.
6. Decide whether to keep the legacy table as a domain-specific history table or deprecate it after verification.

## Error Handling

- Invalid date range should return a bad request using the project's standard exception pattern.
- Authorization failures should use existing security behavior.
- Empty filters should return an empty page or available data, not an error.
- If `category` is not `USER` before the common table exists, return an empty page unless the enum value is unsupported by binding.

## Testing Plan

### Unit Tests

- Service rejects `from > to`.
- Blank keyword is treated as null.
- Mapper creates expected summary and metadata for `DROP`.
- Mapper creates expected summary and metadata for `RESTORE`.
- Mapper creates expected summary and metadata for `ROLE_CHANGE`.

### Repository Tests

- Filters by date range.
- Filters by action type.
- Searches administrator email.
- Searches target user email.
- Sorts by newest first.
- Returns only `USER` category in the first implementation.

### Controller Tests

- Admin can list audit logs.
- Non-admin cannot list audit logs.
- Response uses `ApiResponse<PageResponse<AdminAuditLogResponse>>`.

## Open Decisions

- Whether `keyword` should also search user IDs in addition to emails.
- Whether a row detail endpoint is needed after the frontend design is finalized.
- Whether export is required and who can access it.
- Whether the common audit table should be introduced immediately after the MVP or only when the second audit category is added.

## Acceptance Criteria

- Admin can request `GET /api/v2/admin/audit-logs`.
- The API returns existing user admin action logs in a common audit log response shape.
- Filters work for period, category, action type, and administrator/target email keyword.
- Results are paged and sorted newest first.
- The response contract does not need to change when moving to `tb_admin_audit_log`.

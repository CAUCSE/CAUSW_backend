# V1 Orphan Removal Design

## Goal

Remove v1 logic that became orphaned after v1 API endpoint deletion, while preserving logic still used by v2 runtime paths by moving it to v2 or non-versioned packages.

## Current State

- v1 controller entrypoints are already removed.
- `api/v1`, `service/v1`, and `repository/v1` packages still contain legacy DTOs, mappers, services, and repositories.
- v2 and non-versioned runtime code still directly import a small set of v1 types.
- Some entities depend on v1 API request DTOs, which violates the desired dependency direction.

## Approach

Use static references and compilation as the safety boundary.

1. Remove direct v2 and non-versioned runtime imports from v1 packages.
2. Move still-used DTOs/utilities to v2 or non-versioned packages.
3. Replace entity methods that accept v1 API DTOs with service-level DTO or primitive/value parameters.
4. Delete v1 packages after references are removed.
5. Run the smallest verification command that proves compile safety, then broader tests if needed.

## Boundaries

- Do not reintroduce v1 API endpoints.
- Do not rewrite unrelated domain behavior.
- Do not change schema or Flyway migrations.
- Do not delete runtime services only because they are under `service/v1` if current v2/non-versioned code still needs their behavior; move them first.

## Expected Refactoring Targets

- `PasswordGenerator` should move out of `user/account/service/v1` because v2 auth uses it.
- `UserFcmTokenResponseDto` should move to a v2 response DTO and callers should depend on that.
- `User`, `Form`, and `FormQuestion` should not import v1 API DTOs.
- notification, block, and user notification code should stop importing v1 packages.
- `UserInfoQueryV1Repository` naming and DTO dependency should be corrected if still used by v2/admin logic.

## Testing

- Run `./gradlew :app-main:compileJava` after structural deletion.
- Run focused tests for touched domains when compile succeeds.
- Run `./gradlew :app-main:spotlessCheck` before final completion.

## Commit Strategy

Commit in Korean at meaningful checkpoints:

1. Document design and implementation plan.
2. Move v2-used v1 DTO/util dependencies.
3. Remove entity dependencies on v1 API DTOs.
4. Move or replace shared runtime services still under v1.
5. Delete orphaned v1 packages.
6. Apply formatting and verification fixes if necessary.

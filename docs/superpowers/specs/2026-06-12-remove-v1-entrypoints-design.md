# Remove v1 API Entrypoints Design

## Goal

Remove externally reachable v1 API entrypoints for this release while keeping internal services that are still needed by v2, batch jobs, export jobs, crawlers, or shared application flows.

## Scope

The change targets HTTP routing and v1-specific web infrastructure:

- Remove `/api/v1/**` controller entrypoints.
- Remove v1-specific Spring Security chain, CORS source, and endpoint authorization lists if they are no longer used.
- Remove v1 Swagger groups.
- Update request/query logging filters so API matching reflects the remaining v2 API surface.
- Remove v1 exception handler and marker annotation if controller removal makes them unused.
- Delete v1 API DTOs and mappers only when they become unused after controller removal.

The change does not target all packages named `service.v1`. Some of those classes are internal implementation names, not public API entrypoints. They remain until a separate refactor replaces or renames their active consumers.

## Architecture

The application should expose API traffic through `/api/v2/**` plus existing non-versioned infrastructure routes such as health checks and OAuth routes. Spring Security should keep the v2 filter chain and OAuth login support, and should not register a catch-all v1-oriented chain for application API traffic.

Swagger should document the remaining API groups only. The `all` group may continue matching `/api/**`, but v1-specific groups should be removed so removed routes are not advertised.

## Components

- Controllers: delete or otherwise remove Spring registration for classes under `domain/**/api/v1/controller`.
- Security: remove `securityFilterChainV1`, `corsConfigurationSourceV1`, and `SecurityEndpoints` usage when no remaining code requires them.
- Swagger: remove `v1` and `admin-v1` grouped API beans and comments.
- Logging: replace `/api/v1` or `/api/v1|/api/v2` checks with the intended remaining API paths.
- Exception handling: remove `GlobalV1ExceptionHandler` and `V1Api` if unused.
- Tests: remove or update tests that only verify deleted v1 entrypoints. Keep tests for internal services that remain active.

## Data Flow

Incoming API requests should resolve to v2 controllers or be rejected by normal Spring routing/security behavior. There should be no controller mappings under `/api/v1/**`.

Internal services may still call classes in `service.v1` packages. Those calls are not public v1 entrypoints and should continue compiling unless separately replaced.

## Error Handling

v2 authentication failures should continue using the existing v2 response behavior. The v1 error response path should be removed only after no registered endpoint or advice depends on it.

## Testing

Primary verification:

- `./gradlew :app-main:compileJava`

Secondary verification when feasible:

- Run focused tests for security, logging, and any touched internal services.
- Run broader app tests if compile changes show cross-module impact.

## Non-Goals

- Rename or fully delete all `service.v1` packages.
- Rewrite v1-only business flows into v2 replacements.
- Modify database migrations, including historical files whose names mention v1.
- Change v2 API behavior beyond removing v1 routing dependencies.

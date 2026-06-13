# 관리자 감사 로그 페이지 설계

## 목표

관리자가 주요 관리자 활동을 확인할 수 있는 별도 감사 로그 페이지와 조회 API를 만든다. 1차 구현에서는 이미 저장 중인 사용자 관리 감사 로그를 노출하고, 이후 공통 `AdminAuditLog` 테이블로 전환하더라도 외부 API 계약은 유지되도록 설계한다.

## 범위

### 1차 구현 범위

- 관리자 전용 감사 로그 목록 조회 API를 추가한다.
- 기존 `UserAdminActionLog` 레코드를 공통 감사 로그 응답 형태로 반환한다.
- 페이지 조회 필터를 지원한다.
  - 기간
  - 감사 로그 카테고리
  - 액션 타입
  - 관리자 또는 대상 사용자 이메일 키워드
  - 페이지네이션
- 목록 테이블과 행 상세 영역에서 사용할 수 있는 정보를 포함한다.
  - 수행자 정보
  - 대상 정보
  - 액션 카테고리와 타입
  - 요약 문구
  - 상태 변경 전후, 역할 변경 전후, 사유를 담은 메타데이터
  - 생성 시각
- 이후 공통 감사 로그 테이블을 도입할 때 프론트엔드 API 계약을 바꾸지 않고 repository 구현만 교체할 수 있게 한다.

### 1차 구현 제외 범위

- 재학인증, 신고, 게시판, 게시글, 댓글, 일정 관리 등 다른 관리자 활동의 신규 감사 이벤트 저장.
- 공통 `tb_admin_audit_log` 테이블 생성과 백필.
- CSV 또는 Excel 내보내기.
- 프론트엔드에서 별도 상세 라우트가 필요하다고 확정되기 전까지는 ID 기반 상세 조회 API.
- 보관 기간, 아카이빙, 삭제 정책.

## 권장 접근

단계적으로 확장 가능한 구조를 사용한다.

1. 공통 감사 로그 API 계약을 먼저 정의한다.
2. 기존 `tb_user_admin_action_log` 행을 공통 응답 형태로 변환해 반환한다.
3. 이후 `tb_admin_audit_log`를 도입하고 기존 사용자 액션 로그를 백필한다.
4. API 요청/응답 계약은 유지한 채 query repository만 기존 테이블 조회에서 공통 테이블 조회로 교체한다.

이 방식은 지금 큰 스키마 마이그레이션을 피하면서도 첫 페이지가 사용자 계정 액션에만 강하게 결합되는 문제를 줄인다.

## API 설계

### 엔드포인트

`GET /api/v2/admin/audit-logs`

권한:

- `@PreAuthorize("@security.hasRole(@Role.ADMIN)")`

응답 래퍼:

- `ApiResponse<PageResponse<AdminAuditLogResponse>>`

### 요청 쿼리 파라미터

`AdminAuditLogRequest`를 만든다.

필드:

- `LocalDateTime from`
  - 선택값.
  - `createdAt`의 포함 하한.
- `LocalDateTime to`
  - 선택값.
  - `createdAt`의 포함 상한.
- `AdminAuditLogCategory category`
  - 선택값.
  - 1차 구현에서는 `USER`를 지원한다.
  - 생략하면 현재 데이터 소스에서 제공 가능한 모든 카테고리를 반환한다. 1차 구현에서는 `USER`와 동일하다.
- `String actionType`
  - 선택값.
  - 1차 구현에서는 `DROP`, `RESTORE`, `ROLE_CHANGE`를 허용한다.
  - 이후 카테고리마다 다른 액션 타입을 가질 수 있으므로 API와 service 경계에서는 특정 enum이 아니라 `String`으로 둔다.
- `String keyword`
  - 선택값.
  - `adminUserEmail`, `targetUserEmail`을 검색한다.
- `Pageable pageable`
  - `@PageableDefault(page = 0, size = 10)`을 사용한다.
  - 기본 정렬은 `createdAt` 최신순이다.

검증:

- `from`, `to`가 모두 있으면 `from`은 `to`보다 늦을 수 없다.
- 공백 `keyword`는 필터 없음으로 처리한다.
- 알 수 없는 `category`는 enum binding 단계에서 실패한다.
- 현재 데이터 소스가 지원하지 않는 `actionType`은 서버 오류가 아니라 빈 페이지를 반환한다.

## 응답 설계

`AdminAuditLogResponse`를 만든다.

필드:

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
  - 1차 구현에서는 `USER`를 사용한다.
- `String id`
- `String email`

1차 구현의 사용자 액션 `metadata` key:

- `beforeState`
- `afterState`
- `beforeRoles`
- `afterRoles`
- `reason`

요약 문구 규칙:

- `DROP`: `{adminEmail} dropped user {targetEmail}`
- `RESTORE`: `{adminEmail} restored user {targetEmail}`
- `ROLE_CHANGE`: `{adminEmail} changed roles for user {targetEmail}`

프론트엔드는 최상위 필드로 목록 테이블을 구성하고, 행 상세 영역에서는 `metadata`를 사용한다.

## 백엔드 컴포넌트

### Controller

패키지:

- `net.causw.app.main.domain.admin.audit.api.v2.controller`

클래스:

- `AdminAuditLogController`

책임:

- 요청 파라미터를 바인딩한다.
- 관리자 권한을 강제한다.
- `AdminAuditLogService`를 호출한다.
- 결과를 `ApiResponse`, `PageResponse`로 감싼다.

### API DTO

패키지:

- `net.causw.app.main.domain.admin.audit.api.v2.dto`

클래스:

- `AdminAuditLogRequest`
- `AdminAuditLogResponse`
- `AuditActorResponse`
- `AuditTargetResponse`

### Service

패키지:

- `net.causw.app.main.domain.admin.audit.service`

클래스:

- `AdminAuditLogService`

책임:

- 기간 조건을 검증한다.
- 공백 키워드를 정규화한다.
- 현재 query adapter를 호출한다.
- 기존 프로젝트 스타일에 맞춰 service result 또는 API 응답 DTO로 변환 가능한 Page를 반환한다.

### Query Adapter

1차 구현 패키지:

- `net.causw.app.main.domain.admin.audit.repository`

클래스:

- `AdminAuditLogQueryRepository`

책임:

- QueryDSL로 `UserAdminActionLog`를 조회한다.
- 선택 필터를 적용한다.
- 조회 결과를 공통 감사 로그 query result로 projection한다.
- 최신순으로 반환한다.

클래스명은 `UserAdminActionLogQueryRepository`보다 `AdminAuditLogQueryRepository`를 사용한다. 이 컴포넌트의 역할은 기존 테이블 자체가 아니라 공통 감사 로그 API를 제공하는 것이기 때문이다. 내부 구현에서는 `QUserAdminActionLog`를 사용해도 된다.

### Mapper

패키지:

- `net.causw.app.main.domain.admin.audit.api.v2.mapper`

클래스:

- `AdminAuditLogMapper`

책임:

- query/service 결과를 `AdminAuditLogResponse`로 변환한다.
- `summary`를 만든다.
- 사용자 액션용 `metadata`를 만든다.

## 데이터 흐름

1. 관리자가 감사 로그 페이지를 연다.
2. 프론트엔드가 `GET /api/v2/admin/audit-logs`를 호출한다.
3. Controller가 필터와 pageable을 바인딩한다.
4. Service가 요청을 검증하고 정규화한다.
5. Query repository가 `tb_user_admin_action_log`를 조회한다.
6. Mapper가 각 행을 공통 감사 로그 응답으로 변환한다.
7. API가 `PageResponse`를 반환한다.

## 향후 공통 테이블 설계

여러 관리자 활동 카테고리를 하나의 저장소에서 조회해야 하는 시점에 `tb_admin_audit_log`를 추가한다.

권장 컬럼:

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

권장 인덱스:

- `(created_at)`
- `(category, action_type, created_at)`
- `(actor_email, created_at)`
- `(target_email, created_at)`

마이그레이션 단계:

1. `tb_admin_audit_log`를 생성한다.
2. 새 관리자 이벤트부터 공통 테이블에 저장한다.
3. 기존 `tb_user_admin_action_log` 데이터를 `tb_admin_audit_log`로 백필한다.
4. `AdminAuditLogQueryRepository`가 공통 테이블을 조회하도록 교체한다.
5. 기존 엔드포인트와 응답 계약은 유지한다.
6. 검증 이후 기존 테이블을 도메인별 이력 테이블로 유지할지, deprecated 처리할지 결정한다.

## 오류 처리

- 유효하지 않은 기간 조건은 프로젝트 표준 예외 패턴에 맞춰 bad request로 반환한다.
- 권한 오류는 기존 Spring Security 동작을 따른다.
- 필터 결과가 없으면 오류가 아니라 빈 페이지를 반환한다.
- 공통 테이블 도입 전 `category`가 `USER`가 아니면 빈 페이지를 반환한다. 단, enum binding 자체가 실패하는 값은 binding 오류로 처리한다.

## 테스트 계획

### 단위 테스트

- Service는 `from > to` 조건을 거부한다.
- 공백 keyword는 null 또는 필터 없음으로 처리한다.
- Mapper는 `DROP`에 대해 기대한 summary와 metadata를 만든다.
- Mapper는 `RESTORE`에 대해 기대한 summary와 metadata를 만든다.
- Mapper는 `ROLE_CHANGE`에 대해 기대한 summary와 metadata를 만든다.

### Repository 테스트

- 기간 조건으로 필터링한다.
- 액션 타입으로 필터링한다.
- 관리자 이메일을 검색한다.
- 대상 사용자 이메일을 검색한다.
- 최신순으로 정렬한다.
- 1차 구현에서는 `USER` 카테고리만 반환한다.

### Controller 테스트

- 관리자는 감사 로그 목록을 조회할 수 있다.
- 관리자가 아닌 사용자는 감사 로그 목록을 조회할 수 없다.
- 응답은 `ApiResponse<PageResponse<AdminAuditLogResponse>>` 형태를 사용한다.

## 남은 결정 사항

- `keyword`가 이메일 외에 사용자 ID까지 검색해야 하는지.
- 프론트엔드 설계 확정 후 행 상세 조회 API가 필요한지.
- export가 필요한지, 필요하다면 누가 접근할 수 있는지.
- 공통 감사 테이블을 1차 구현 직후 도입할지, 두 번째 감사 카테고리가 추가될 때 도입할지.

## 인수 조건

- 관리자는 `GET /api/v2/admin/audit-logs`를 호출할 수 있다.
- API는 기존 사용자 관리자 액션 로그를 공통 감사 로그 응답 형태로 반환한다.
- 기간, 카테고리, 액션 타입, 관리자/대상 이메일 키워드 필터가 동작한다.
- 결과는 페이지네이션되며 최신순으로 정렬된다.
- `tb_admin_audit_log`로 전환할 때 응답 계약을 바꾸지 않는다.

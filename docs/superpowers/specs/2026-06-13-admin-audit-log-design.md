# 관리자 감사 로그 페이지 설계

## 목표

관리자가 주요 관리자 활동을 확인할 수 있는 별도 감사 로그 페이지와 조회 API를 만든다. 1차 구현부터 공통 `tb_admin_audit_log` 테이블을 사용하고, 기존 사용자 관리 감사 로그는 공통 테이블로 백필한다. 이후 다른 관리자 활동을 추가하더라도 외부 API 계약과 조회 흐름은 유지되도록 설계한다.

## 범위

### 1차 구현 범위

- 관리자 전용 감사 로그 목록 조회 API를 추가한다.
- 공통 `tb_admin_audit_log` 테이블을 생성한다.
- 기존 `tb_user_admin_action_log` 레코드를 `tb_admin_audit_log`로 백필한다.
- 사용자 관리 액션 로그 저장을 공통 `AdminAuditLog` 저장 흐름으로 전환한다.
- 페이지 조회 필터를 지원한다.
  - 기간
  - 감사 로그 카테고리
  - 액션 타입
  - 수행자 또는 대상 사용자의 이메일, 이름, 학번 키워드
  - 페이지네이션
- 목록 테이블과 행 상세 영역에서 사용할 수 있는 정보를 포함한다.
  - 수행자 스냅샷 정보
  - 대상 스냅샷 정보
  - 액션 카테고리와 타입
  - 요약 문구
  - 상태 변경 전후, 역할 변경 전후, 사유를 담은 메타데이터
  - 생성 시각
- 기존 `tb_user_admin_action_log`는 백필 후 deprecated 테이블로 유지하고, 검증 이후 별도 PR에서 제거 여부를 결정한다.

### 1차 구현 제외 범위

- 재학인증, 신고, 게시판, 게시글, 댓글, 일정 관리 등 다른 관리자 활동의 신규 감사 이벤트 저장.
- CSV 또는 Excel 내보내기.
- 프론트엔드에서 별도 상세 라우트가 필요하다고 확정되기 전까지는 ID 기반 상세 조회 API.
- 보관 기간, 아카이빙, 삭제 정책.
- 기존 `tb_user_admin_action_log` 즉시 삭제.

## 권장 접근

처음부터 공통 감사 로그 저장소를 사용한다.

1. 공통 감사 로그 API 계약을 정의한다.
2. `tb_admin_audit_log`를 생성한다.
3. 기존 `tb_user_admin_action_log` 행을 `tb_admin_audit_log`로 백필한다.
4. 새 사용자 관리 액션부터 공통 감사 로그 writer로 저장한다.
5. `AdminAuditLogQueryRepository`는 `tb_admin_audit_log`만 조회한다.
6. 기존 `tb_user_admin_action_log`는 deprecated 상태로 유지하고, 데이터 검증 후 별도 migration에서 제거 여부를 결정한다.

이 방식은 테이블 이름과 역할을 일치시키면서, 이후 다른 관리자 활동 카테고리를 같은 조회 API에 자연스럽게 추가할 수 있게 한다.

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
  - `AdminAuditLogCondition`과 query repository 경계에서도 `String`으로 유지한다. `UserAdminActionType` 같은 특정 도메인 enum은 사용자 액션 로그를 생성하는 쪽에서만 사용한다.
- `String keyword`
  - 선택값.
  - 수행자 또는 대상 사용자의 이메일, 이름, 학번을 검색한다.
- `Pageable pageable`
  - `@PageableDefault(page = 0, size = 10)`을 사용한다.
  - 기본 정렬은 `createdAt` 최신순이다.

검증:

- `from`, `to`가 모두 있으면 `from`은 `to`보다 늦을 수 없다.
- 공백 `keyword`는 필터 없음으로 처리한다.
- 알 수 없는 `category`는 enum binding 단계에서 실패한다.
- 현재 카테고리에서 지원하지 않는 `actionType`은 bad request로 처리한다.

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
- `String name`
- `String studentId`

`AuditTargetResponse`:

- `String type`
  - 1차 구현에서는 `USER`를 사용한다.
- `String id`
- `String email`
- `String name`
- `String studentId`

수행자와 대상 사용자 정보는 로그 발생 시점의 스냅샷으로 저장한다. 사용자 이름, 이메일, 학번이 이후 변경되거나 사용자가 삭제되더라도 감사 로그는 당시 행위의 식별 정보를 유지해야 한다. `studentId`는 없는 사용자가 있을 수 있으므로 nullable로 둔다. 대상이 사용자가 아닌 카테고리에서는 `targetEmail`, `targetName`, `targetStudentId`를 nullable로 둔다.

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
- 공백 키워드와 액션 타입을 정규화한다.
- 지원하지 않는 액션 타입을 bad request로 처리한다.
- `AdminAuditLogCondition.actionType`은 `String`으로 전달한다.
- 공통 query adapter를 호출한다.
- 기존 프로젝트 스타일에 맞춰 service result 또는 API 응답 DTO로 변환 가능한 Page를 반환한다.

### Entity

패키지:

- `net.causw.app.main.domain.admin.audit.entity`

클래스:

- `AdminAuditLog`

책임:

- `tb_admin_audit_log`와 매핑된다.
- 수행자와 대상의 식별 스냅샷을 저장한다.
- 도메인별 상세 변경 내용은 `metadataJson`에 저장한다.
- `metadataJson`은 유효한 JSON object 문자열로 저장한다. JPA entity는 `String metadataJson`을 갖고, writer/mapper에서 `ObjectMapper`로 `Map<String, Object>`와 상호 변환한다.

### Writer

패키지:

- `net.causw.app.main.domain.admin.audit.service.implementation`

클래스:

- `AdminAuditLogWriter`

책임:

- 도메인 서비스에서 전달한 감사 로그 생성 command를 저장한다.
- 사용자 관리 액션의 `DROP`, `RESTORE`, `ROLE_CHANGE` 저장 흐름을 기존 `UserAdminActionLogWriter`에서 대체한다.
- 변경 전후 상태처럼 도메인 서비스가 이미 알고 있는 값은 command의 metadata로 전달받는다.
- `AdminAuditLogCreateCommand.actionType`은 `String`으로 받는다. 사용자 관리 도메인은 `UserAdminActionType.name()`을 넘기고, 공통 감사 로그 도메인은 특정 사용자 enum에 의존하지 않는다.
- `metadata`는 writer에서 JSON 문자열로 직렬화한다. 직렬화 실패는 서버 설정/개발 오류로 보고 프로젝트 표준 예외로 감싼다.

### Query Adapter

1차 구현 패키지:

- `net.causw.app.main.domain.admin.audit.repository`

클래스:

- `AdminAuditLogQueryRepository`

책임:

- QueryDSL로 `AdminAuditLog`를 조회한다.
- 선택 필터를 적용한다.
- 조회 결과를 공통 감사 로그 query result로 projection한다.
- 최신순으로 반환한다.
- `QUserAdminActionLog` 또는 `tb_user_admin_action_log`를 조회하지 않는다. 기존 테이블은 migration 백필 입력과 deprecated 보존 용도로만 남긴다.

클래스명은 `AdminAuditLogQueryRepository`를 사용한다. 이 컴포넌트의 역할은 공통 감사 로그 API 조회를 제공하는 것이다.

### Mapper

패키지:

- `net.causw.app.main.domain.admin.audit.api.v2.mapper`

클래스:

- `AdminAuditLogMapper`

책임:

- query/service 결과를 `AdminAuditLogResponse`로 변환한다.
- 저장된 `summary`와 `actionDescription`을 응답에 그대로 전달한다.
- 저장된 `metadataJson`을 `Map<String, Object>`로 역직렬화한다.
- JSON 역직렬화에 실패하면 잘못 저장된 감사 로그 데이터이므로 서버 오류로 처리한다.

## 데이터 흐름

1. 관리자가 감사 로그 페이지를 연다.
2. 프론트엔드가 `GET /api/v2/admin/audit-logs`를 호출한다.
3. Controller가 필터와 pageable을 바인딩한다.
4. Service가 요청을 검증하고 정규화한다.
5. Query repository가 `tb_admin_audit_log`를 조회한다.
6. Mapper가 각 행을 공통 감사 로그 응답으로 변환한다.
7. API가 `PageResponse`를 반환한다.

## 공통 테이블 설계

여러 관리자 활동 카테고리를 하나의 저장소에서 조회할 수 있도록 `tb_admin_audit_log`를 1차 구현에 포함한다.

권장 컬럼:

- `id`
- `created_at`
- `updated_at`
- `category`
- `action_type`
- `action_description`
- `actor_user_id`
- `actor_email`
- `actor_name`
- `actor_student_id`
- `target_type`
- `target_id`
- `target_email`
- `target_name`
- `target_student_id`
- `summary`
- `metadata_json`
  - DB 호환성을 위해 `TEXT`로 생성하고 유효한 JSON object 문자열만 저장한다. 사용하는 DB에서 `JSON` 타입이 안정적으로 지원된다는 점이 확인되면 `JSON` 타입을 선택할 수 있다.

권장 인덱스:

- `(created_at)`
- `(category, action_type, created_at)`
- `(actor_email, created_at)`
- `(actor_name, created_at)`
- `(actor_student_id, created_at)`
- `(target_email, created_at)`
- `(target_name, created_at)`
- `(target_student_id, created_at)`

마이그레이션 단계:

1. `tb_admin_audit_log`를 생성한다.
2. 기존 `tb_user_admin_action_log` 데이터를 `tb_admin_audit_log`로 백필한다.
3. 백필 시 `category`는 `USER`, `target_type`은 `USER`, `action_type`은 기존 enum name, `action_description`은 기존 enum description, `summary`는 응답 설계의 요약 문구 규칙으로 채운다.
4. 백필 시 기존 테이블에 없는 이름과 학번은 사용자 테이블에서 조회 가능한 경우 채운다. 조회할 수 없으면 nullable로 둔다.
5. 백필 시 `beforeState`, `afterState`, `beforeRoles`, `afterRoles`, `reason`은 JSON object 형태의 `metadata_json`으로 저장한다.
6. 새 사용자 관리 액션부터 공통 테이블에 저장한다.
7. `AdminAuditLogQueryRepository`가 공통 테이블만 조회하도록 구현한다.
8. 기존 엔드포인트와 응답 계약은 유지한다.
9. 기존 `tb_user_admin_action_log`는 deprecated 상태로 유지하고, 검증 이후 별도 migration에서 제거 여부를 결정한다.

## 오류 처리

- 유효하지 않은 기간 조건은 프로젝트 표준 예외 패턴에 맞춰 bad request로 반환한다.
- 권한 오류는 기존 Spring Security 동작을 따른다.
- 필터 결과가 없으면 오류가 아니라 빈 페이지를 반환한다.
- `category` enum binding 자체가 실패하는 값은 binding 오류로 처리한다.
- 현재 카테고리에서 지원하지 않는 `actionType`은 bad request로 처리한다.

## 테스트 계획

### 단위 테스트

- Service는 `from > to` 조건을 거부한다.
- 공백 keyword는 null 또는 필터 없음으로 처리한다.
- 지원하지 않는 actionType은 bad request로 처리한다.
- service/query condition에서 actionType이 특정 도메인 enum이 아니라 `String`으로 유지된다.
- Writer는 사용자 관리 액션을 공통 `AdminAuditLog`로 저장한다.
- Writer는 metadata map을 JSON 문자열로 직렬화한다.
- Mapper는 `DROP`에 대해 기대한 summary와 metadata를 만든다.
- Mapper는 `RESTORE`에 대해 기대한 summary와 metadata를 만든다.
- Mapper는 `ROLE_CHANGE`에 대해 기대한 summary와 metadata를 만든다.
- Mapper는 저장된 `metadataJson`을 응답 `metadata` map으로 역직렬화한다.

### Repository 테스트

- 기간 조건으로 필터링한다.
- 카테고리로 필터링한다.
- 액션 타입으로 필터링한다.
- 관리자 이메일을 검색한다.
- 관리자 이름과 학번을 검색한다.
- 대상 사용자 이메일을 검색한다.
- 대상 사용자 이름과 학번을 검색한다.
- 최신순으로 정렬한다.
- `tb_user_admin_action_log`에만 존재하는 행은 백필 전에는 조회되지 않으며, 조회 repository는 `tb_admin_audit_log`만 데이터 소스로 사용한다.

### Controller 테스트

- 관리자는 감사 로그 목록을 조회할 수 있다.
- 관리자가 아닌 사용자는 감사 로그 목록을 조회할 수 없다.
- 응답은 `ApiResponse<PageResponse<AdminAuditLogResponse>>` 형태를 사용한다.

## 남은 결정 사항

- `keyword`가 이메일 외에 사용자 ID까지 검색해야 하는지.
- 프론트엔드 설계 확정 후 행 상세 조회 API가 필요한지.
- export가 필요한지, 필요하다면 누가 접근할 수 있는지.
- 기존 `tb_user_admin_action_log`를 언제 제거할지.
- 개인정보가 포함된 감사 로그의 보관 기간과 마스킹 정책.

## 인수 조건

- 관리자는 `GET /api/v2/admin/audit-logs`를 호출할 수 있다.
- API는 공통 `tb_admin_audit_log` 데이터를 공통 감사 로그 응답 형태로 반환한다.
- 기존 사용자 관리자 액션 로그는 공통 감사 로그 테이블로 백필된다.
- 신규 사용자 관리자 액션 로그는 공통 감사 로그 테이블에 저장된다.
- 기간, 카테고리, 액션 타입, 수행자/대상 이메일·이름·학번 키워드 필터가 동작한다.
- 결과는 페이지네이션되며 최신순으로 정렬된다.
- actor와 target 응답은 로그 발생 시점의 이름, 이메일, 학번 스냅샷을 포함한다.
- 공통 감사 로그 service/query DTO는 사용자 관리 전용 enum에 의존하지 않는다.

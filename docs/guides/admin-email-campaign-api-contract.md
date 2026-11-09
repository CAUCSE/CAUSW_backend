# 관리자 이메일 캠페인 API 계약

## 범위

관리자가 활성 회원을 조건으로 선택해 이메일 캠페인을 생성하고, 최종 확인 후 Amazon SES 발송을 요청하며, 캠페인과 수신자별 처리 상태를 조회하는 V2 API 계약을 정의한다.

기본 경로는 `/api/v2/admin/email-campaigns`이며 모든 API에 관리자 권한 정책을 적용한다. 모든 응답은 `ApiResponse<T>`로 감싸고 페이지 응답은 `PageResponse<T>`를 사용한다.

## 상태 계약

### 캠페인

| 현재 상태 | 허용되는 다음 상태 | 조건 |
|---|---|---|
| `DRAFT` | `QUEUED` | 제목과 수신자 수의 최종 확인값이 저장값과 일치할 때 |
| `QUEUED` | `SENDING` | dispatcher가 처리 가능한 recipient를 claim했을 때 |
| `SENDING` | `COMPLETED` | 모든 recipient가 terminal이며 실패가 없을 때 |
| `SENDING` | `PARTIALLY_FAILED` | 모든 recipient가 terminal이며 성공과 실패가 함께 있을 때 |
| `SENDING` | `FAILED` | 모든 recipient가 terminal이며 성공이 없을 때 |

`COMPLETED`, `PARTIALLY_FAILED`, `FAILED`는 terminal 상태다. `DRAFT`가 아닌 캠페인에 대한 발송 요청은 새 작업을 만들지 않는다.

### 수신자

| 현재 상태 | 허용되는 다음 상태 | 조건 |
|---|---|---|
| `PENDING` | `SENDING` | `nextAttemptAt`이 현재 이하이고 조건부 DB claim에 성공했을 때 |
| `SENDING` | `SENT` | SES가 메시지를 접수하고 message ID를 반환했을 때 |
| `SENDING` | `PENDING` | 재시도 가능한 오류이며 최대 시도 횟수 미만일 때 |
| `SENDING` | `FAILED` | 영구 오류이거나 최대 시도 횟수에 도달했을 때 |
| `PENDING` | `SKIPPED` | 발송 전에 스냅샷을 사용할 수 없는 명시적 사유가 확인됐을 때 |
| `SENDING` | `PENDING` | claim timeout을 넘긴 미완료 작업을 recovery할 때 |

`SENT`, `FAILED`, `SKIPPED`는 terminal 상태다. `SENT` recipient는 재실행 시 항상 제외한다.

## 대상 필터 계약

```json
{
  "admissionYears": [2020, 2021],
  "departments": ["SCHOOL_OF_SW"],
  "academicStatuses": ["ENROLLED", "GRADUATED"]
}
```

- 각 필드는 생략하거나 빈 배열로 보낼 수 있으며, 이때 해당 항목은 제한하지 않는다.
- 같은 필드의 값은 SQL `IN`으로 OR 처리하고 서로 다른 필드는 AND 처리한다.
- 중복 값은 제거한다.
- 알 수 없는 enum 값과 유효 범위를 벗어난 입학연도는 `EMAIL_CAMPAIGN_INVALID_FILTER`로 거부한다.
- 모든 조회는 `UserState.ACTIVE`, `deletedAt IS NULL`, 유효한 이메일을 기본 조건으로 적용한다.
- 미리보기와 캠페인 생성은 동일한 정규화 필터와 조회 조건을 사용한다.

## API 계약

### 대상 미리보기

`POST /target-preview`

- 요청: `filter`
- 응답: `recipientCount`, 입학연도·학과·학적 상태별 허용된 분포 집계
- 수신자 ID와 이메일 주소는 반환하지 않는다.

### 캠페인 생성

`POST /`

- 요청: `subject`, `html`, `filter`
- 응답: `campaignId`, `subject`, `recipientCount`, `status`, `createdAt`
- 서버는 HTML을 정제하고 대상 snapshot을 같은 생성 트랜잭션에 저장한다.
- 정제 후 본문이 비거나 허용할 수 없는 구조면 `EMAIL_CAMPAIGN_INVALID_HTML`로 거부한다.
- 대상이 없으면 `EMAIL_CAMPAIGN_RECIPIENT_EMPTY`로 거부한다.

### 발송 요청

`POST /{campaignId}/send`

```json
{
  "confirmedSubject": "동문회 행사 안내",
  "confirmedRecipientCount": 120
}
```

- 별도 confirmation token은 사용하지 않는다.
- 두 확인값을 저장된 캠페인 값과 비교하고 하나라도 다르면 `EMAIL_CAMPAIGN_CONFIRMATION_MISMATCH`로 거부한다.
- 최초 성공 요청만 `DRAFT`에서 `QUEUED`로 조건부 전이한다.
- 이미 발송 요청된 캠페인은 `EMAIL_CAMPAIGN_SEND_ALREADY_REQUESTED`로 거부하며 작업을 추가하지 않는다.

### 캠페인 목록 및 상세

- `GET /`: 상태·기간 조건과 `Pageable`로 캠페인 목록 및 집계를 조회한다.
- `GET /{campaignId}`: 캠페인 내용, 필터 요약, 상태별 집계와 진행률을 조회한다.
- 존재하지 않는 캠페인은 `EMAIL_CAMPAIGN_NOT_FOUND`로 응답한다.

### 캠페인별 recipient 목록

`GET /{campaignId}/recipients`

- 선택 조건: `status`
- 페이지 조건: Spring `Pageable`
- 응답 항목: `recipientId`, `maskedEmail`, `status`, `attemptCount`, `lastAttemptAt`, `sentAt`, `lastErrorCode`
- recipient 단건 상세 API는 제공하지 않는다.
- 이메일은 기존 `EmailMasker`를 사용해 로컬 파트 최대 3글자를 노출하고 최소 3개의 `*`를 붙이며 도메인은 유지한다.
- 원본 이메일과 `lastErrorDetail`은 API에 노출하지 않는다.

## 오류 계약

| 이름 | HTTP 상태 | 코드 |
|---|---:|---|
| `EMAIL_CAMPAIGN_NOT_FOUND` | 404 | `EMAIL_CAMPAIGN_404_001` |
| `EMAIL_CAMPAIGN_RECIPIENT_EMPTY` | 400 | `EMAIL_CAMPAIGN_400_001` |
| `EMAIL_CAMPAIGN_INVALID_FILTER` | 400 | `EMAIL_CAMPAIGN_400_002` |
| `EMAIL_CAMPAIGN_INVALID_HTML` | 400 | `EMAIL_CAMPAIGN_400_003` |
| `EMAIL_CAMPAIGN_CONFIRMATION_MISMATCH` | 400 | `EMAIL_CAMPAIGN_400_004` |
| `EMAIL_CAMPAIGN_INVALID_STATUS` | 409 | `EMAIL_CAMPAIGN_409_001` |
| `EMAIL_CAMPAIGN_SEND_ALREADY_REQUESTED` | 409 | `EMAIL_CAMPAIGN_409_002` |
| `EMAIL_CAMPAIGN_SES_DISABLED` | 503 | `EMAIL_CAMPAIGN_503_001` |
| `EMAIL_CAMPAIGN_SES_CONFIGURATION_INVALID` | 503 | `EMAIL_CAMPAIGN_503_002` |

SES의 수신자별 일시·영구 오류는 발송 작업 상태에 저장하며 관리자 API 요청 자체의 HTTP 실패로 전파하지 않는다.

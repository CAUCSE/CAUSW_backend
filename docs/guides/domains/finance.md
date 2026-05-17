# finance 도메인

위치: `app-main/src/main/java/net/causw/app/main/domain/finance`

## 책임

**학생회비(학생회 회비) 납부 관리** 를 책임집니다. 가입자/비가입자(가짜 사용자) 모두를 다루는 점이 특징입니다.

## 서브 도메인

```
domain/finance/
└── usercouncilfee/   # 학생회비
```

## usercouncilfee — 학생회비

`usercouncilfee/entity/` 하위:
- `UserCouncilFee` — 사용자별 회비 납부 기록
- `CouncilFeeFakeUser` — **회원 가입은 안 했지만 학생회비를 낸 가짜 사용자** (재학생 신원만 보유)
- `UserCouncilFeeLog` — 회비 관련 변경 이력 로그

주요 필드 (대표):
- 납부 학기 범위 (`paidAt`, `numOfPaidSemester`)
- 가입 회원(`User`) 또는 `CouncilFeeFakeUser` 중 하나 연결
- 면제 여부, 환불 여부 등

비즈니스 규칙 (대표):
- 한 학기 1회 납부 기록
- 가짜 사용자가 실제 회원 가입 시 `CouncilFeeFakeUser` → `User` 로 병합 / 매칭
- 학기 단위 통계 (총 납부 인원, 납부율 등)
- 변경 이력은 `UserCouncilFeeLog` 로 누적

## 외부 도메인 의존

| 의존 도메인 | 사용 |
|------------|------|
| user/account | `User` 와 1:N 연결 (또는 `CouncilFeeFakeUser` 가 별도) |
| campus/semester | 납부 기준 학기 |
| integration/export | 회비 납부자 명단 Excel/CSV 내보내기 |

## 주의 사항

- `CouncilFeeFakeUser` 는 회원 가입 흐름과 분리된 별도 엔티티 — 검색/병합 로직에서 빠뜨리지 않도록 주의
- 회비 데이터는 **개인정보** 라는 점 인식. 관련 조회는 관리자 권한 (`@PreAuthorize("hasRole('ADMIN')")` 등) 필수
- 정산 / 환불은 별도 외부 시스템과 연결되어 있지 않으면 수기 처리 (`integration/export` 활용)
- 운영자 액션은 가능한 한 `UserCouncilFeeLog` 에 기록

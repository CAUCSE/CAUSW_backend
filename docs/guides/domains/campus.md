# campus 도메인

위치: `app-main/src/main/java/net/causw/app/main/domain/campus`

## 책임

학교 캠퍼스 라이프와 관련된 도메인입니다. **학사 일정, 학기 정보, 동아리, 행사** 를 다룹니다.

## 서브 도메인

```
domain/campus/
├── schedule/      # 학사 일정
├── semester/      # 학기 정보
├── circle/        # 동아리
└── event/         # 행사
```

## schedule — 학사 일정

`schedule/entity/` 하위: `Schedule`

용도:
- 등록 기간, 시험 기간, 방학 등 학사 일정
- 메인 페이지 / 캘린더에 노출

ErrorCode: `ScheduleErrorCode`

## semester — 학기

`semester/entity/` 하위: `Semester`

용도:
- 현재 학기 식별 (예: 2026-1 학기)
- 학생회비 / 학적 등에서 학기 단위 기준점으로 사용

## circle — 동아리

`circle/entity/` 하위:
- `Circle` — 동아리 정의 (이름, 설명, 리더, 멤버 정원, 모집 상태 등)
- `CircleMember` — 동아리 ↔ 사용자 가입 정보 (조인 엔티티)

연관:
- `User` ↔ `CircleMember` ↔ `Circle`
- 동아리별 전용 게시판(`Board`) 보유 가능

API: v1, v2 모두 존재

비즈니스 규칙:
- 동아리장(`Circle.leader`) 변경, 멤버 가입 신청 / 승인 / 거절
- 비활성/탈퇴 멤버 처리

## event — 행사

`event/entity/` 하위: `Event`

용도:
- 학과 / 학생회 주최 행사 일정 및 정보
- 첨부 이미지 / 참여 신청 등

## 외부 도메인 의존

| 의존 도메인 | 사용 |
|------------|------|
| user/account | 동아리장 / 멤버, 행사 등록자 등으로 `User` 참조 |
| community/board | 동아리 전용 게시판 |
| asset/file | 동아리/행사 대표 이미지 등 (`UuidFile`) |
| notification | 일정/행사 알림 이벤트 |

## 주의 사항

- `Circle` 비활성화 시 소속 멤버 / 게시판 / 게시글 처리 정책 확인 필요
- 일정/행사는 `Asia/Seoul` 타임존 기준 (`CauswApplication` 의 `init()` 에서 강제)

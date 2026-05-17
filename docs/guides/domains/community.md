# community 도메인

위치: `app-main/src/main/java/net/causw/app/main/domain/community`

## 책임

회원 간 **콘텐츠 생산과 상호작용** 을 책임집니다. 게시판, 게시글, 댓글, 좋아요, 즐겨찾기, 투표, 폼 응답, 신고, 경조사, 홈페이지(메인 노출) 까지 커뮤니티 기능 전반.

## 서브 도메인

```
domain/community/
├── board/         # 게시판 / 카테고리
├── post/          # 게시글
├── comment/       # 댓글 / 대댓글
├── reaction/      # 좋아요, 즐겨찾기
├── vote/          # 게시글 첨부 투표
├── form/          # 게시글 첨부 폼 / 응답
├── report/        # 신고
├── ceremony/      # 경조사 게시판
└── homepage/      # 메인 페이지 노출 로직
```

## board — 게시판

`board/entity/` 하위:
- `Board` — 게시판 정의
- `FavoriteBoard` — 사용자가 즐겨찾기한 게시판

특징:
- 게시판마다 작성 가능한 Role 그룹, 폼/투표 허용 여부, 카테고리 등을 설정
- 동아리 (campus/circle) 별 전용 게시판 존재 가능

ErrorCode: `BoardErrorCode`, `BoardConfigErrorCode`

## post — 게시글

`post/entity/` 하위:
- `Post` (`tb_post`)
  - 핵심 필드: `content` (TEXT), `writer` (`@ManyToOne User`), `board` (`@ManyToOne Board`)
  - 상태 플래그: `isAnonymous`, `isQuestion`, `isCrawled`, `isDeleted` (soft delete)
  - 첨부: `postAttachImageList` (`PostAttachImage` 조인 엔티티 → `UuidFile`)
  - 옵션: `form`, `vote` (1:1 관계, 각각 폼/투표가 게시글에 붙음)
- 정의된 인덱스: `board_id_index`, `user_id_index`, `form_id_index`, `post_cursor_index (created_at, id)`

API:
- v1: `post/api/v1/controller/PostV1Controller`
- v2: `post/api/v2/controller/PostController` (`/api/v2/posts`)
  - 게시글 CRUD, 좋아요, 좋아요 취소, 게시판별 목록, 검색

서비스 (`post/service/v2/`):
- `PostService` — 트랜잭션 경계
- `LikePostService` — 좋아요 처리
- `implementation/PostReader`, `PostWriter`, `PostImageManager`

ErrorCode: `PostErrorCode`

## comment — 댓글 / 대댓글

`comment/entity/` 하위:
- `Comment` — 1차 댓글
- `ChildComment` — 대댓글 (부모 `Comment` 참조)
- `LikeComment`, `LikeChildComment` — 댓글 좋아요

연관:
- `Post` ← N `Comment` ← N `ChildComment`

ErrorCode: `CommentErrorCode`, `ChildCommentErrorCode`

## reaction — 반응

`reaction/entity/` 하위:
- `LikePost` — 게시글 좋아요
- `FavoritePost` — 게시글 즐겨찾기

UNIQUE 제약 (현행/권장): `(user_id, post_id)` — 중복 좋아요 방지

ErrorCode: `LikePostErrorCode`

## vote — 투표

`vote/entity/` 하위:
- `Vote` — 게시글에 첨부되는 투표
- `VoteOption` — 보기
- `VoteRecord` — 사용자 투표 기록

비즈니스 규칙 (대표):
- 복수 선택 가능 여부 (`allowMultiple`)
- 익명 투표 (`allowAnonymous`)
- 마감 시각 이후 변경 불가

## form — 폼

`form/entity/` 하위:
- `Form` — 게시글에 첨부되는 폼 정의
- `FormQuestion` — 질문
- `FormQuestionOption` — 선택형 질문의 보기
- `Reply` — 응답 (회당 하나)
- `ReplyQuestion` — 응답 안의 개별 질문 답
- `ReplySelectedOptionManager` — 선택형 응답 관리

비즈니스 규칙 (대표):
- 응답 가능 여부 검증 — 위반 시 `ErrorCode.NOT_ALLOWED_TO_REPLY_FORM`

## report — 신고

`report/entity/` 하위:
- `Report` — **통합 신고 엔티티** (대상 유형은 `ReportType` enum 으로 구분)
- `ReportReason` (enum) — 신고 사유
- `ReportType` (enum) — 신고 대상 유형 (Post / Comment / ChildComment 등)

Repository:
- `ReportRepository`
- `repository/projection/ReportedPostNativeProjection`, `ReportedCommentNativeProjection` — 관리자 화면용 native projection

API:
- v1: `ReportController`
- v2: `ReportController` + `ReportAdminController` (관리자 전용)

Mapper:
- `PostReportDtoMapper`, `CommentReportDtoMapper`, `ChildCommentReportDtoMapper`, `ReportAdminMapper` — 대상 유형별로 매퍼 분리

ErrorCode: `PostReportErrorCode`, `CommentReportErrorCode`, `ChildCommentReportErrorCode`

규칙:
- 한 사용자가 동일 대상에 중복 신고 불가
- 신고가 임계치를 넘으면 자동 숨김 등 후속 처리 (운영팀 검토)

엔티티는 통합되어 있지만 표현 계층(매퍼 / ErrorCode) 은 대상 유형별로 분리되어 있습니다.

## ceremony — 경조사

`ceremony/entity/` 하위에 경조사 알림용 엔티티가 위치. 알림 도메인과 연동되어 `CeremonyNotificationSetting` 으로 사용자별 수신 여부 제어.

ErrorCode: `CeremonyErrorCode`

## homepage — 메인 노출

서브 도메인이라기보다는, 홈페이지(메인) 에 노출할 인기 게시글/공지 등을 모으는 조회 전용 책임이 위치합니다.

## 외부 도메인 의존

| 의존 도메인 | 사용 |
|------------|------|
| user/account | 게시글/댓글 작성자, 좋아요/투표 주체 |
| user/relation | 차단 사용자 콘텐츠 필터링 |
| asset/file | 게시글 / 댓글 첨부 이미지 (`UuidFile`) |
| notification | 댓글 알림, 좋아요 알림 등의 이벤트 발행 |
| integration/crawled | 외부 공지 크롤링 결과를 게시글로 변환 (`Post.isCrawled=true`) |

## 주의 사항

- **익명 게시글/댓글** — `isAnonymous=true` 인 경우 응답 시 작성자 정보 마스킹 필수
- **soft delete 일관성** — `is_deleted=true` 인 데이터는 모든 조회에서 제외
- **인기/정렬 쿼리** — `created_at, id` 복합 인덱스 활용 (커서 기반 페이징 가능)
- **첨부 이미지** — `cascade = CascadeType.REMOVE` 가 걸려 있어 게시글 영구 삭제 시 첨부도 제거. 단 soft delete 만 했다면 첨부는 그대로 유지
- **report 엔티티 통합** — 새 신고 유형을 추가할 때 `ReportType` enum 갱신 + 매퍼/ErrorCode 분리 패턴 따르기

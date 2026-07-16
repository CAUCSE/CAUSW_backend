# Community 권한 정책 현황 및 의사결정 문서

> 작성일: 2026-07-16  
> 상태: 정책 결정 및 구현 완료 — 현재 정책은 [`community-permission-policy.md`](./community-permission-policy.md) 참고  
> 대상: Community 도메인의 게시판, 게시글, 댓글·대댓글 및 관련 반응 API

> 이 문서에서 "현재 구현"은 정책 통일 전의 점검 기준선을 뜻한다. 확정·적용된 정책은
> 위 현재 정책 문서를 기준으로 한다.

## 1. 문서 목적

Community 도메인에서 목록 노출, 상세 조회, 응답의 권한 필드, 실제 수정·삭제 API가 서로 다른 기준으로 권한을 판단하고 있다.

이 문서는 현재 구현을 기준으로 다음 내용을 정리한다.

- API별 실제 권한 판단 기준
- 목록·상세·변경 API 사이의 불일치
- `updatable`, `deletable`과 실제 실행 권한의 불일치
- 삭제, 숨김, 차단 및 사용자 상태에 따른 경계 사례
- 구현 전에 결정해야 하는 정책 선택지

이 문서는 현황 점검 및 정책 결정을 위한 문서이며, 아직 목표 정책을 확정하지 않는다.

## 2. 검토 범위

### 포함

- 게시판 읽기·쓰기 가능 목록
- 게시글 작성, 일반 목록, 개인화 목록, 상세, 수정, 삭제
- 댓글·대댓글 목록, 작성, 수정, 삭제
- 게시글·댓글·대댓글 좋아요
- 응답의 `isOwner`, `updatable`, `deletable`
- 게시판·게시글·댓글의 삭제 상태
- 사용자의 역할, 상태, 학적 상태
- 사용자 차단 관계

### 제외

- 신고 처리 권한
- 경조사 권한
- 관리자용 게시판 설정 API 자체의 접근 제어
- 투표 및 폼 서브도메인

관리자 게시판 API는 `/api/v2/admin/**` 보안 설정과 `@PreAuthorize`를 통해 시스템 관리자에게 제한되어 있어, 이번 공개 Community API 간 비교에서는 제외한다.

## 3. 용어

| 용어 | 의미 |
| --- | --- |
| 시스템 관리자 | `Role.ADMIN`을 가진 사용자 |
| 게시판 관리자 | 해당 게시판의 `BoardAdmin`에 등록된 사용자 |
| 전역 임원 | 댓글 수정 검증에 사용되는 `ADMIN`, `PRESIDENT`, `VICE_PRESIDENT` 역할 |
| 작성자 | 게시글 또는 댓글의 `writer`와 요청 사용자가 같은 경우 |
| 읽기 범위 | `BoardConfig.readScope`: `ENROLLED`, `GRADUATED`, `BOTH` |
| 쓰기 범위 | `BoardConfig.writeScope`: `ALL_USER`, `ONLY_ADMIN` |
| 숨김 게시판 | `BoardConfig.visibility == HIDDEN`인 게시판 |
| 삭제 게시판 | `Board.isDeleted == true`인 게시판 |
| 유효 사용자 | 현재 구현에서 API별 검증을 통과하는 사용자. 아직 통일된 정의가 없음 |

시스템 관리자와 게시판 관리자는 별개의 권한이다. 한 사용자가 두 권한을 동시에 가질 수 있지만, 현재 구현은 경로에 따라 두 권한을 다르게 취급한다.

## 4. 핵심 요약

현재 확인된 가장 중요한 문제는 다음과 같다.

1. 댓글·대댓글 삭제는 작성자 또는 관리자 여부를 확인하지 않는다.
2. 댓글·대댓글 응답의 `updatable`, `deletable`이 실제 수정·삭제 권한과 다르다.
3. 댓글 목록과 댓글 작성은 게시판 `readScope`, `visibility`를 확인하지 않는다.
4. 개인 게시글 목록은 현재 게시판 읽기 권한을 확인하지 않는다.
5. 삭제 게시판과 삭제 게시글의 노출·수정·반응 정책이 API마다 다르다.
6. 쓰기 가능한 게시판 목록과 실제 게시글 작성의 학적·숨김 조건이 다르다.
7. 사용자 상태 검증이 게시글 조회, 댓글 조회, 좋아요 등에서 서로 다르다.
8. 게시글 목록은 `isOwner`만 반환하여 게시판 관리자와 시스템 관리자의 삭제 가능 여부를 표현할 수 없다.

## 5. 현재 권한 모델

### 5.1 게시판 읽기 가능 목록

`GET /api/v2/boards/available`

현재 조건은 다음과 같다.

- 사용자의 학적 상태에 맞는 `readScope`
- `BoardConfig.visibility == VISIBLE`
- `Board.isDeleted == false`
- `isTab=true`이면 공지 게시판만 반환
- 시스템 관리자 우회 없음
- 게시판 관리자 우회 없음

관련 코드:

- `community/board/service/BoardService#getReadableBoards`
- `community/board/service/implementation/BoardConfigReader#getAccessibleBoardIdsByAcademicStatus`
- `community/board/repository/BoardConfigQueryRepository#findBoardsByReadScopes`

### 5.2 게시판 쓰기 가능 목록

`GET /api/v2/boards/writable`

| 사용자 | 현재 조건 |
| --- | --- |
| 일반 사용자 | 학적 상태가 `ENROLLED` 또는 `GRADUATED`이고, 게시판이 `VISIBLE`, 미삭제이며 `writeScope=ALL_USER` |
| 게시판 관리자 | 일반 사용자 학적 조건을 만족해야 하며, 본인이 관리하는 `ONLY_ADMIN` 게시판도 포함 |
| 시스템 관리자 | 학적 상태와 `writeScope`는 우회하지만 게시판은 `VISIBLE`, 미삭제여야 함 |

관련 코드:

- `community/board/service/BoardService#getWritableBoards`
- `community/board/repository/BoardConfigQueryRepository#findWritableBoardsByUserId`

### 5.3 게시판 직접 읽기 검증

게시글 목록에 `boardIds`가 명시되거나 게시글 상세를 조회할 때는 `PostValidator.validateRead`를 사용한다.

현재 조건은 다음과 같다.

- 시스템 관리자는 `visibility`, `readScope`를 모두 우회
- 게시판 관리자는 `visibility`, `readScope`를 모두 우회
- 일반 사용자는 `VISIBLE`이면서 학적 상태가 `readScope`에 맞아야 함
- 게시판 삭제 여부는 확인하지 않음
- 사용자 상태와 `Role.NONE` 여부는 확인하지 않음

따라서 읽기 가능 게시판 목록과 게시판 직접 읽기 검증은 동일한 권한 정책이 아니다.

## 6. 조회 API 비교

### 6.1 게시글·댓글 조회 경로

| 조회 경로 | `readScope` | `HIDDEN` | 삭제 게시판 | 삭제 게시글 | 관리자 우회 |
| --- | --- | --- | --- | --- | --- |
| `GET /boards/available` | 적용 | 제외 | 제외 | 해당 없음 | 없음 |
| `GET /posts` — `boardIds` 없음 | 적용 | 제외 | 필터 없음 | 제외 | 없음 |
| `GET /posts` — `boardIds` 지정 | 적용 | 일반 사용자 제외 | 필터 없음 | 제외 | 시스템/게시판 관리자 |
| `GET /posts/me` | 미적용 | 포함 가능 | 포함 가능 | 제외 | 해당 없음 |
| `GET /posts/me/liked` | 미적용 | 포함 가능 | 포함 가능 | 제외 | 해당 없음 |
| `GET /posts/me/commented` | 미적용 | 포함 가능 | 포함 가능 | 제외 | 해당 없음 |
| `GET /posts/{postId}` | 적용 | 일반 사용자 제외 | 필터 없음 | 필터 없음 | 시스템/게시판 관리자 |
| `GET /comments?postId={postId}` | 미적용 | 접근 가능 | 제외 | 제외 | 별도 우회 불필요 |

### 6.2 일반 게시글 목록

`GET /api/v2/posts`

#### `boardIds`가 없는 경우

- 학적 상태와 `VISIBLE` 기준으로 게시판 ID를 가져온다.
- 시스템 관리자 및 게시판 관리자 우회를 적용하지 않는다.
- 게시글 삭제 여부를 검사한다.
- 차단한 사용자의 게시글을 제외한다.
- 게시판 삭제 여부는 쿼리에서 검사하지 않는다.

`GET /boards/available`은 마지막에 삭제 게시판을 제거하지만 게시글 기본 목록은 `BoardConfig`에서 얻은 ID를 바로 사용한다. 따라서 삭제 게시판의 설정이 `VISIBLE`로 남아 있고 게시글이 삭제되지 않았다면 목록에 노출될 수 있다.

#### `boardIds`가 있는 경우

- 각 게시판에 `PostValidator.validateRead`를 적용한다.
- 시스템 관리자와 게시판 관리자는 숨김·학적 범위를 우회한다.
- 게시판 삭제 여부는 검사하지 않는다.
- 게시글 삭제 여부와 차단 관계는 검사한다.

### 6.3 개인화 게시글 목록

다음 API는 게시판 읽기 권한을 확인하지 않는다.

- `GET /api/v2/posts/me`
- `GET /api/v2/posts/me/liked`
- `GET /api/v2/posts/me/commented`

공통적으로 삭제 게시글은 제외하지만 다음 상태의 게시글은 포함될 수 있다.

- 현재 학적 상태로 읽을 수 없는 게시판
- 숨김 게시판
- 삭제 게시판

`me/liked`, `me/commented`는 차단한 작성자의 게시글을 제외하지만 게시판 권한은 확인하지 않는다.

이 때문에 개인 목록에는 표시되지만 상세 조회에서는 권한 오류가 발생하는 항목이 생길 수 있다.

### 6.4 게시글 상세 조회

`GET /api/v2/posts/{postId}`

현재 흐름은 다음과 같다.

1. `PostReader.findById`로 게시글 조회
2. `PostValidator.validateRead`로 게시판 읽기 범위 검증
3. 게시글 작성자 차단 관계 검증
4. `updatable`, `deletable` 계산

`PostReader.findById`는 게시글 삭제 여부를 검사하지 않는다. `validateRead`는 게시판 삭제 여부를 검사하지 않는다.

따라서 다음 상태도 상세 응답이 나갈 수 있다.

- 삭제된 게시글
- 삭제 게시판에 속한 게시글

또한 시스템 관리자는 읽기 범위는 우회하지만 차단 관계에서는 별도 우회가 없다. 시스템 관리자가 게시글 작성자를 차단한 경우, 게시판 관리자가 아니라면 상세 조회는 실패하지만 게시글 삭제 API는 호출할 수 있다.

### 6.5 댓글 목록 조회

`GET /api/v2/comments?postId={postId}`

현재 검증은 다음과 같다.

- 요청 사용자 `DROP`, `INACTIVE`, `Role.NONE` 차단
- 삭제 게시판 차단
- 삭제 게시글 차단
- 삭제 댓글 제외

다음 항목은 검증하지 않는다.

- 게시판 `readScope`
- 게시판 `visibility`
- 상위 게시글 작성자에 대한 차단 관계

따라서 게시글 상세를 조회할 수 없는 사용자도 `postId`를 알고 있다면 댓글 목록을 조회할 수 있다.

## 7. 작성 API 비교

| 경로 | 학적 상태 | `HIDDEN` | `readScope` | `writeScope` |
| --- | --- | --- | --- | --- |
| `GET /boards/writable` | 일반 사용자는 재학·졸업만 | 제외 | 미적용 | 적용 |
| `POST /posts` | 미검사 | 작성 가능 | 미적용 | 적용 |
| `POST /comments` | 미검사 | 작성 가능 | 미적용 | 미적용 |
| `POST /child-comments` | 미검사 | 작성 가능 | 미적용 | 미적용 |

### 7.1 게시글 작성

`POST /api/v2/posts`

현재 조건은 다음과 같다.

- 삭제 게시판에는 작성 불가
- `DROP`, `INACTIVE`, `Role.NONE` 사용자 작성 불가
- 시스템 관리자는 `writeScope` 우회
- `ALL_USER`이면 일반 사용자 작성 가능
- `ONLY_ADMIN`이면 게시판 관리자 작성 가능
- 게시판 `visibility` 미검사
- 학적 상태 미검사

이에 따라 다음 불일치가 발생한다.

- 쓰기 가능 목록이 빈 사용자도 `ALL_USER` 게시판 ID를 직접 전달하면 작성 가능
- 숨김 게시판은 쓰기 가능 목록에 없지만 실제 작성 가능
- 시스템 관리자도 숨김 게시판을 쓰기 목록에서는 찾을 수 없지만 직접 작성 가능

### 7.2 댓글·대댓글 작성

댓글 작성은 사용자 상태와 상위 게시글·게시판 삭제 여부만 검사한다.

다음 항목은 검사하지 않는다.

- 상위 게시글 읽기 권한
- 게시판 `visibility`
- 게시판 `readScope`
- 게시판 `writeScope`
- 게시판 익명 허용 여부

댓글 작성에 게시글 작성용 `writeScope`를 적용할지는 정책 결정 사항이다. 다만 최소한 상위 게시글을 읽을 수 있는지 여부는 통일할 필요가 있다.

## 8. 수정·삭제 권한 비교

다음 표는 게시판·게시글·댓글이 삭제되지 않았고 요청 사용자가 각 API의 공통 상태 검증을 통과한다는 전제다. 각 칸은 `응답 플래그 / 실제 API` 순서다.

| 요청자 | 게시글 수정 | 게시글 삭제 | 댓글 수정 | 댓글 삭제 |
| --- | --- | --- | --- | --- |
| 작성자 | 허용 / 허용 | 허용 / 허용 | 허용 / 허용 | 허용 / 허용 |
| 게시판 관리자 | 불가 / 불가 | 허용 / 허용 | 허용 / 불가 | 허용 / 허용 |
| 시스템 관리자 | 불가 / 불가 | 허용 / 허용 | 불가 / 허용 | 불가 / 허용 |
| 회장·부회장 | 불가 / 불가 | 불가 / 불가 | 불가 / 허용 | 불가 / 허용 |
| 일반 사용자 | 불가 / 불가 | 불가 / 불가 | 불가 / 불가 | 불가 / 허용 |

### 8.1 게시글 수정

실제 API와 상세 응답 모두 작성자만 허용한다.

다만 실제 수정 API는 다음 상태를 추가로 차단한다.

- 삭제 게시판
- 삭제 게시글
- `DROP`, `INACTIVE`, `Role.NONE` 사용자

상세 응답의 `updatable` 계산에는 위 상태가 포함되지 않는다. 삭제된 게시글 또는 삭제 게시판 게시글의 상세 응답이 나가면 `updatable=true`이지만 실제 수정은 실패할 수 있다.

### 8.2 게시글 삭제

실제 API와 상세 응답의 역할 기준은 다음과 같이 일치한다.

- 작성자
- 게시판 관리자
- 시스템 관리자

그러나 다음 상태 차이가 있다.

- 상세 응답은 게시판 삭제 여부를 반영하지 않음
- 삭제 게시글 상세에서도 `deletable=true`가 될 수 있음
- 실제 삭제는 삭제 게시판에서 실패
- 실제 삭제는 게시글의 기존 삭제 여부를 확인하지 않아 재삭제 가능

### 8.3 댓글·대댓글 응답 플래그

`CommentAuthorInfo`는 댓글과 대댓글에 공통으로 사용된다.

현재 계산은 다음과 같다.

```text
isOwner = 댓글 작성자 == 현재 사용자
canEdit = isOwner || 현재 사용자가 게시판 관리자
updatable = canEdit
deletable = canEdit
```

따라서 작성자 또는 게시판 관리자에게 수정·삭제 가능으로 응답한다.

### 8.4 댓글·대댓글 실제 수정

실제 수정은 다음 사용자에게 허용된다.

- 댓글 작성자
- 시스템 관리자
- 회장
- 부회장

게시판 관리자는 위 역할을 함께 가지고 있지 않으면 타인의 댓글을 수정할 수 없다.

결과적으로 다음 문제가 발생한다.

- 게시판 관리자: `updatable=true`이지만 실제 수정 실패
- 시스템 관리자·회장·부회장: `updatable=false`이지만 실제 수정 성공

### 8.5 댓글·대댓글 실제 삭제

실제 삭제 검증에는 작성자 또는 관리자 여부를 확인하는 로직이 없다.

다음 공통 조건만 통과하면 타인의 댓글을 삭제할 수 있다.

- 요청자가 `DROP`, `INACTIVE`, `Role.NONE`이 아님
- 상위 게시판이 삭제되지 않음
- 상위 게시글이 삭제되지 않음
- 댓글이 삭제되지 않음

따라서 일반 사용자도 타인의 댓글과 대댓글을 삭제할 수 있다. 우선순위가 가장 높은 권한 취약점이다.

## 9. 응답 계약 불일치

### 9.1 게시글 목록과 상세

게시글 목록 응답은 다음 필드만 제공한다.

- `isOwner`

게시글 상세 응답은 다음 필드를 제공한다.

- `isOwner`
- `updatable`
- `deletable`

게시판 관리자와 시스템 관리자는 타인의 게시글을 삭제할 수 있지만 목록 응답의 `isOwner=false`만으로는 이를 표현할 수 없다.

목록 화면에서 수정·삭제 액션을 제공해야 한다면 다음 중 하나를 결정해야 한다.

- 목록에도 `updatable`, `deletable` 제공
- 목록에서는 액션을 제공하지 않고 상세에서만 판단
- 별도 권한 객체 제공

### 9.2 댓글 목록

댓글 목록에는 `updatable`, `deletable`이 있지만 실제 API와 기준이 다르다. 필드 자체보다 계산 기준을 실제 액션 정책과 공유하도록 변경해야 한다.

### 9.3 철자

현재 응답 필드명은 `deleatable`이 아니라 `deletable`이다. 문서, 프론트엔드 타입 및 신규 코드에서도 `deletable`로 통일한다.

## 10. 상태 정책 불일치

### 10.1 게시판 삭제

게시판 삭제는 현재 `Board.isDeleted=true`만 설정한다.

다음 작업은 수행하지 않는다.

- `BoardConfig.visibility=HIDDEN` 변경
- 해당 게시판 게시글 일괄 소프트 삭제
- 게시글 쿼리에서 `board.isDeleted=false` 공통 적용

이 때문에 삭제 게시판에 대해 API별 결과가 달라진다.

| 액션 | 현재 결과 |
| --- | --- |
| 읽기 가능 게시판 목록 | 제외 |
| 쓰기 가능 게시판 목록 | 제외 |
| 기본 게시글 목록 | 노출 가능 |
| 개인 게시글 목록 | 노출 가능 |
| 게시글 상세 | 조회 가능 |
| 게시글 수정·삭제 | 실패 |
| 댓글 목록·작성 | 실패 |
| 좋아요 | 일부 경로에서 가능 |

### 10.2 게시글 삭제

| 액션 | 현재 결과 |
| --- | --- |
| 게시글 일반·개인 목록 | 제외 |
| 게시글 상세 | 조회 가능 |
| 게시글 수정 | 실패 |
| 게시글 재삭제 | 가능 |
| 댓글 목록·작성 | 실패 |
| 게시글 좋아요 | 가능 |

삭제 게시글을 완전히 찾을 수 없는 것으로 처리할지, tombstone 형태로 상세 조회를 허용할지 결정해야 한다.

### 10.3 댓글 삭제

- 댓글·대댓글 목록에서는 삭제 댓글 제외
- 단건 수정·삭제 조회에서도 삭제 댓글 제외
- 삭제 API 응답에는 삭제된 Comment 객체를 다시 매핑
- 삭제 응답의 `updatable`, `deletable`은 삭제 상태를 반영하지 않을 수 있음

## 11. 사용자 상태 검증 불일치

Community 공개 API는 보안 설정상 기본적으로 인증만 요구한다. 사용자 상태는 서비스별로 다르게 검증한다.

| 경로 | 사용자 상태 검증 |
| --- | --- |
| 게시판 읽기 목록 | 명시적 검증 없음 |
| 게시판 쓰기 목록 | 학적 상태만 확인 |
| 게시글 목록·상세 | 명시적 검증 없음 |
| 게시글 작성·수정·삭제 | `DROP`, `INACTIVE`, `Role.NONE` 차단 |
| 댓글 목록·작성·수정·삭제 | `DROP`, `INACTIVE`, `Role.NONE` 차단 |
| 게시글 좋아요 | 서비스에서 사용자 상태 조회 없음 |
| 댓글 좋아요 | `deletedAt`이 없는 사용자 조회, 상태 검증 없음 |

현재 검증은 `UserState.ACTIVE`만 허용하는 방식이 아니라 특정 상태와 역할을 개별적으로 거부하는 방식이다. `AWAIT`, `REJECT`, `GUEST` 등의 처리도 명시적으로 결정할 필요가 있다.

## 12. 차단 정책 불일치

| 경로 | 현재 차단 정책 |
| --- | --- |
| 일반 게시글 목록 | 차단한 작성자의 게시글 제외 |
| `me/liked`, `me/commented` | 차단한 작성자의 게시글 제외 |
| 게시글 상세 | 차단한 작성자의 게시글 조회 실패 |
| 게시글 상세 — 작성자 본인 | 차단 관계 우회 |
| 게시글 상세 — 게시판 관리자 | 차단 관계 우회 |
| 게시글 상세 — 시스템 관리자 | 게시판 관리자가 아니면 우회하지 않음 |
| 댓글 목록 | 상위 게시글 작성자 차단 여부 미검사 |
| 댓글 표시 | 차단한 댓글 작성자의 내용과 프로필 마스킹 |

시스템 관리자가 moderation 목적으로 차단 관계를 우회할 수 있어야 하는지 결정해야 한다.

## 13. 좋아요 권한

### 13.1 게시글 좋아요

좋아요 생성은 `PostReader.findById`로 게시글을 조회한 뒤 중복 여부만 확인한다.

다음 항목은 확인하지 않는다.

- 게시글 읽기 권한
- 게시판 삭제 여부
- 게시판 `visibility`
- 게시글 삭제 여부
- 사용자 상태

좋아요 취소는 게시글 자체를 조회하지 않고 기존 좋아요 관계만 확인한다.

### 13.2 댓글·대댓글 좋아요

댓글은 삭제되지 않은 댓글을 조회하지만 다음 항목은 확인하지 않는다.

- 상위 게시글 삭제 여부
- 상위 게시판 삭제 여부
- 상위 게시글 읽기 권한
- 게시판 `visibility`
- 사용자 활성 상태

좋아요 권한을 이번 정리 범위에 포함할지 결정해야 한다.

## 14. 불일치 목록 및 우선순위

| ID | 우선순위 | 문제 |
| --- | --- | --- |
| CMT-AUTH-001 | P0 | 일반 사용자가 타인의 댓글·대댓글을 삭제할 수 있음 |
| CMT-AUTH-002 | P0 | 댓글·대댓글 `updatable`, `deletable`과 실제 API 권한 불일치 |
| CMT-READ-001 | P1 | 댓글 목록이 게시판 읽기 권한을 검사하지 않음 |
| CMT-WRITE-001 | P1 | 댓글 작성이 게시판 읽기·노출 정책을 검사하지 않음 |
| PST-READ-001 | P1 | 개인화 목록이 현재 게시판 읽기 권한을 우회함 |
| PST-STATE-001 | P1 | 삭제 게시글이 상세 조회 및 좋아요 대상이 될 수 있음 |
| BRD-STATE-001 | P1 | 삭제 게시판 콘텐츠 처리 기준이 API마다 다름 |
| PST-WRITE-001 | P1 | 쓰기 가능 목록과 실제 게시글 작성의 학적·숨김 기준이 다름 |
| PST-CONTRACT-001 | P2 | 게시글 목록에는 실제 삭제 권한을 나타낼 필드가 없음 |
| USER-AUTH-001 | P2 | 사용자 상태 검증이 API마다 다름 |
| BLOCK-001 | P2 | 목록·상세·댓글·관리자의 차단 우회 기준이 다름 |
| REACTION-001 | P2 | 게시글·댓글 좋아요가 읽기 및 대상 상태 정책을 우회함 |

## 15. 정책 결정 항목

아래 항목의 `결정` 열을 채운 후 구현 범위를 확정한다.

### D1. Community 유효 사용자

| 선택지 | 내용 |
| --- | --- |
| A | 모든 Community API를 `ACTIVE && !Role.NONE` 사용자에게만 허용 |
| B | 읽기는 인증 사용자에게 허용하고 작성·반응·수정·삭제만 활성 사용자에게 허용 |
| C | API별로 다른 상태 정책 유지 |

**결정:** 미정

### D2. `HIDDEN`의 의미

| 선택지 | 내용 |
| --- | --- |
| A | 목록 비노출만 의미하며 ID를 통한 직접 접근은 허용 |
| B | 일반 사용자 접근 불가, 게시판 관리자와 시스템 관리자만 접근 |
| C | 공개 API에서는 모두 접근 불가, 관리자 API에서만 접근 |

**결정:** 미정

### D3. 읽기 권한 우회 주체

다음 주체별로 `readScope` 및 `HIDDEN` 우회 여부를 결정한다.

| 주체 | `readScope` 우회 | `HIDDEN` 우회 |
| --- | --- | --- |
| 시스템 관리자 | 미정 | 미정 |
| 게시판 관리자 | 미정 | 미정 |
| 게시글 작성자 | 미정 | 미정 |

### D4. 게시글 작성의 학적 제한

| 선택지 | 내용 |
| --- | --- |
| A | 재학·졸업 사용자만 작성 가능 |
| B | `writeScope`만 적용하고 학적 상태는 제한하지 않음 |
| C | 일반 사용자는 재학·졸업만, 게시판 관리자와 시스템 관리자는 우회 |

**결정:** 미정

### D5. 삭제 게시판

| 선택지 | 내용 |
| --- | --- |
| A | 모든 공개 조회·작성·수정·삭제·반응 차단 |
| B | 과거 콘텐츠 읽기만 허용하고 모든 변경 차단 |
| C | 게시판 삭제 시 소속 게시글도 소프트 삭제 |

A와 C는 함께 선택할 수 있다.

**결정:** 미정

### D6. 삭제 게시글

| 선택지 | 내용 |
| --- | --- |
| A | 목록·상세·댓글·좋아요에서 모두 찾을 수 없는 것으로 처리 |
| B | tombstone 상세만 허용하고 모든 변경·반응 차단 |

재삭제 요청도 함께 결정한다.

| 선택지 | 내용 |
| --- | --- |
| A | 이미 삭제된 경우 `404 Not Found` |
| B | 멱등 요청으로 보고 `204 No Content` |

**결정:** 미정

### D7. 개인 게시글 목록

| 선택지 | 내용 |
| --- | --- |
| A | 현재 읽을 수 없는 게시판의 게시글 제외 |
| B | 과거 이력을 반환하되 `accessible=false`와 같이 상태 명시 |
| C | 현재처럼 목록에는 반환하고 상세에서 실패 |

**결정:** 미정

### D8. 댓글 조회·작성

| 항목 | 결정 |
| --- | --- |
| 댓글 조회는 상위 게시글 `canRead` 필요 | 미정 |
| 댓글 작성은 상위 게시글 `canRead` 필요 | 미정 |
| 댓글 작성에도 게시판 `writeScope` 적용 | 미정 |
| `HIDDEN` 게시판 댓글 작성 허용 주체 | 미정 |
| 게시판 익명 설정을 댓글·대댓글에도 적용 | 미정 |

### D9. 콘텐츠 수정·삭제 주체

목표 권한을 직접 표시한다.

| 주체 | 게시글 수정 | 게시글 삭제 | 댓글 수정 | 댓글 삭제 |
| --- | --- | --- | --- | --- |
| 작성자 | 미정 | 미정 | 미정 | 미정 |
| 게시판 관리자 | 미정 | 미정 | 미정 | 미정 |
| 시스템 관리자 | 미정 | 미정 | 미정 | 미정 |
| 회장·부회장 | 미정 | 미정 | 미정 | 미정 |
| 일반 사용자 | 미정 | 미정 | 미정 | 미정 |

추가 결정 사항:

- 관리자가 타인의 콘텐츠를 직접 수정할 수 있는가
- 관리자는 타인의 콘텐츠를 수정하지 않고 삭제만 할 수 있는가
- 회장·부회장을 Community 전체 관리자로 볼 것인가

### D10. 응답 권한 필드

| 항목 | 결정 |
| --- | --- |
| 게시글 목록에도 `updatable`, `deletable` 제공 | 미정 |
| `isOwner`는 작성자 표시 용도로만 사용 | 미정 |
| 삭제·숨김·사용자 상태까지 플래그에 반영 | 미정 |
| 목록과 상세가 동일한 권한 계산기를 사용 | 미정 |

### D11. 차단 관계

| 항목 | 결정 |
| --- | --- |
| 차단한 작성자의 게시글을 목록에서 제외 | 미정 |
| 차단한 작성자의 게시글 상세 차단 | 미정 |
| 해당 게시글의 댓글 목록도 함께 차단 | 미정 |
| 게시판 관리자의 moderation 우회 | 미정 |
| 시스템 관리자의 moderation 우회 | 미정 |

### D12. 좋아요

| 항목 | 결정 |
| --- | --- |
| 좋아요 생성 시 대상 읽기 권한 필요 | 미정 |
| 좋아요 생성 시 대상·게시판 생존 필요 | 미정 |
| 좋아요 취소 시 읽기 권한 재검사 | 미정 |
| 접근 권한 상실 후에도 본인 좋아요 취소 허용 | 미정 |

## 16. 일관된 정책을 위한 권장 불변 조건

구체적인 역할 범위와 무관하게 다음 조건은 공통으로 지키는 것을 권장한다.

1. 목록에서 정상 콘텐츠로 노출된 항목은 상세 조회도 가능해야 한다.
2. 개인 이력 때문에 읽을 수 없는 항목을 반환한다면 접근 불가 상태를 응답에 명시해야 한다.
3. 댓글 조회·작성·반응은 상위 게시글 접근 정책을 우회하지 않아야 한다.
4. `updatable`, `deletable`은 실제 수정·삭제 API와 동일한 정책 함수를 사용해야 한다.
5. 댓글과 대댓글은 콘텐츠 깊이 외에는 동일한 권한 정책을 사용해야 한다.
6. 일반 목록, 명시적 게시판 목록, 개인화 목록은 공통 `canReadBoard` 또는 `canReadPost` 정책을 사용해야 한다.
7. 대상 삭제 상태와 요청 사용자 상태를 권한 플래그 계산에도 포함해야 한다.
8. 시스템 관리자와 게시판 관리자의 역할 차이를 명시하고 암묵적으로 혼용하지 않아야 한다.
9. 숨김을 단순 목록 비노출로 사용할 경우 직접 접근 허용은 의도된 차이임을 API 문서에 명시해야 한다.
10. 삭제 정책은 목록, 상세, 댓글, 좋아요에서 동일하게 적용해야 한다.

## 17. 참고 기준안

안전성과 클라이언트 예측 가능성을 우선한 하나의 기준 예시는 다음과 같다. 이 항목은 확정 정책이 아니다.

1. 공개 Community API는 활성 사용자만 사용한다.
2. 일반 사용자의 게시판 읽기는 `미삭제 && VISIBLE && readScope 충족`으로 통일한다.
3. 시스템 관리자와 게시판 관리자는 moderation 목적으로 `readScope`, `HIDDEN`을 우회한다.
4. 게시글 작성은 쓰기 가능 게시판 목록과 같은 기준을 사용한다.
5. 개인화 목록도 현재 읽을 수 있는 게시글만 반환한다.
6. 게시글 상세는 미삭제 게시판의 미삭제 게시글만 반환한다.
7. 댓글 조회·작성은 상위 게시글 읽기 권한을 요구한다.
8. 게시글·댓글 수정은 작성자만 허용한다.
9. 게시글·댓글 삭제는 작성자, 게시판 관리자, 시스템 관리자에게 허용한다.
10. 회장·부회장은 별도 운영 정책이 없다면 시스템 관리자와 동일하게 취급하지 않는다.
11. 모든 `updatable`, `deletable`은 실제 액션 정책에서 계산한다.
12. 좋아요 생성은 읽을 수 있는 활성 콘텐츠에만 허용하고, 취소는 본인의 기존 관계가 있으면 허용한다.

## 18. 정책 확정 후 예상 작업 범위

- 공통 사용자 유효성 정책 정의
- 공통 `canReadBoard`, `canWritePost` 정책 정의
- 공통 `canReadPost`, `canUpdatePost`, `canDeletePost` 정책 정의
- 공통 `canReadComment`, `canCreateComment`, `canUpdateComment`, `canDeleteComment` 정책 정의
- 게시판 목록과 게시글 일반·개인 목록에 공통 읽기 정책 적용
- 게시글 상세 및 댓글 목록에 공통 읽기 정책 적용
- 응답 권한 필드를 실제 액션 정책으로 계산
- 게시판·게시글 삭제 상태 조회 방식 통일
- 좋아요 생성·취소 정책 적용
- Swagger 및 Community 기능 명세 갱신

## 19. 정책 확정 후 필수 테스트

### 사용자 축

- 일반 사용자
- 게시글·댓글 작성자
- 게시판 관리자
- 시스템 관리자
- 회장·부회장
- `Role.NONE`
- `ACTIVE`, `AWAIT`, `GUEST`, `INACTIVE`, `DROP`, `REJECT`

### 게시판 축

- `VISIBLE`, `HIDDEN`
- `ENROLLED`, `GRADUATED`, `BOTH`
- `ALL_USER`, `ONLY_ADMIN`
- 정상, 삭제

### 콘텐츠 축

- 정상 게시글·댓글
- 삭제 게시글·댓글
- 삭제 게시판의 게시글·댓글
- 본인 콘텐츠, 타인 콘텐츠
- 차단한 사용자 콘텐츠

### API 일관성 검증

- 게시판 읽기 가능 목록에 포함된 게시판의 게시글 목록·상세 조회 성공
- 개인 목록에 노출된 게시글의 상세 접근 정책 확인
- 댓글 목록·작성 권한이 상위 게시글 읽기 권한과 일치
- `updatable=true`이면 실제 수정 성공
- `updatable=false`이면 실제 수정 실패
- `deletable=true`이면 실제 삭제 성공
- `deletable=false`이면 실제 삭제 실패
- 댓글과 대댓글이 동일한 권한 결과를 반환
- 삭제·숨김·차단 상태에서 목록·상세·변경·반응 결과 일치

## 20. 주요 구현 근거

- `app-main/src/main/java/net/causw/app/main/domain/community/board/service/BoardService.java`
- `app-main/src/main/java/net/causw/app/main/domain/community/board/service/BoardAdminService.java`
- `app-main/src/main/java/net/causw/app/main/domain/community/board/service/implementation/BoardConfigReader.java`
- `app-main/src/main/java/net/causw/app/main/domain/community/board/repository/BoardConfigQueryRepository.java`
- `app-main/src/main/java/net/causw/app/main/domain/community/post/service/PostService.java`
- `app-main/src/main/java/net/causw/app/main/domain/community/post/service/LikePostService.java`
- `app-main/src/main/java/net/causw/app/main/domain/community/post/service/util/PostValidator.java`
- `app-main/src/main/java/net/causw/app/main/domain/community/post/service/util/LikePostValidator.java`
- `app-main/src/main/java/net/causw/app/main/domain/community/post/service/implementation/PostReader.java`
- `app-main/src/main/java/net/causw/app/main/domain/community/post/repository/query/PostQueryRepository.java`
- `app-main/src/main/java/net/causw/app/main/domain/community/comment/service/CommentService.java`
- `app-main/src/main/java/net/causw/app/main/domain/community/comment/util/CommentValidator.java`
- `app-main/src/main/java/net/causw/app/main/domain/community/comment/service/dto/CommentAuthorInfo.java`
- `app-main/src/main/java/net/causw/app/main/domain/community/comment/service/implementation/CommentReader.java`
- `app-main/src/main/java/net/causw/app/main/core/security/WebSecurityConfig.java`

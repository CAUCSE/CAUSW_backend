# Community soft delete PR 리뷰 대응

> 대상 PR: [#1418](https://github.com/CAUCSE/CAUSW_backend/pull/1418)
> 상태: 선택안 구현 및 검증 완료

## 1. 삭제된 게시글 재삭제 권한

### Review

삭제된 게시글이면 권한과 차단 정책을 확인하지 않고 성공해 비인가 사용자도 삭제 API를
우회할 수 있다는 지적이다.

### Options

- 권한과 차단을 확인한 뒤 권한자에게만 멱등 성공: 기존 API 계약을 유지하지만 재삭제
  전용 권한 계산이 필요하다.
- 삭제된 게시글은 항상 `404`: 구현은 단순하지만 문서화된 멱등 계약이 깨진다.
- 권한자는 성공, 비인가자는 `404`: 정보 은닉에는 유리하지만 생존 게시글의 `403`
  계약과 달라진다.

### Decision

첫 번째 안을 선택했다. 게시글 자체의 삭제 플래그만 무시하는
`canDeletePostIgnoringTargetDeletion`을 추가하고, 게시판 생존·읽기 권한·삭제 주체
권한과 차단 정책은 그대로 검사한다.

## 2. 개인 게시글 목록의 관리자 읽기 정책

### Review

개인 목록에서 생성하는 조회 컨텍스트가 `systemAdmin=false`로 고정되어 시스템 관리자의
HIDDEN/readScope 우회가 사라진다는 지적이다. 서비스의 게시판 ID 선필터 때문에 단순히
해당 불리언만 고쳐도 HIDDEN 게시판은 쿼리에 도달하지 않는 추가 불일치가 있다.

### Options

- 게시판 ID 선필터를 제거하고 공통 `PostReadQueryContext`를 전달: 일반 목록과 정책이
  완전히 일치하며 시스템 관리자와 게시판 관리자 모두 올바르게 처리된다.
- 시스템 관리자만 예외 처리: 변경은 작지만 게시판 관리자의 HIDDEN 게시판 누락이 남는다.
- `BoardAccessManager`로 게시판 ID를 선계산: 정확하지만 게시판 전체 조회와 게시글 쿼리의
  권한 필터가 중복된다.

### Decision

첫 번째 안을 선택했다. `/me`, `/me/liked`, `/me/commented` 모두 서비스에서 조회 컨텍스트를
생성하고 QueryRepository의 공통 `canReadPost` 조건으로 권한을 계산한다.

## 3. Form bulk update 이후 영속성 컨텍스트

### Review

JPQL bulk update가 영속성 컨텍스트를 우회하므로 같은 트랜잭션에 이미 로드된 Form이
stale 상태로 남을 수 있다는 지적이다.

### Decision

기존 Post bulk update와 동일하게 `clearAutomatically=true`를 적용한다. 현재 게시판 삭제
흐름은 bulk update 이후 관리 엔티티를 다시 변경하지 않으므로 전체 컨텍스트 clear의
부작용이 없다.

## 4. CommentValidator의 미사용 Post 인자

### Review

`validateForUpdate`와 `validateForDelete`가 전달받은 Post를 사용하지 않고
`comment.getPost()` 기반 공통 정책만 사용한다는 지적이다.

### Decision

미사용 인자를 제거한다. 서비스의 Post 조회는 게시글 생존 확인, 게시판 설정 조회,
차단 검증에 계속 사용한다. 함께 발견된 `CommentAuthorInfo.of` Javadoc 파라미터도 실제
시그니처에 맞춘다.

## 검증 상태

- 구현: 완료
- 관련 테스트: 108개 성공
- `app-main` 전체 테스트: 591개 성공
- Spotless 검사: 성공

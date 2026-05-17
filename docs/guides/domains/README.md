# 도메인 매트릭스

`app-main/src/main/java/net/causw/app/main/domain/` 하위 8개 도메인의 책임을 한눈에 정리한 매트릭스입니다.

## 도메인 한눈에 보기

| 도메인 | 책임 | 서브 도메인 | 주요 엔티티 | 문서 |
|--------|------|-------------|-------------|------|
| **user** | 사용자 계정 / 인증 / 학적 / 관계 | `account`, `auth`, `terms`, `academic`, `relation` | `User`, `UserInfo`, `UserAcademicRecord`, `UserBlock` | [user.md](./user.md) |
| **community** | 학우 간 커뮤니티(게시글/댓글/투표 등) | `post`, `comment`, `board`, `form`, `vote`, `reaction`, `report`, `ceremony`, `homepage` | `Post`, `Comment`, `Board`, `Vote`, `LikePost`, `Report` (통합) | [community.md](./community.md) |
| **campus** | 학사 / 동아리 / 행사 | `schedule`, `semester`, `circle`, `event` | `Schedule`, `Semester`, `Circle`, `CircleMember`, `Event` | [campus.md](./campus.md) |
| **finance** | 학생회비 | `usercouncilfee` | `UserCouncilFee`, `CouncilFeeFakeUser`, `UserCouncilFeeLog` | [finance.md](./finance.md) |
| **notification** | 알림 / 푸시 / 구독 | `notification` | `Notification`, `NotificationLog`, `UserNotificationSetting`, `UserPostSubscribe`, `UserCommentSubscribe`, `UserBoardSubscribe`, `CeremonyNotificationSetting` | [notification.md](./notification.md) |
| **integration** | 외부 시스템 연동 (크롤링 / 내보내기) | `crawled`, `export` | `CrawledNotice`, `CrawledPostImage`, `CrawledFileLink` | [integration.md](./integration.md) |
| **asset** | 파일 / 사물함 | `file`, `locker` | `UuidFile`, `Locker`, `LockerLog`, `LockerLocation` | [asset.md](./asset.md) |
| **etc** | 기능 플래그 / 정책 텍스트 / 외부 API 관리 | `flag`, `textfield`, `api` | `Flag`, `TextField` | [etc.md](./etc.md) |

## 도메인 의존 관계 (대략)

```
                  ┌── user ──────────────┐
                  ▲       (writer/author) │
                  │                       ▼
   campus ──── community ◄──── notification ──→ asset (file)
      ▲             ▲                      ▲
      │             │                      │
      finance     integration            etc
                  (crawled posts)
```

- 거의 모든 도메인이 `user` 에 의존합니다 (작성자 / 알림 수신자 등).
- `community` 의 일부 엔티티(`Post`, `Comment`)는 `asset/file` (`UuidFile`) 에 첨부 이미지로 의존합니다.
- `integration/crawled` 가 만든 데이터는 `community/post` 로 반영됩니다 (`Post.isCrawled`).
- `notification` 은 다양한 도메인 이벤트의 소비자입니다 (loose coupling, 이벤트 기반).

## 도메인 간 통신 원칙

[../conventions/service-layer.md](../conventions/service-layer.md) §7 참고.

- 다른 도메인의 데이터가 필요하면 **해당 도메인의 Reader 컴포넌트** 를 주입해 사용
- 다른 도메인의 Repository 를 직접 호출하지 않음
- 도메인 이벤트가 적절한 경우 `ApplicationEventPublisher` 로 발행하고 다른 도메인이 리스너로 구독

## 도메인 추가 / 변경 시

1. 적절한 상위 도메인 (asset/campus/community/etc/finance/integration/notification/user) 하위에 서브 도메인 패키지 생성
2. 표준 레이어 (api / service / repository / entity / enums) 적용 — [../architecture/package-structure.md](../architecture/package-structure.md)
3. 서브 도메인 단위로 `*ErrorCode` 추가 (필요 시) — [../conventions/exception.md](../conventions/exception.md)
4. 이 매트릭스와 개별 도메인 문서를 함께 갱신

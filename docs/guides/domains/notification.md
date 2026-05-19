# notification 도메인

위치: `app-main/src/main/java/net/causw/app/main/domain/notification`

## 책임

**사용자 알림(인앱/푸시) 발송 및 구독 설정** 을 책임집니다. 다른 도메인의 이벤트를 받아 알림을 만들고 발송합니다.

## 서브 도메인

```
domain/notification/
└── notification/
    ├── api/         # 알림 조회, 설정 변경 API
    ├── entity/      # Notification, NotificationLog, *Subscribe 등
    ├── enums/       # NotificationType 등
    ├── event/       # 이벤트 발행 / 리스너
    ├── repository/
    ├── service/
    │   ├── handler/         # 이벤트 → 알림 변환 핸들러
    │   ├── implementation/  # Reader / Writer / 발송 로직
    │   ├── dto/
    │   ├── mapper/
    │   └── v1/
    └── util/
```

## 엔티티

`notification/entity/` 하위:

| 엔티티 | 역할 |
|--------|------|
| `Notification` | 사용자에게 발송된 알림 (인앱 표시용) |
| `NotificationLog` | 발송 결과 / 실패 / 재시도 로그 |
| `UserNotificationSetting` | 사용자별 알림 종류 on/off 설정 |
| `UserPostSubscribe` | 게시글 단위 구독 (특정 글 알림) |
| `UserCommentSubscribe` | 댓글 단위 구독 (대댓글 알림) |
| `UserBoardSubscribe` | 게시판 단위 구독 |
| `CeremonyNotificationSetting` | 경조사 알림 수신 설정 |

## enum

`notification/enums/` 하위:
- `NotificationType` — 댓글 알림 / 좋아요 알림 / 공지 알림 등 종류

## 발송 채널

| 채널 | 인프라 |
|------|--------|
| 인앱 알림 | `Notification` 엔티티 저장 후 클라이언트가 조회 |
| 푸시 (모바일) | Firebase Cloud Messaging (FCM) |
| 이메일 | Spring Mail (`shared/infra/mail/`) |
| 디스코드 | 로그 / 운영 알림용 (`logback-discord-appender`) |

외부 인프라 클라이언트: [../cross-cutting/infrastructure.md](../cross-cutting/infrastructure.md).

## 이벤트 기반 흐름

1. 다른 도메인이 `ApplicationEventPublisher` 로 도메인 이벤트 발행
   - 예: `PostCommentedEvent`, `PostLikedEvent`
2. notification 의 `service/handler/` 의 `@EventListener` / `@TransactionalEventListener` 가 구독
3. `UserNotificationSetting` / `*Subscribe` 확인 후 발송 채널 결정
4. `Notification` 저장 + 채널별 발송 + `NotificationLog` 기록

## API

`notification/api/v{n}/` 에 위치. 대표 기능:
- 사용자 알림 목록 조회
- 알림 읽음 처리 / 일괄 읽음
- 알림 설정 (종류별 on/off)
- 디바이스 토큰 등록 / 해제 (푸시용)

## ErrorCode

- `NotificationLogErrorCode`
- `NotificationSettingErrorCode`

## 외부 도메인 의존

| 의존 도메인 | 사용 |
|------------|------|
| user/account | 수신자 `User`, 알림 설정 보유자 |
| community | 댓글/좋아요/신고 등 이벤트 소스 |
| campus | 일정/행사 알림 소스 |
| shared/infra/firebase | FCM 클라이언트 |
| shared/infra/mail | 이메일 발송 |
| shared/infra/push | 푸시 통합 인터페이스 |

## 주의 사항

- 알림은 **비동기 발송** 이 원칙 (외부 API 지연으로 메인 흐름 지연 방지)
- 트랜잭션 커밋 이후 발송이 안전 — `@TransactionalEventListener(phase = AFTER_COMMIT)` 권장
- 실패 시 `NotificationLog` 에 기록 (`spring-retry` 의존성은 추가되어 있으나 현재 `@EnableRetry`/`@Retryable` 미사용 — 재시도 정책이 필요하면 활성화부터 검토)
- FCM 토큰 만료 / 무효 응답 처리 — 만료 토큰은 즉시 제거해야 다음 발송에서 비용 낭비 방지
- 외부 도메인은 notification 의 내부 엔티티에 직접 접근하지 말고 이벤트 발행으로만 호출
- 구독 엔티티 (`UserPostSubscribe`, `UserCommentSubscribe`, `UserBoardSubscribe`) 는 사용자별로 빠르게 누적될 수 있으므로 인덱스 / 정리 정책 점검

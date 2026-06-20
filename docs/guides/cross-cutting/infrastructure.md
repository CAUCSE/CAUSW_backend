# 외부 인프라

`shared/infra/` 와 일부 `core/config/` 에 위치한 외부 의존 인프라(Redis, S3, Mail, FCM) 설정입니다.

## 1. Redis

용도:
- Refresh Token 저장 (인증 도메인)
- 캐시 (게시판 / 카테고리 등 자주 조회되는 데이터)
- 분산 락 (필요 시)

라이브러리: `spring-boot-starter-data-redis`

위치: `shared/infra/redis/`
- 설정 Bean: `RedisConfig` (Lettuce 클라이언트, `RedisTemplate`)
- 유틸: `RedisUtils` (자주 쓰는 GET / SET / TTL / DEL 헬퍼)

환경 변수: `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`
- 로컬: docker-compose 또는 별도 설치
- 운영: AWS ElastiCache 등 매니지드 서비스 권장

환경변수 운영: [../../env_guide.md](../../env_guide.md).

## 2. S3 (파일 저장소)

용도:
- 사용자 업로드 이미지, 첨부 파일 등 영구 저장

라이브러리: `spring-cloud-starter-aws 2.x` (다소 오래된 버전)

위치:
- `shared/infra/storage/` — S3 클라이언트 / 유틸
- `shared/storage/` — 파일 스토리지 추상화 (`StorageClient` 인터페이스 + `S3StorageClient`/`LocalStorageClient` 구현체, `UuidFile` 도메인과 연결)

흐름:
1. Controller 가 `MultipartFile` 수신
2. 저장 유틸로 S3 업로드 (키는 UUID 기반)
3. `UuidFile` 엔티티에 메타 저장
4. 사용처에서 조인 엔티티 (예: `PostAttachImage`) 로 연결

응답 URL 변환:
- `shared/dto/util/dtoMapper/custom/UuidFileToUrlDtoMapper` 가 S3 키 → public/CDN URL 변환

환경 변수: `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_REGION`, `S3_BUCKET_NAME` 등
- EC2 메타데이터 비활성화: `CauswApplication` 의 static initializer 에서 `com.amazonaws.sdk.disableEc2Metadata=true` 설정

`spring-cloud-starter-aws 2.x` 는 오래된 메이저 버전입니다. AWS SDK v2 / Spring Cloud AWS 3.x 마이그레이션을 시도하기 전 호환성 / 마이그레이션 비용 평가가 필요합니다.

## 3. Mail

용도:
- 인증 메일 (가입, 비밀번호 재설정)
- 이벤트 기반 알림 메일

라이브러리: `spring-boot-starter-mail`

위치: `shared/infra/mail/`
- `GoogleMailSender` — Gmail SMTP 사용 래퍼
- `event/MailEventListener` — 도메인 이벤트 받아 메일 발송 (`EmailVerificationEvent`, `FindPasswordEvent`, `PasswordResetCodeEvent` 등)

환경 변수: `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`
- Gmail SMTP 사용 시 앱 비밀번호 발급 필요

비동기 처리:
- `@Async` 메서드로 메일 발송 (메인 흐름 블로킹 방지)
- 트랜잭션 커밋 이후에 발송 (`@TransactionalEventListener(phase = AFTER_COMMIT)`)

## 4. FCM (Firebase Cloud Messaging)

용도:
- 모바일 앱 푸시 알림

라이브러리: `firebase-admin`

위치:
- `shared/infra/firebase/` — Firebase SDK 초기화, FCM 메시지 전송
- `shared/infra/push/` — 푸시 전송 인터페이스 (FCM 외 다른 채널 확장 여지)

환경 변수 / 비밀키:
- Firebase 서비스 계정 JSON (보통 환경변수 또는 마운트 파일)

흐름:
1. notification 도메인이 발송 결정
2. 사용자 디바이스 토큰 (FCM 토큰) 조회 — `UserNotificationSetting` 또는 별도 토큰 테이블
3. Firebase SDK 로 메시지 전송
4. 실패 응답 (`UNREGISTERED`, `INVALID_ARGUMENT`) 시 토큰 제거

## 5. DataSource 프록시

라이브러리: `datasource-proxy-spring-boot-starter`

용도: 실행 SQL / 파라미터 / 실행 시간 로깅 + 요청별 쿼리 카운트

위치: `core/datasourceProxy/`

상세: [observability.md](./observability.md) §6.

## 6. Retry (의존성만 있고 미활성화)

라이브러리: `spring-retry` (build.gradle 에 추가되어 있음)

**현재 상태**: `@EnableRetry` / `@Retryable` 사용처가 코드에 없음 — 즉, 의존성은 있으나 **활성화되어 있지 않습니다.**

외부 API 호출(이메일, FCM, 크롤링 등) 자동 재시도가 필요해지면 다음 단계가 필요합니다.

1. `core/config/` 의 설정 클래스 어딘가에 `@EnableRetry` 추가
2. 재시도가 필요한 메서드에 `@Retryable(value = ..., maxAttempts = ..., backoff = ...)` 적용
3. 실패 시 복구 로직은 `@Recover` 메서드

새로 도입할 때는 무한 재시도 / 결제처럼 부작용이 있는 호출 / 재시도 가능 예외 분류를 함께 설계해야 합니다.

## 7. spring-dotenv

라이브러리: `me.paulschwarz:spring-dotenv`

용도: `.env` 파일을 Spring `Environment` 로 자동 로딩

- 로컬 실행 시 루트 `.env` 자동 적용
- application.yml 의 `${VAR}` placeholder 가 `.env` 값으로 채워짐

환경변수 운영 흐름: [../../env_guide.md](../../env_guide.md).

## 8. 외부 의존성 추가 시 가이드

1. **라이브러리 추가** — `app-main/build.gradle` 에 의존성 등록 + 사유 주석
2. **설정 Bean 클래스 위치** — `core/config/` 또는 `shared/infra/{대상}/`
3. **환경 변수 분리** — 키/엔드포인트는 `application-{profile}.yml` 의 `${VAR}` placeholder 로
4. **`.env.example` 갱신** — 키 템플릿 추가 + 커밋
5. **Notion `.env (local/dev/prod)` 버전 기록 추가** — 운영 흐름 따름
6. **로컬 fallback** — 외부 의존이 없을 때도 부팅이 가능하도록 `@ConditionalOnProperty` 등으로 옵셔널 처리 검토
7. **에러 핸들링** — 외부 호출 실패는 `WARN` + (필요 시) 재시도, 영구 실패는 `ERROR` + 알림

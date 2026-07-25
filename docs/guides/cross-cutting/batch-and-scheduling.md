# 배치와 스케줄링

주기적 / 비동기 백그라운드 작업 처리에 대한 가이드입니다.

## 1. 구성 요소

| 구성 | 위치 |
|------|------|
| Spring Batch | `core/batch/`, `core/config/batch/` |
| 스케줄링 | `core/config/scheduling/SchedulingConfig` (`@EnableScheduling`) |
| 비동기 | `core/config/async/AsyncConfig` (`@EnableAsync`) |
| 재시도 | `spring-retry` 의존성만 추가됨 — 현재 미활성화 (§6) |

## 2. 현재 운영 중인 배치 / 스케줄링 작업

| 이름 | 트리거 | 위치 | 설명 |
|------|--------|------|------|
| `cleanUpUnusedFilesJob` | cron `0 0 3 1 * ?` (매달 1일 03:00) — `BatchScheduler.scheduleCleanUpJob` 이 `JobLauncher` 로 기동 | `core/batch/BatchScheduler`, Job 정의: `core/config/batch/CleanUnusedUuidFilesBatchConfig` | 미사용 UUID 첨부파일 정리. init → 게시글/학적증명/입회신청/입회로그/프로필이미지 사용여부 체크 5단계 → 미사용 파일 삭제, 총 7-Step Job |
| 탈퇴 유저 후처리 | cron `0 10 3 * * ?` (매일 03:10) | `core/batch/BatchScheduler#scheduleCleanupDeactivatedUsers` | Spring Batch Job 아님(일반 `@Scheduled`). `deletedAt` 30일 경과 탈퇴/추방 유저를 페이지 단위로 조회 후 프로필이미지/유저정보/세레모니/소셜계정/입회정보 삭제 및 유저 cleanup |
| 크롤링 수집 | cron `0 0 */1 * * *` (매시 정각) | `domain/integration/crawled/service/CrawlingScheduler#crawlAndSave` | 외부 게시판 크롤링 + 변경 감지 |
| 크롤링 → 게시글 변환 | cron `0 5 */1 * * *` (매시 5분) | `CrawlingScheduler#transferToPosts` | 크롤링 결과를 내부 게시글로 변환 (수집 5분 뒤 실행되도록 오프셋) |
| 크롤링 즉시 실행 | `ApplicationReadyEvent`, `local` 프로필 한정 | `CrawlingScheduler#onApplicationStart` | 로컬 개발 시 기동 즉시 크롤링→변환을 1회 수행해 cron 대기 없이 데이터 확인 |
| 공휴일 동기화(월간) | cron `0 0 0 1 * *`, zone `Asia/Seoul` (매달 1일 00:00) | `domain/campus/schedule/service/HolidayScheduleSyncService#syncMonthly` | 공공 API에서 올해/내년 공휴일을 조회해 `Schedule`(HOLIDAY) 로 upsert (중복/기존 존재 시 skip) |
| 공휴일 동기화(기동 시) | `ApplicationReadyEvent`, `@Order(1)` | `HolidayScheduleSyncService#syncOnApplicationReady` | 앱 기동 직후 공휴일 동기화 1회 수행 (월간 cron 도달 전 공백 방지) |

비활성/보류:
- `spring-retry` — 의존성만 존재, `@EnableRetry`/`@Retryable` 사용처 없음 (§6)
- `CleanUnusedUuidFilesBatchConfig` 내 `RetryTemplate` 빈은 정의돼 있으나 현재 Step 에서 참조하지 않음 — 실질 미사용

## 3. Spring Batch

라이브러리: `spring-boot-starter-batch`, `spring-batch-core`

위치 (배치 설정): `core/config/batch/`, `core/batch/`

용도 (대표 예):
- 미사용 파일 정리 — `CleanUnusedUuidFilesBatchConfig`
- 통계 집계 / 데이터 백필 / 일괄 변경 등

기본 구조:
```java
@Configuration
@RequiredArgsConstructor
public class CleanUnusedUuidFilesBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager txManager;

    @Bean
    public Job cleanUnusedUuidFilesJob(Step step) {
        return new JobBuilder("cleanUnusedUuidFilesJob", jobRepository)
            .start(step)
            .build();
    }

    @Bean
    public Step cleanUnusedUuidFilesStep(/* reader/processor/writer 등 */) {
        return new StepBuilder("cleanUnusedUuidFilesStep", jobRepository)
            .<UuidFile, UuidFile>chunk(100, txManager)
            .reader(...)
            .processor(...)
            .writer(...)
            .build();
    }
}
```

규칙:
- 잡 메타 테이블은 자동 생성 (Spring Batch 기본)
- chunk 크기는 메모리/DB 부하 균형에서 결정 (보통 100~1000)
- 트랜잭션 매니저: 동일 DB 사용 시 JPA `PlatformTransactionManager`

## 4. 스케줄링 (`@Scheduled`)

활성화: `core/config/scheduling/SchedulingConfig` 의 `@EnableScheduling`

대표 예: `domain/integration/crawled/service/CrawlingScheduler` — 매시 정각에 크롤링, 5분 후 게시글 변환을 수행합니다.

```java
@Service
@RequiredArgsConstructor
public class CrawlingScheduler {

    private final CrawlingAndSavingService crawlingAndSavingService;
    private final CrawledToPostTransferService crawledToPostTransferService;

    @Scheduled(cron = "0 0 */1 * * *") // 매 1시간 (정시)
    public void crawlAndSave() {
        crawlingAndSavingService.crawlAndDetectUpdates();
    }

    @Scheduled(cron = "0 5 */1 * * *") // 매 1시간 5분 후
    public void transferToPosts() {
        crawledToPostTransferService.transferToPosts();
    }
}
```

규칙:
- 신규 cron 에는 `zone = "Asia/Seoul"` 명시를 권장 (예: `campus/schedule/service/HolidayScheduleSyncService`). 다만 기존 스케줄러 다수는 명시 없이 앱 전역 타임존(`Asia/Seoul`)에 의존하고 있어 일관 적용된 규칙은 아닙니다
- 스케줄러 메서드는 가능한 한 짧게 — 실제 작업은 별도 서비스 / 배치 잡에 위임
- 동일 잡 중복 실행 방지가 필요하면 `@SchedulerLock` (ShedLock) 또는 분산 락 검토

## 5. 비동기 (`@Async`)

활성화: `core/config/async/AsyncConfig` 의 `@EnableAsync` + Executor Bean

```java
@Service
@RequiredArgsConstructor
public class MailEventListener {

    private final JavaMailSender mailSender;

    @Async("mailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserSignedUp(UserSignedUpEvent event) {
        // 비동기 + 트랜잭션 커밋 이후
        mailSender.send(...);
    }
}
```

규칙:
- 비동기 메서드는 별도 트랜잭션 컨텍스트에서 실행됨 — `@Transactional` 필요 시 명시
- 메일/푸시 같은 외부 호출은 `@Async` 권장
- 예외 발생 시 호출자에게 전파되지 않음 — Future 반환 또는 별도 로깅/알림 필수

## 6. 재시도 (`spring-retry`)

**현재 상태**: 의존성은 추가되어 있지만 `@EnableRetry` / `@Retryable` 사용처가 없습니다.

활성화 절차 / 도입 시 고려 사항: [infrastructure.md](./infrastructure.md) §6.

## 7. 배치 잡 추가 가이드

1. `core/batch/{잡명}/` 또는 `core/config/batch/` 에 설정 클래스 생성
2. `@Configuration` 클래스에 `Job`, `Step`, `Reader/Processor/Writer` Bean 정의
3. 트리거 방식 결정:
   - 시간 기반 → `@Scheduled` + `JobLauncher` 호출
   - API 트리거 → 관리자 Controller 추가
4. 실패 시 알림 — `JobExecutionListener` 또는 ERROR 레벨 로깅
5. 멱등성 보장 — 같은 데이터에 두 번 실행해도 안전하게 동작하도록 설계
6. 운영 환경에서만 활성화할 거라면 `@Profile("prod")` 또는 `@ConditionalOnProperty`

## 8. 운영 / 모니터링

- 잡 실행 결과는 Spring Batch 메타 테이블 (`BATCH_JOB_EXECUTION`, `BATCH_STEP_EXECUTION` 등) 에 저장됨
- 실패 시 ERROR 레벨 로그 (스택트레이스 포함)
- 장시간 잡은 분할 / chunk 처리 / heartbeat 로그로 진행률 노출
- 동일 잡 동시 실행 방지: Spring Batch 기본은 동일 파라미터 동시 실행 불가, 다른 파라미터로는 허용됨 → 정책에 맞춰 설계

## 9. 시간대 주의

- 모든 cron / 일정 / 로그 시각은 **`Asia/Seoul`** (`CauswApplication` 의 `init()` 이 강제)
- DB 의 `created_at`, `updated_at` 도 Asia/Seoul 기준
- 외부 시스템 (FCM 등) 과 연동할 때 UTC 변환 / 타임존 정합성 확인

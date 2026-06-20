# 횡단 관심사 (Cross-cutting)

특정 도메인에 속하지 않고, 여러 도메인에 걸쳐 영향을 주는 인프라 / 플랫폼 관심사를 정리합니다.

## 문서

| 주제 | 문서 |
|------|------|
| Spring Security, JWT, OAuth2, 권한 | [security-and-auth.md](./security-and-auth.md) |
| 로깅, MDC, AOP, Discord 알림 | [observability.md](./observability.md) |
| Redis, S3, Mail, FCM, DataSource 프록시 | [infrastructure.md](./infrastructure.md) |
| Spring Batch, `@Scheduled`, `@Async` | [batch-and-scheduling.md](./batch-and-scheduling.md) |

## 코드 위치 한눈에 보기

```
app-main/src/main/java/net/causw/app/main/
├── core/
│   ├── aop/                # AOP (LogAspect, @MeasureTime)
│   ├── batch/              # Spring Batch 잡
│   ├── config/
│   │   ├── async/          # 비동기 설정
│   │   ├── batch/          # 배치 설정
│   │   ├── flyway/         # Flyway 설정
│   │   ├── persistence/    # JPA / Audit / DataSource
│   │   ├── querydsl/       # JPAQueryFactory
│   │   ├── scheduling/     # @Scheduled 활성화
│   │   └── swagger/        # OpenAPI
│   ├── datasourceProxy/    # SQL 로깅
│   ├── filter/             # Servlet Filter
│   └── security/           # Spring Security, JWT, OAuth2
└── shared/
    ├── infra/
    │   ├── firebase/       # FCM
    │   ├── mail/           # Mail
    │   ├── push/           # 푸시 인터페이스
    │   ├── redis/          # Redis
    │   └── storage/        # S3
    └── storage/
        ├── StorageClient    # 파일 스토리지 추상화 인터페이스
        └── S3StorageClient, LocalStorageClient  # 구현체
```

## 설계 원칙

- **도메인 코드가 인프라를 직접 알지 않게** — `shared/infra/` 의 추상 인터페이스 또는 Spring 표준 추상(`JavaMailSender` 등) 을 통해 사용
- **설정은 `core/config` 에 집중** — `@Bean`, `@Enable*` 어노테이션은 한 곳에서 관리
- **AOP / Filter / Interceptor 는 신중히** — 동작 흐름이 분산되므로, 추가 시 README 또는 본 문서를 갱신

# 횡단 관심사 (Cross-cutting)

특정 도메인에 속하지 않고, 여러 도메인에 걸쳐 영향을 주는 인프라 / 플랫폼 관심사를 정리합니다.

## 문서

| 주제 | 문서 |
|------|------|
| Spring Security, JWT, OAuth2, 권한 | [security-and-auth.md](./security-and-auth.md) |
| 로깅, MDC, AOP | [observability.md](./observability.md) |
| Redis, S3, Mail, FCM, DataSource 프록시 | [infrastructure.md](./infrastructure.md) |
| Spring Batch, `@Scheduled`, `@Async` | [batch-and-scheduling.md](./batch-and-scheduling.md) |

`core/`, `shared/` 패키지 맵은 [../architecture.md](../architecture.md) §4 참조 (중복 관리 방지를 위해 여기서는 별도로 나열하지 않음).

## 설계 원칙

- **도메인 코드가 인프라를 직접 알지 않게** — `shared/infra/` 의 추상 인터페이스 또는 Spring 표준 추상(`JavaMailSender` 등) 을 통해 사용
- **설정은 `core/config` 에 집중** — `@Bean`, `@Enable*` 어노테이션은 한 곳에서 관리
- **AOP / Filter / Interceptor 는 신중히** — 동작 흐름이 분산되므로, 추가 시 README 또는 본 문서를 갱신

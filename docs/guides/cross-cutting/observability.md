# 관측성 / 로깅

운영 환경에서 시스템이 어떻게 동작 중인지 파악하기 위한 로깅, AOP 측정, 모니터링 구성입니다.

## 1. 로깅 스택

| 구성 | 라이브러리 |
|------|----------|
| 로깅 추상 | SLF4J |
| 구현체 | Logback (Spring Boot 기본) |
| JSON 포맷 | `logback-json-classic`, `logback-jackson` |
| 메트릭 | Spring Boot Actuator + Micrometer Prometheus |

설정 파일: `app-main/src/main/resources/logback-spring.xml`

## 2. 로그 레벨 정책

| 레벨 | 사용 시점 |
|------|----------|
| DEBUG | 메서드 실행 시간, 상세 흐름 (개발 / 임시 디버깅) |
| INFO | 정상 흐름 중 의미 있는 이벤트 (가입, 결제, 배포 등) |
| WARN | 4xx 클라이언트 에러, 외부 의존성 일시 실패 |
| ERROR | 5xx 서버 에러, 복구 불가, 외부 의존성 영구 실패 + 스택트레이스 |

## 3. 글로벌 예외 핸들러의 로깅

`shared/exception/GlobalV2ExceptionHandler` 내부 헬퍼 메서드 (`logException`) 가 status 별로 다른 레벨로 로깅합니다.

- 4xx → WARN (스택트레이스 없이)
- 5xx → ERROR (스택트레이스 포함)

## 4. AOP — `@MeasureTime`

위치:
- `core/aop/LogAspect`
- `core/aop/annotation/MeasureTime`

용도: 클래스에 `@MeasureTime` 어노테이션을 붙이면, 해당 클래스의 모든 메서드 실행 시간을 측정/로깅합니다.

```java
@MeasureTime
@Service
public class PostService { ... }
```

내부 동작:
- AspectJ `@Around` 로 메서드 실행을 감쌈
- `StopWatch` 로 실행 시간 측정
- `MDC` 에 `methodName`, `executionTimeMs` 기록 → JSON 로그 필드로 포함
- DEBUG 레벨로 출력

운영에서 DEBUG 가 비활성화되어 있으면 로그 라인은 출력되지 않습니다. MDC 필드로 활용하려면 `logback-spring.xml` 의 JSON appender 설정에서 MDC 필드 포함 여부 확인이 필요합니다.

## 5. MDC (Mapped Diagnostic Context)

분산 로그에서 요청 흐름을 추적하기 위한 컨텍스트.

기본 활용:
- `methodName`, `executionTimeMs` — `LogAspect` 가 채움
- 추가 컨텍스트 (요청 ID, 사용자 ID 등) 는 Filter / Interceptor 에서 채울 수 있음 (현재 기본 적용 여부는 `core/filter/` 확인)

## 6. DataSource 프록시 — SQL 로깅 / 카운트

라이브러리: `datasource-proxy-spring-boot-starter`

위치: `core/datasourceProxy/`
- `ApiQueryCountListener` — 요청별 쿼리 카운트 누적
- `ApiQueryLoggingAspect` — SQL 로깅 AOP
- `QueryContext` — 요청 컨텍스트 보관

용도:
- 실행된 SQL / 파라미터 / 실행 시간 로깅
- N+1 쿼리 탐지에 유용
- 활성화 조건은 application yml 에 따라 다름 — 로컬에서는 보통 활성화, 운영에서는 비활성화 또는 sampling

## 7. Prometheus / Actuator

- `spring-boot-starter-actuator` + `micrometer-registry-prometheus` 의존성
- `/actuator/health`, `/actuator/info`, `/actuator/prometheus` 등 노출
- 노출 범위는 `application.yml` 의 `management.endpoints.web.exposure.include` 로 제어
- 운영에서는 IP / 인증 제한 (보안 필터 또는 별도 포트)

루트 `monitoring/` 디렉터리 — Grafana 대시보드 / Prometheus 설정 모음 (운영 인프라용)

## 8. 보안 / 개인정보 로깅 주의

❌ **로그에 절대 출력하지 말 것**:
- 비밀번호 (해시 포함)
- JWT (Access / Refresh)
- 카드 정보 / 결제 정보
- 학번 / 전화번호 / 이메일 원본 (필요 시 마스킹)

마스킹 유틸은 `domain/user/account/util/masking/` 참고 (예: `EmailMasker`).

## 9. 로깅 / 메트릭 체크리스트

- [ ] 새 비즈니스 이벤트는 적절한 레벨로 로깅 (INFO 권장)
- [ ] 외부 API 호출 / 재시도는 WARN
- [ ] 예외는 GlobalExceptionHandler 에 위임 (직접 catch 후 로깅 지양)
- [ ] 민감 정보는 마스킹
- [ ] 신규 메트릭은 Micrometer `MeterRegistry` 로 등록

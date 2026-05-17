# CAUSW Backend 가이드 문서

CAUSW Backend (중앙대학교 소프트웨어학부 동문네트워크 백엔드, Spring Boot 3.2 / Java 17) 의 아키텍처, 코드 컨벤션, 도메인별 책임을 정리한 문서 모음입니다.

신규 기여자와 AI 코딩 어시스턴트가 프로젝트 구조와 관례를 빠르게 파악하기 위한 레퍼런스로 사용합니다.

## 카테고리

| 카테고리 | 설명 |
|---------|------|
| [architecture/](./architecture/) | 모듈 구성, 패키지 레이어, 의존성 흐름 등 전반적인 아키텍처 |
| [conventions/](./conventions/) | API, 서비스, 영속화, 예외, 코드 스타일 등 코드 컨벤션 |
| [domains/](./domains/) | `app-main/domain` 하위 8개 도메인의 책임/주요 엔티티 |
| [cross-cutting/](./cross-cutting/) | 보안, 관측성, 외부 인프라, 배치 등 횡단 관심사 |

## 빠른 시작

- 프로젝트 개요: 루트의 [readme.md](../../readme.md)
- 기여 가이드: 루트의 [CONTRIBUTING.md](../../CONTRIBUTING.md)
- 환경 변수 / `.env` 운영: [docs/env_guide.md](../env_guide.md)
- DB 마이그레이션 (Flyway): [docs/flyway_guide.md](../flyway_guide.md)

## 문서 인덱스

### Architecture

- [개요와 레이어 설계](./architecture/README.md)
- [패키지 구조](./architecture/package-structure.md)

### Conventions

- [API 레이어 컨벤션](./conventions/api-layer.md)
- [서비스 레이어 컨벤션](./conventions/service-layer.md)
- [영속화 컨벤션](./conventions/persistence.md)
- [예외 처리 컨벤션](./conventions/exception.md)
- [코드 스타일](./conventions/code-style.md)

### Domains

- [도메인 매트릭스](./domains/README.md)
- [user](./domains/user.md) — 계정 / 인증 / 학적 / 약관 / 관계
- [community](./domains/community.md) — 게시판 / 게시글 / 댓글 / 투표 / 신고
- [campus](./domains/campus.md) — 학사일정 / 학기 / 동아리 / 행사
- [finance](./domains/finance.md) — 학생회비 관리
- [notification](./domains/notification.md) — 알림 / 푸시
- [integration](./domains/integration.md) — 외부 크롤링 / 내보내기
- [asset](./domains/asset.md) — 파일 / 사물함
- [etc](./domains/etc.md) — 플래그 / 텍스트 필드 / 외부 API 호출 관리

### Cross-cutting

- [보안과 인증](./cross-cutting/security-and-auth.md)
- [관측성 / 로깅](./cross-cutting/observability.md)
- [외부 인프라 (Redis / S3 / Mail / FCM)](./cross-cutting/infrastructure.md)
- [배치와 스케줄링](./cross-cutting/batch-and-scheduling.md)

## 문서 작성 규칙

- 모든 가이드는 한국어로 작성합니다.
- **파일경로:라인번호** 형식의 인용은 사용하지 않습니다 — 라인이 바뀌면 문서가 빠르게 노후화됩니다. 대신 "**어떤 디렉터리에 어떤 명명 패턴의 클래스로 존재하는지**" 를 설명하세요 (예: `core/security/` 의 `*FilterChain*` Bean, `service/implementation/` 의 `*Reader`/`*Writer` 컴포넌트).
- 클래스명 / 패키지명을 인용할 때는 `.java` 확장자 없이 표기합니다 (`PostController`, `core/security/WebSecurityConfig`).
- 코드 컨벤션이 변경되면 해당 문서를 함께 갱신하는 것을 원칙으로 합니다.

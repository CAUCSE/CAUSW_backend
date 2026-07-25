# CAUSW Backend

[![CI - Develop](https://github.com/CAUCSE/CAUSW_backend/actions/workflows/dev-ci.yml/badge.svg)](https://github.com/CAUCSE/CAUSW_backend/actions/workflows/dev-ci.yml)
[![CI - Production](https://github.com/CAUCSE/CAUSW_backend/actions/workflows/main-ci.yml/badge.svg)](https://github.com/CAUCSE/CAUSW_backend/actions/workflows/main-ci.yml)

중앙대학교 소프트웨어학부 동문네트워크 커뮤니티([causw.co.kr](https://causw.co.kr)) 서비스의 Backend 입니다.

서비스 이용 중 불편한 사항 혹은 문의사항이 있으신 경우 개발팀에 연락 부탁드리며, 서비스 개선을 위한 다양한 의견은 언제든 환영입니다.

프로젝트에 참여하시고 싶으시다면, [Contributing Guide](CONTRIBUTING.md)를 참조하시어 issue 혹은 pull request를 생성해주세요!

## 주요 기능

- **커뮤니티**: 게시판/게시글/댓글/대댓글, 투표, 좋아요·즐겨찾기, 신고, 경조사(결혼/장례 등) 공지
- **사용자**: 자체 가입 + Google/Kakao/Apple OAuth2 로그인, 학적 인증, 약관 동의, 차단 관계 관리
- **애셋**: 파일 업로드(S3), 사물함 신청/관리
- **캠퍼스**: 학사 일정 관리
- **알림**: 이벤트 기반 푸시 알림(FCM) 발송/구독 설정
- **연동**: 외부 공지사항 크롤링 → 게시글 자동 변환
- **관리자**: 운영자 로깅 기능

## Tech Stack

- Java 25, Spring Boot 4.0
- JPA / QueryDSL, MySQL, Flyway
- Spring Security, JWT, OAuth2(Google/Kakao/Apple)
- Redis, S3, Firebase(FCM), Spring Batch

## Architecture

멀티 모듈(`global`, `app-main`) 구성이며, 코드 구조는 도메인 중심 레이어드 패턴(`api → service → repository → entity`)을 따릅니다.

자세한 아키텍처/컨벤션 문서: [docs/guides/](docs/guides/README.md)

## Getting Started

### 요구 사항

- JDK 25
- MySQL
- Redis
- (선택) AWS S3, Firebase, Gmail SMTP, OAuth2 클라이언트 키 — 해당 기능 사용 시에만 필요

### 실행

```bash
# .env 준비 (.env.example 참고)
cp .env.example .env

# 빌드
./gradlew clean build

# 로컬 실행 (application-local.yml + .env 로딩)
./gradlew :app-main:bootRun

# 테스트
./gradlew test
```

- 환경 변수 / `.env` 운영: [docs/env_guide.md](docs/env_guide.md)
- DB 마이그레이션(Flyway): [docs/flyway_guide.md](docs/flyway_guide.md)

### API 문서

서버 실행 후 Swagger UI 에서 확인할 수 있습니다 (`/swagger-ui/index.html`). `local` 은 인증 없이 전면 허용, `prod` 는 전면 차단(403)되며, 그 외(dev 등) 환경은 폼 로그인(`SWAGGER_USERNAME` / `SWAGGER_PASSWORD`)으로 보호됩니다.

## CI/CD

- `dev` 브랜치로의 PR → `dev-ci` 가 빌드/테스트 실행, `dev` 브랜치 merge(push) → `dev-cd` 가 개발 서버에 배포
- `main` 브랜치로의 PR → `main-ci` 가 빌드/테스트 실행, `main` 브랜치 merge(push) → `main-cd` 가 운영 서버에 배포
- 워크플로 정의: [.github/workflows/](.github/workflows/)

## Contact

**Email** : <a href="mailto:caucsedongne@gmail.com">caucsedongne@gmail.com</a>

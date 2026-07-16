# 경량 Flyway 마이그레이션 이미지 설계

## 목적

dev CD에서 Gradle builder 전체를 마이그레이션 이미지로 배포하는 구조를 공식 Flyway CLI 기반 이미지로 교체한다. Private RDS 접근은 기존처럼 EC2 내부 실행으로 유지하면서 이미지 크기, pull 시간, Gradle cold start를 줄인다.

## 현재 문제

- `Dockerfile`의 `builder` stage에는 JDK, Gradle, 전체 소스와 빌드 결과가 포함된다.
- dev CD는 이 stage를 migration 이미지로 push하고 EC2에서 애플리케이션 이미지와 함께 pull한다.
- EC2에서 `flywayRepair`와 `flywayMigrate`를 각각 Gradle로 실행하므로 이미지 pull과 Gradle 초기화 시간이 누적된다.
- `flywayRepair || true`는 schema history를 변경하면서 실패 상태를 숨길 수 있다.
- 현재 배포 이미지 정리 로직은 같은 ECR 저장소의 migration 이미지도 삭제할 수 있어 Flyway 기반 레이어 캐시를 잃을 수 있다.

## 선택한 접근

기존 `Dockerfile`에 `flyway/flyway:12.4.0-alpine` 기반의 `migration` stage를 추가한다. 프로젝트에서 사용하는 Flyway Gradle 플러그인 및 MySQL 모듈과 버전을 맞추고, migration SQL, 공통 dev 설정 파일과 환경변수 매핑 entrypoint만 추가한다.

dev 환경의 Flyway 동작 옵션은 `app-main/gradle/flyway/flyway-dev.conf`를 단일 원본으로 사용한다. Gradle 플러그인과 migration 이미지가 같은 설정 파일을 읽어 Dockerfile 또는 workflow에 dev 옵션을 중복 선언하지 않는다.

다른 접근은 선택하지 않는다.

- 공식 이미지를 직접 실행하고 SQL을 SCP로 전달하는 방식은 이미지와 SQL이 하나의 immutable artifact로 묶이지 않는다.
- Flyway CLI를 직접 다운로드해 커스텀 JRE 이미지에 설치하는 방식은 체크섬, 의존성, 업그레이드 유지보수를 프로젝트가 부담한다.

## 이미지 구성

`migration` stage는 다음 요소만 포함한다.

- base image: `flyway/flyway:12.4.0-alpine`
- migration SQL: `app-main/src/main/resources/db/migration`을 `/flyway/migrations`로 복사
- 공통 dev 설정: `app-main/gradle/flyway/flyway-dev.conf`를 `/flyway/conf/flyway-dev.conf`로 복사
- entrypoint: `docker/flyway/entrypoint.sh`

entrypoint는 기존 `.env`의 다음 값을 검증하고 Flyway 표준 환경변수로 변환한다.

- `DB_URL` -> `FLYWAY_URL`
- `DB_USERNAME` -> `FLYWAY_USER`
- `DB_PASSWORD` -> `FLYWAY_PASSWORD`

DB 비밀번호는 CLI 인자로 전달하거나 로그에 출력하지 않는다. 필수 환경변수가 없으면 migration을 시작하지 않고 non-zero로 종료한다.

## Flyway 설정

기존 Gradle dev 설정을 `app-main/gradle/flyway/flyway-dev.conf`로 이동한다.

- `flyway.baselineOnMigrate=true`
- `flyway.validateOnMigrate=true`
- `flyway.outOfOrder=false`
- `flyway.cleanDisabled=true`

Gradle의 `flyway-config.gradle`은 dev 프로필에서 Flyway Gradle 플러그인의 `configFiles`에 이 파일을 지정한다. migration 이미지는 `FLYWAY_CONFIG_FILES=/flyway/conf/flyway-dev.conf`로 같은 파일을 읽는다.

migration 위치는 실행 환경마다 파일 시스템 경로가 다르므로 공통 설정에 포함하지 않는다.

- Gradle: `filesystem:src/main/resources/db/migration`
- migration 이미지: `FLYWAY_LOCATIONS=filesystem:/flyway/migrations`

DB URL, 사용자명과 비밀번호도 공통 설정 파일에 저장하지 않고 기존 `.env`에서 주입한다. Flyway 설정 우선순위에 따라 환경변수로 주입한 접속정보와 migration 위치가 공통 config file보다 우선한다.

정상 CD에서는 `migrate`만 실행한다. `repair`는 자동 실행하지 않으며, 장애 원인과 DB 상태를 확인한 후 운영자가 별도로 수행하는 복구 작업으로 둔다.

## CD 흐름

1. GitHub Actions에서 애플리케이션 이미지와 migration 이미지를 각각 빌드하고 ECR에 push한다.
2. migration 이미지는 `dev-<commit SHA>-migration` 태그를 사용한다.
3. Flyway job은 EC2에서 ECR 로그인 후 migration 이미지만 pull한다.
4. `.env`를 `--env-file`로 전달해 migration 컨테이너에서 `migrate`를 한 번 실행한다.
5. migration이 non-zero로 종료되면 deploy job은 실행하지 않는다.
6. deploy job은 ECR 로그인 후 애플리케이션 이미지를 pull하고 컨테이너를 교체한다.

이미지 pull을 각 job의 책임에 맞게 분리해 Flyway SSH 명령이 애플리케이션 이미지 pull 시간까지 함께 부담하지 않도록 한다.

## 이미지 정리

기존 dev 배포 이미지 정리 동작은 현재 애플리케이션 이미지와 직전 애플리케이션 이미지 ID를 보존하고, 현재 migration 이미지 태그를 정확히 일치시켜 보존한다. 과거 migration 태그는 제거하되 현재 태그가 공식 Flyway base layer를 계속 참조하므로 다음 배포에서 레이어를 재사용할 수 있다.

## 오류 처리

- Docker 및 Flyway 명령 실패는 `set -euo pipefail`에 의해 즉시 job 실패로 전파한다.
- ECR 로그인, 이미지 pull 또는 migration 실패 시 애플리케이션 배포를 시작하지 않는다.
- `repair` 실패를 무시하는 `|| true`는 제거한다.
- 환경변수 값과 DB 인증 정보는 출력하지 않는다.

## 변경 범위

- `Dockerfile`
- `docker/flyway/entrypoint.sh`
- `app-main/gradle/flyway/flyway-dev.conf`
- `app-main/gradle/flyway/flyway-config.gradle`
- `.github/workflows/dev-cd-docker.yml`

애플리케이션 코드, Flyway migration SQL, 운영 CD workflow는 변경하지 않는다.

## 검증 범위

사용자 요청에 따라 자동 테스트와 Docker 기반 migration 통합 테스트는 실행하지 않는다. 구현 후 변경 diff와 workflow의 변수 전달 및 의존관계만 정적으로 확인하며, 테스트 미실행 사실을 결과에 명시한다.

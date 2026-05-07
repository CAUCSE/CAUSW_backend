# 🚀 Flyway 마이그레이션 가이드

이 문서는 프로젝트의 데이터베이스 형상 관리를 위한 Flyway 사용법 및 환경별 워크플로를 설명합니다.


## 👋🏻 Flyway 소개
* [Flyway 도입 세미나](https://docs.google.com/presentation/d/1934h87wWPVou8HSmpKOBbLzmetSWzxTBOoZpDSisOJo/edit?usp=sharing)
* [자체 노션 Flyway 문서](https://www.notion.so/Flyway-9f8739df55f88326aedf81b272efed05?source=copy_link)
---

## ⚙️ 초기 설정 (필독)

Flyway 커맨드 실행 시 `.env` 파일 내부의 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `SPRING_PROFILES_ACTIVE` 값을 자동으로 읽어 타겟 DB를 결정합니다.

```env
# 프로젝트 루트 디렉토리의 .env 파일 예시
SPRING_PROFILES_ACTIVE=local
DB_URL=jdbc:mysql://localhost:3306/db_caucse
DB_USERNAME=root
DB_PASSWORD=12345678
```

---

## 📌 환경별 워크플로

### 💻 Local (로컬 개발 환경)

1. **마이그레이션 파일 생성**
    - 터미널에 `flywayCreate` 명령어 입력
    - `-Pdesc`: Camel Case로 변경 사항에 대한 설명(Description) 작성
   ```bash
   ./gradlew flywayCreate -Pdesc=CreateUserTable
   ```

2. **생성된 파일 내에 변경 사항 작성**
   ```sql
   -- src/main/resources/db/migration/V20250608230947__CreateUserTable.sql
   CREATE TABLE IF NOT EXISTS user (
       id BIGINT AUTO_INCREMENT PRIMARY KEY
   );
   ```

3. **애플리케이션 실행하여 로컬 DB에 테스트**
    - 스프링 부트(CauswApplication)를 실행하면 로컬 DB에 마이그레이션이 자동 적용됩니다.
    - ⚠️ **마이그레이션 실패 시:** SQL 오류를 수정한 후 애플리케이션을 재실행하면 실패 기록이 자동 삭제되며 다시 시도합니다.

### ☁️ Dev / Prod (개발 및 운영 환경)

1. **Github Actions 자동 마이그레이션**
    - `dev` 또는 `main` 브랜치에 코드가 푸시/머지되면, CD 파이프라인(`dev-cd.yml`, `main-cd.yml`)에서 자동으로 `flywayMigrate`를 수행합니다.


2. ⚠️ **마이그레이션 실패 시 수동 복구 방법**
    - GitHub Actions 로그에서 실패 원인을 파악합니다.
    - 로컬 `.env` 파일의 DB 정보를 복구하려는 타겟(Dev 또는 Prod) DB 정보로 임시 변경합니다.
    - `flywayRepair` 커맨드로 `flyway_schema_history` 테이블의 실패 기록을 삭제합니다.
        ```
        ./gradlew flywayRepair
        ```
    - SQL 스크립트의 오류를 수정하여 커밋 및 푸시(Push)하거나, 수동으로 마이그레이션을 재실행합니다.
        ```
        ./gradlew flywayMigrate
        ```
    - 🚨 **(주의)** 수동 복구가 끝난 후에는 반드시 본인의 로컬 .env 파일을 **원래의 로컬 DB 정보**로 원상복구해야 합니다! (실수로 Dev/Prod DB에 로컬 테스트용 마이그레이션을 날리는 것을 방지)
---

## 🪄 Flyway 커맨드 설명

> 💡 **참고:** 모든 명령어는 루트 경로의 `.env` 파일을 자동으로 참조하므로, 별도의 `-Penv` 옵션이 필요하지 않습니다.

### `flywayCreate`
- 마이그레이션 스크립트 파일(`.sql`)을 빈 포맷으로 생성합니다.
- `-Pdesc` 프로퍼티 입력이 필수입니다.
```
./gradlew flywayCreate -Pdesc=CreateUserTable
```

### `flywayInfo`
- 현재 타겟 DB의 마이그레이션 상태(적용 여부, 버전, 실행 시간 등)를 조회합니다.
```
./gradlew flywayInfo
```

### `flywayValidate`
- DB에 적용된 내역과 로컬에 있는 마이그레이션 스크립트 간의 불일치를 탐지합니다.
- (예: 로컬에는 스크립트가 있지만 DB에는 미적용된 경우 등)
```
./gradlew flywayValidate
```

### `flywayRepair`
- DB의 `flyway_schema_history` 테이블에 남아있는 **마이그레이션 실패 이력(Failed)을 삭제**합니다.
- 오류 스크립트를 수정한 후 재시도하기 전에 반드시 실행해야 합니다.
```
./gradlew flywayRepair
```

### `flywayMigrate`
- 작성된 마이그레이션 스크립트를 타겟 DB에 강제로 반영합니다.
```
./gradlew flywayMigrate
```

### ⚠️ `flywayClean` (위험)
- **타겟 DB의 모든 테이블과 데이터를 초기화(Drop)합니다.** 🚨 **Local에서만 주의해서 사용하세요!** 🚨
- 사용 전 `app-main/gradle/flyway/flyway-config.gradle`에 아래 설정을 임시로 추가해야 합니다.
  ```groovy
  flyway {
      // ... 생략 ...
      cleanDisabled = false // 임시 추가 후, clean 끝나면 반드시 원상복구(삭제)
  }
  ```
```
./gradlew flywayClean
```

### `flywayBaseline`
- 이미 테이블이 존재하는 기존 DB에 Flyway를 최초 도입할 때, 현재 상태를 기준점(Baseline)으로 지정합니다.
```
./gradlew flywayBaseline
```

### ❌ `flywayUndo`
- 마이그레이션을 롤백하는 기능이나, **Flyway Teams (유료 라이선스)**가 필요하므로 현재 프로젝트에서는 지원하지 않습니다. (롤백이 필요할 경우 새로운 수정 스크립트를 작성하여 전진 마이그레이션 해야 합니다.)

---

## ⚙️ 환경별 `application.yml` 설정 차이점

### Local (`application-local.yml`)
- `spring.flyway.out-of-order: true`
- 다른 팀원의 마이그레이션 스크립트를 뒤늦게 Pull 받았을 때, 버전 순서를 무시하고 마이그레이션을 허용하도록 설정되어 있어 협업에 유용합니다.

### Dev & Prod (`application-dev.yml`, `application-prod.yml`)
- `spring.flyway.enabled: false`
- 서버 애플리케이션 기동 시 스프링 부트가 자동으로 마이그레이션을 수행하는 것을 방지합니다.
- 오직 CI/CD 파이프라인(Github Actions)을 통해서만 안전하게 마이그레이션이 실행되도록 제어합니다.


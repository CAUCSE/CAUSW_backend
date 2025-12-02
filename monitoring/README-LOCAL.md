# 로컬 모니터링 환경 구축 가이드

이 문서는 CAUSW_backend 프로젝트의 로컬 개발 환경에서 Prometheus, Grafana, MySQL Exporter를 사용하여 모니터링 스택을 구축하고 사용하는 방법을 설명합니다.

## 구성 요소

*   **Prometheus**: 애플리케이션 및 MySQL 메트릭 수집 및 저장 (포트: `9090`)
*   **Grafana**: 수집된 메트릭을 시각화하는 대시보드 (포트: `3001`)
*   **mysqld-exporter**: 로컬 MySQL 데이터베이스의 성능 지표를 수집하여 Prometheus로 전송 (포트: `9104`)

## 사전 준비

### 1. `.env.local` 파일 설정

프로젝트 루트 디렉토리 (`CAUSW_Backend/`)에 `.env.local` 파일을 생성하고 다음 내용을 채워주세요.

```bash
# .env.local 파일 생성 (CAUSW_backend 루트 디렉토리에 생성)
cd CAUSW_backend
cat > .env.local << 'EOF'
DB_USERNAME=root
DB_PASSWORD=your_mysql_password
MY_UID=501
MY_GID=20
EOF
```

**설정 값 설명:**
- `DB_USERNAME`: 로컬 MySQL 루트 계정 (기본값: root)
- `DB_PASSWORD`: 로컬 MySQL 비밀번호 (실제 비밀번호로 변경 필수)
- `MY_UID`: 현재 사용자 UID (터미널에서 `id -u` 실행)
- `MY_GID`: 현재 사용자 GID (터미널에서 `id -g` 실행)

> 중요: `start.sh` 스크립트가 자동으로 MySQL에 `exporter` 계정을 생성하므로, 별도 MySQL 설정은 필요 없습니다.

### 2. 로컬 MySQL 실행 확인

모니터링 스택을 시작하기 전에 로컬 MySQL이 실행 중인지 확인하세요.

```bash
# MySQL 실행 확인
mysql -u root -p -e "SELECT 1;"

# MySQL이 실행 중이 아니라면 시작
# macOS (Homebrew)
brew services start mysql

# Linux (systemd)
sudo systemctl start mysql
```

## 실행 방법

### 빠른 시작 (Quickstart)

프로젝트 루트 디렉토리에서 다음 명령어만 실행하면 됩니다:

```bash
cd CAUSW_backend

# 1. .env.local 파일 생성 (최초 1회)
#    → 위의 "사전 준비" 섹션 참고

# 2. 스크립트 실행 권한 부여 (최초 1회)
chmod +x monitoring/local/start.sh

# 3. Spring Boot 애플리케이션 실행
#    → IntelliJ에서 실행하거나 ./gradlew bootRun

# 4. 모니터링 스택 시작
monitoring/local/start.sh
```

### 스크립트가 자동으로 수행하는 작업

`start.sh` 실행 시 다음 작업이 자동으로 진행됩니다:

1. `.env.local` 파일 존재 여부 확인
2. Spring Boot 애플리케이션 실행 여부 확인
3. 필요한 디렉토리 생성 (`prometheus-data`, `grafana-data`)
4. `mysqld-exporter` 설정 파일 생성
5. MySQL 자동 설정:
   - Slow Query Log 활성화 (1초 이상)
   - `exporter` 계정 생성 및 권한 부여
6. Docker Compose로 모니터링 스택 시작

### 중지 방법

모니터링 스택을 중지하려면, `CAUSW_Backend/monitoring/local` 디렉토리로 이동하여 다음 명령어를 실행합니다:

```bash
cd monitoring/local
docker-compose down
```

## 접속 정보

모든 서비스가 성공적으로 시작되었다면 다음 URL로 접속할 수 있습니다.

*   **Prometheus**: [http://localhost:9090](http://localhost:9090)
    *   Targets: [http://localhost:9090/targets](http://localhost:9090/targets) 에서 애플리케이션(`causw-backend-local`)과 MySQL Exporter(`mysql-local`)가 `UP` 상태인지 확인하세요.
*   **Grafana**: [http://localhost:3001](http://localhost:3001)
    *   초기 로그인 정보: ID `admin`, PW `admin`

## Grafana 초기 설정

### 1. Prometheus 데이터 소스 추가

1.  Grafana에 접속하여 로그인합니다.
2.  좌측 메뉴에서 `Configuration` (톱니바퀴 아이콘) → `Data Sources`를 클릭합니다.
3.  `Add data source`를 클릭하고 `Prometheus`를 선택합니다.
4.  `URL` 필드에 `http://prometheus:9090`를 입력합니다. (Docker Compose 네트워크 내부 이름)
5.  `Save & Test` 버튼을 클릭하여 연결을 확인합니다.

### 2. 추천 대시보드 Import

널리 사용되는 대시보드 템플릿을 가져와 활용할 수 있습니다.

1.  좌측 메뉴에서 `Dashboards` → `Import`를 클릭합니다.
2.  `Import via grafana.com` 섹션에 아래 ID를 입력하고 `Load` 버튼을 클릭합니다.
3.  가져온 대시보드에 사용할 `Prometheus` 데이터 소스를 선택하고 `Import`를 클릭합니다.

*   **Spring Boot System Monitor**: `11378`
*   **MySQL Overview**: `7362`

## 검증 방법

### 1. Prometheus Targets 확인

Prometheus UI ([http://localhost:9090/targets](http://localhost:9090/targets))에서 `causw-backend-local`과 `mysql-local` 두 개의 `job`이 모두 `UP` 상태인지 확인합니다.

### 2. 메트릭 수집 확인 (Prometheus UI)

Prometheus Query ([http://localhost:9090/graph](http://localhost:9090/graph)) 페이지에서 다음 쿼리를 실행하여 데이터가 수집되는지 확인합니다.

*   **애플리케이션 메트릭**: `http_server_requests_seconds_count`
*   **MySQL 메트릭**: `mysql_up`

### 3. Tail Latency (p99, p95) 확인 (Prometheus UI)

애플리케이션에 트래픽을 발생시킨 후, 다음 쿼리를 실행하여 히스토그램 기반의 레이턴시 메트릭이 계산되는지 확인합니다.

*   **p95 Latency**: `histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket[5m])) by (le, uri))`
*   **p99 Latency**: `histogram_quantile(0.99, sum(rate(http_server_requests_seconds_bucket[5m])) by (le, uri))`

## 트러블슈팅

### 1. `start.sh` 실행 시 ".env.local 파일이 없습니다" 에러

```bash
[ERROR] CAUSW_backend 루트에 .env.local 파일이 없습니다.
```

**해결:**
- `CAUSW_backend` 루트 디렉토리에 `.env.local` 파일 생성 (위의 "사전 준비" 섹션 참고)
- 현재 위치 확인: `pwd` (CAUSW_backend 디렉토리에서 실행해야 함)

---

### 2. "Spring Boot가 실행되지 않았거나 응답하지 않습니다" 경고

```bash
[WARNING] Spring Boot가 실행되지 않았거나 응답하지 않습니다.
```

**원인:**
- Spring Boot 애플리케이션이 실행되지 않음
- 8080 포트가 아닌 다른 포트로 실행 중

**해결:**
```bash
# 애플리케이션 실행 확인
curl http://localhost:8080/actuator/health

# 응답이 없다면 Spring Boot 실행
cd app-main
./gradlew bootRun
```

**참고:** 이 경고가 발생해도 모니터링 스택은 실행됩니다. 애플리케이션 실행 후 Prometheus Targets에서 상태를 확인하세요.

---

### 3. MySQL 접속 실패

```bash
[ERROR] MySQL 접속 실패!
```

**원인 및 해결:**

#### 3-1. MySQL이 실행 중이 아님
```bash
# MySQL 상태 확인
brew services list | grep mysql  # macOS
systemctl status mysql           # Linux

# MySQL 시작
brew services start mysql        # macOS
sudo systemctl start mysql       # Linux
```

#### 3-2. .env.local의 DB_PASSWORD가 틀림
```bash
# MySQL 접속 테스트
mysql -u root -p

# 접속되면 .env.local의 DB_PASSWORD 확인
cat .env.local | grep DB_PASSWORD
```

#### 3-3. MySQL이 외부 접속을 허용하지 않음
```bash
# MySQL 설정 확인
mysql -u root -p -e "SHOW VARIABLES LIKE 'bind-address';"

# bind-address가 127.0.0.1인지 확인
# 만약 다른 값이면 my.cnf 수정 필요
```

#### 3-4. 수동으로 MySQL 설정
```sql
mysql -u root -p << EOF
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 1.0;
CREATE USER IF NOT EXISTS 'exporter'@'%' IDENTIFIED BY 'exporter_password';
GRANT PROCESS, REPLICATION CLIENT, SELECT ON *.* TO 'exporter'@'%';
GRANT SELECT ON performance_schema.* TO 'exporter'@'%';
FLUSH PRIVILEGES;
EOF
```

---

### 4. mysqld-exporter가 메트릭을 수집하지 못함

**증상:**
- Prometheus Targets에서 `mysql-local`이 DOWN 상태

**확인:**
```bash
# mysqld-exporter 로그 확인
docker logs causw-mysqld-exporter

# 에러 예시: "Error 1045: Access denied for user 'exporter'@'...'
```

**해결:**
```bash
# 1. exporter 계정 권한 확인
mysql -u root -p -e "SHOW GRANTS FOR 'exporter'@'%';"

# 2. 권한이 없다면 수동 부여
mysql -u root -p << EOF
GRANT PROCESS, REPLICATION CLIENT, SELECT ON *.* TO 'exporter'@'%';
GRANT SELECT ON performance_schema.* TO 'exporter'@'%';
FLUSH PRIVILEGES;
EOF

# 3. mysqld-exporter 재시작
cd monitoring/local
docker-compose restart mysqld-exporter
```

---

### 5. Prometheus가 애플리케이션 메트릭을 수집하지 못함

**증상:**
- Prometheus Targets에서 `causw-backend-local`이 DOWN 상태

**확인:**
```bash
# Spring Boot Actuator 엔드포인트 접근 테스트
curl http://localhost:8080/actuator/prometheus

# 응답이 없으면 애플리케이션 설정 확인
```

**해결:**
- `application-local.yml`에 다음 설정이 있는지 확인:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,metrics
```

---

### 6. Grafana 대시보드에 데이터가 없음

**원인:**
1. Prometheus 데이터 소스가 연결되지 않음
2. Time Range가 부적절함
3. 애플리케이션에 트래픽이 없음

**해결:**
```bash
# 1. Prometheus 데이터 소스 확인
#    → Grafana: Configuration → Data Sources → Prometheus
#    → URL이 "http://prometheus:9090"인지 확인

# 2. Time Range 조정
#    → 대시보드 우측 상단에서 "Last 15 minutes" 선택

# 3. 애플리케이션에 요청 보내기
curl http://localhost:8080/actuator/health
curl http://localhost:8080/api/users  # 실제 API 엔드포인트
```

---

### 7. 권한 오류 (Permission Denied)

**증상:**
```bash
Error response from daemon: failed to create shim task:
OCI runtime create failed: unable to start container process:
error during container init: error mounting ...
```

**해결:**
```bash
# prometheus-data와 grafana-data 권한 수정
cd monitoring
chmod -R 777 prometheus-data grafana-data

# 또는 .env.local의 MY_UID, MY_GID 확인
id -u  # MY_UID
id -g  # MY_GID
```

---

### 8. 포트 충돌

**증상:**
```bash
Error starting userland proxy: listen tcp 0.0.0.0:9090: bind: address already in use
```

**해결:**
```bash
# 포트 사용 중인 프로세스 확인
lsof -i :9090
lsof -i :3001

# 프로세스 종료
kill -9 <PID>

# 또는 docker-compose.yml에서 포트 번호 변경
```

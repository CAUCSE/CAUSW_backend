#!/bin/bash

# 스크립트 위치로 이동
cd "$(dirname "$0")" || exit

echo "[INFO] CAUSW 로컬 모니터링 환경 설정을 시작합니다."
echo ""

# 1. .env.local 확인
if [ ! -f "../../.env.local" ]; then
    echo "[ERROR] CAUSW_backend 루트에 .env.local 파일이 없습니다."
    echo "        다음 내용으로 .env.local 파일을 생성하세요:"
    echo ""
    echo "        DB_USERNAME=root"
    echo "        DB_PASSWORD=your_mysql_password"
    echo "        MY_UID=$(id -u)"
    echo "        MY_GID=$(id -g)"
    echo ""
    exit 1
fi

# 환경변수 로드
export $(grep -v '^#' ../../.env.local | xargs)

# 2. Spring Boot 실행 여부 확인
echo "[INFO] Spring Boot 애플리케이션 상태 확인 중..."
if curl -s --max-time 3 http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "[OK] Spring Boot가 실행 중입니다."
else
    echo "[WARNING] Spring Boot가 실행되지 않았거나 응답하지 않습니다."
    echo "          - Prometheus가 애플리케이션 메트릭을 수집하지 못할 수 있습니다."
    echo "          - 애플리케이션 실행 후 Prometheus Targets를 확인하세요: http://localhost:9090/targets"
fi
echo ""

# 2. 디렉토리 자동 생성
mkdir -p ../prometheus-data
mkdir -p ../grafana-data
mkdir -p ../mysqld-exporter
chmod -R 777 ../prometheus-data ../grafana-data

# 3. mysqld-exporter용 .my.cnf 자동 생성
# exporter 계정은 아래 4번 단계에서 자동으로 DB에 생성됩니다.
echo "[INFO] mysqld-exporter 설정 파일(.my.cnf) 생성 중..."
cat <<EOF > ../mysqld-exporter/.my.cnf
[client]
user=exporter
password=exporter_password
host=host.docker.internal
port=3306
EOF
chmod 644 ../mysqld-exporter/.my.cnf

# 4. [핵심] 호스트 MySQL 설정 자동화 (Docker를 이용해 접속)
echo "[INFO] 로컬 MySQL 설정 자동 적용 중 (Slow Query ON, Exporter 계정 생성)..."

# MySQL 접속 테스트 먼저 수행
if ! docker run --rm \
  --add-host host.docker.internal:host-gateway \
  mysql:8.0 \
  mysql -h host.docker.internal -P 3306 -u"${DB_USERNAME}" -p"${DB_PASSWORD}" \
  -e "SELECT 1;" > /dev/null 2>&1; then
    echo "[ERROR] MySQL 접속 실패!"
    echo "        다음 사항을 확인하세요:"
    echo "        1. 로컬 MySQL이 실행 중인가요? (포트 3306)"
    echo "        2. .env.local의 DB_USERNAME과 DB_PASSWORD가 정확한가요?"
    echo "        3. MySQL이 외부 접속을 허용하나요? (bind-address 확인)"
    echo ""
    echo "        수동으로 다음 명령어를 실행하세요:"
    echo "        -------------------------------------------"
    echo "        mysql -u root -p << EOF"
    echo "        SET GLOBAL slow_query_log = 'ON';"
    echo "        SET GLOBAL long_query_time = 0.1;"
    echo "        CREATE USER IF NOT EXISTS 'exporter'@'%' IDENTIFIED BY 'exporter_password';"
    echo "        GRANT PROCESS, REPLICATION CLIENT, SELECT ON *.* TO 'exporter'@'%';"
    echo "        GRANT SELECT ON performance_schema.* TO 'exporter'@'%';"
    echo "        FLUSH PRIVILEGES;"
    echo "        EOF"
    echo "        -------------------------------------------"
    echo ""
    echo "[WARNING] MySQL 설정 없이 모니터링 스택을 실행합니다."
    echo "          mysqld-exporter는 실행되지만 메트릭 수집에 실패할 수 있습니다."
    echo ""
else
    # MySQL 설정 쿼리 실행
    docker run --rm \
      --add-host host.docker.internal:host-gateway \
      mysql:8.0 \
      mysql -h host.docker.internal -P 3306 -u"${DB_USERNAME}" -p"${DB_PASSWORD}" -e "
        SET GLOBAL slow_query_log = 'ON';
        SET GLOBAL long_query_time = 1.0;
        
        CREATE USER IF NOT EXISTS 'exporter'@'%' IDENTIFIED BY 'exporter_password';
        ALTER USER 'exporter'@'%' IDENTIFIED BY 'exporter_password';
        GRANT PROCESS, REPLICATION CLIENT, SELECT ON *.* TO 'exporter'@'%';
        GRANT SELECT ON performance_schema.* TO 'exporter'@'%';
        
        CREATE USER IF NOT EXISTS 'exporter'@'localhost' IDENTIFIED BY 'exporter_password';
        ALTER USER 'exporter'@'localhost' IDENTIFIED BY 'exporter_password';
        GRANT PROCESS, REPLICATION CLIENT, SELECT ON *.* TO 'exporter'@'localhost';
        GRANT SELECT ON performance_schema.* TO 'exporter'@'localhost';
        
        FLUSH PRIVILEGES;
      " > /dev/null 2>&1

    if [ $? -eq 0 ]; then
        echo "[OK] MySQL 설정이 완료되었습니다."
        echo "     - Slow Query Log: ON (1초 이상)"
        echo "     - Exporter 계정: exporter@'%' (password: exporter_password)"
    else
        echo "[WARNING] MySQL 설정 일부가 실패했을 수 있습니다."
        echo "          mysqld-exporter 로그를 확인하세요: docker logs causw-mysqld-exporter"
    fi
fi
echo ""

# 5. Docker Compose 실행
echo "[INFO] 모니터링 컨테이너를 시작합니다..."
docker-compose -f docker-compose.yml --env-file ../../.env.local up -d --remove-orphans

echo ""
echo "=========================================="
echo "모니터링 시스템이 실행되었습니다!"
echo "=========================================="
echo ""
echo "[접속 정보]"
echo "  - Grafana:    http://localhost:3001 (admin/admin)"
echo "  - Prometheus: http://localhost:9090"
echo ""
echo "[검증 단계]"
echo "  1. Prometheus Targets: http://localhost:9090/targets"
echo "     -> causw-backend-local, mysql-local, node-local 모두 UP 확인"
echo ""
echo "  2. Spring Boot 메트릭 확인:"
echo "     -> http://localhost:8080/actuator/prometheus"
echo ""
echo "  3. Grafana 대시보드 Import (최초 1회):"
echo "     -> ID 11378 (Spring Boot System Monitor)"
echo "     -> ID 7362 (MySQL Overview)"
echo ""
echo "[문제 발생 시]"
echo "  - 컨테이너 로그: docker logs causw-prometheus"
echo "  - 컨테이너 로그: docker logs causw-mysqld-exporter"
echo "  - README 참고: monitoring/README-LOCAL.md"
echo ""

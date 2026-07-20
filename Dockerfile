# ─── Flyway Migration ─────────────────────────────────────────────────────────
FROM flyway/flyway:12.4.0-alpine AS migration

COPY app-main/src/main/resources/db/migration /flyway/migrations
COPY app-main/gradle/flyway/flyway-dev.conf /flyway/conf/flyway-dev.conf
COPY --chmod=755 docker/flyway/entrypoint.sh /flyway/causw-entrypoint.sh

# flyway gradle 실행 환경 변수 관련 사항: https://documentation.red-gate.com/flyway/reference/usage/gradle-task
# flyway gradle sql 저장 위치 관련 사항: https://documentation.red-gate.com/flyway/reference/configuration/flyway-namespace/flyway-locations-setting
# FLYWAY_CONFIG_FILES: 환경 변수로 설정 파일 지정 가능 (명령줄 파라미터보다 우선)
ENV FLYWAY_CONFIG_FILES=/flyway/conf/flyway-dev.conf
# FLYWAY_LOCATIONS: 환경 변수로 sql 저장 위치 지정 가능
ENV FLYWAY_LOCATIONS=filesystem:/flyway/migrations

ENTRYPOINT ["/flyway/causw-entrypoint.sh"]
CMD ["migrate"]

# ─── Stage 1: Build ───────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jdk-jammy AS builder

WORKDIR /app

# Gradle wrapper & build scripts 먼저 복사 (레이어 캐시 활용)
COPY gradlew gradlew.bat build.gradle settings.gradle ./
COPY gradle ./gradle

# 각 모듈의 build.gradle 및 gradle 스크립트 복사
COPY global/build.gradle ./global/
COPY app-main/build.gradle ./app-main/
COPY app-main/gradle ./app-main/gradle

# 의존성 다운로드만 먼저 실행 (소스 변경 시 재다운로드 방지)
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon --quiet

# 전체 소스 복사
COPY global ./global
COPY app-main ./app-main

# bootJar 빌드 (테스트 제외)
RUN ./gradlew :app-main:bootJar -x test --no-daemon

# ─── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jre-jammy

WORKDIR /app

ENV TZ=Asia/Seoul
ENV LOG_DIR=/app/log

# 헬스체크용 curl 설치
RUN apt-get update \
  && apt-get install -y --no-install-recommends curl \
  && rm -rf /var/lib/apt/lists/*

# 비루트 유저 생성
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser

# 빌드 산출물만 복사
COPY --from=builder /app/app-main/build/libs/*.jar app.jar

RUN mkdir -p "$LOG_DIR" \
  && chown -R appuser:appgroup app.jar "$LOG_DIR"

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -sf http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Duser.timezone=Asia/Seoul", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]

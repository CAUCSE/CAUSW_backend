#!/usr/bin/env bash
#
# 긴급 복구 — 지정한 슬롯으로 트래픽을 되돌립니다.
#
# 배포가 잘못됐을 때 명령 한 줄로 서비스를 살리기 위한 스크립트입니다.
# CI 가 막혔거나 배포가 중간에 멈춘 상황에서 쓰게 되므로,
# EC2 에 상주시켜 두고 SSH 만 붙으면 바로 실행할 수 있어야 합니다.
#
# 사용법:
#   bash rollback.sh                         # 현재 상태만 출력 (아무것도 바꾸지 않음)
#   bash rollback.sh status                  # 위와 동일
#   bash rollback.sh prev                    # 직전 슬롯으로 자동 복구  ← 보통 이것만 쓰면 됩니다
#   bash rollback.sh blue                    # 8080 슬롯으로 되돌림
#   bash rollback.sh green                   # 18080 슬롯으로 되돌림
#   bash rollback.sh legacy                  # blue/green 이전의 단일 컨테이너로 되돌림
#   IMAGE=<태그> bash rollback.sh blue       # 컨테이너가 없으면 이 이미지로 새로 기동
#   SKIP_HEALTH=1 bash rollback.sh blue      # 헬스체크 생략하고 즉시 전환
#
# 필수 환경변수:
#   APP_CONTAINER_NAME   컨테이너 이름 접두사 (예: causw-app)
#
#

set -uo pipefail

TARGET="${1:-}"

: "${APP_CONTAINER_NAME:?APP_CONTAINER_NAME variable is required}"

BLUE_PORT="${BLUE_PORT:-8080}"
GREEN_PORT="${GREEN_PORT:-18080}"
UPSTREAM_NAME="${UPSTREAM_NAME:-app-main}"
UPSTREAM_CONF="${UPSTREAM_CONF:-/etc/nginx/conf.d/app-main-upstream.conf}"
ENV_FILE="${ENV_FILE:-/home/ubuntu/app/app-main/.env}"
LOG_DIR="${LOG_DIR:-/var/log/spring-boot/app-main}"
HEALTH_TIMEOUT_SECONDS="${HEALTH_TIMEOUT_SECONDS:-180}"

log()  { printf '[%s] %s\n' "$(date '+%H:%M:%S')" "$*"; }
fail() { printf '[%s] ERROR: %s\n' "$(date '+%H:%M:%S')" "$*" >&2; }

# ── 현재 live 슬롯 판별 ──────────────────────────────────────────────────────
# green(18080)을 먼저 검사합니다. ":8080" 으로 먼저 보면 ":18080" 도 걸립니다.
detect_live() {
  if [ ! -f "$UPSTREAM_CONF" ]; then
    echo "none"
  elif grep -qE "server[[:space:]]+[^;]*:${GREEN_PORT};" "$UPSTREAM_CONF"; then
    echo "green"
  elif grep -qE "server[[:space:]]+[^;]*:${BLUE_PORT};" "$UPSTREAM_CONF"; then
    echo "blue"
  else
    echo "unknown"
  fi
}

container_exists() {
  docker ps -a --format '{{.Names}}' 2>/dev/null | grep -qx "$1"
}

probe() {
  curl -s -o /dev/null -w '%{http_code}' --max-time 3 \
    "http://127.0.0.1:${1}/actuator/health" 2>/dev/null || echo "응답없음"
}

show_status() {
  printf '\n현재 상태\n'
  printf '  live 슬롯      : %s\n' "$(detect_live)"
  printf '  upstream 파일  : %s\n' "$UPSTREAM_CONF"
  if [ -f "$UPSTREAM_CONF" ]; then
    grep -E 'server[[:space:]]+' "$UPSTREAM_CONF" | sed 's/^/                   /'
  else
    printf '                   (없음 — blue/green 미적용 상태)\n'
  fi
  printf '\n  직접 응답 확인\n'
  printf '    blue  (%s) : %s\n' "$BLUE_PORT"  "$(probe "$BLUE_PORT")"
  printf '    green (%s): %s\n' "$GREEN_PORT" "$(probe "$GREEN_PORT")"
  printf '\n  컨테이너\n'
  docker ps -a --filter "name=${APP_CONTAINER_NAME}" \
    --format '    {{.Names}}\t{{.Status}}\t{{.Ports}}' 2>/dev/null || true
  printf '\n  복구하려면:  bash rollback.sh prev\n\n'
}

LIVE="$(detect_live)"

case "$TARGET" in
  ""|status)
    show_status
    exit 0
    ;;
  prev|auto)
    # 직전 슬롯 = 지금 live 가 아닌 쪽
    case "$LIVE" in
      green) TARGET=blue ;;
      blue)  TARGET=green ;;
      *)
        fail "현재 슬롯을 판별할 수 없어 prev 를 쓸 수 없습니다 (live=${LIVE})"
        fail "슬롯을 직접 지정하세요:  bash rollback.sh <blue|green|legacy>"
        show_status
        exit 1
        ;;
    esac
    # 최초 전환 직후에는 직전 슬롯 컨테이너가 없고 legacy 만 있습니다.
    if ! container_exists "${APP_CONTAINER_NAME}-${TARGET}" \
       && container_exists "$APP_CONTAINER_NAME"; then
      log "직전 슬롯(${TARGET}) 컨테이너가 없어 legacy 로 복구합니다"
      TARGET=legacy
    fi
    log "자동 선택: ${TARGET} (현재 live=${LIVE})"
    ;;
  blue|green|legacy)
    :
    ;;
  *)
    fail "사용법: bash rollback.sh [prev|blue|green|legacy|status]"
    fail "  인자 없이 실행하면 현재 상태만 출력합니다."
    exit 1
    ;;
esac

case "$TARGET" in
  blue)   PORT="$BLUE_PORT";  CONTAINER="${APP_CONTAINER_NAME}-blue" ;;
  green)  PORT="$GREEN_PORT"; CONTAINER="${APP_CONTAINER_NAME}-green" ;;
  legacy) PORT="$BLUE_PORT";  CONTAINER="${APP_CONTAINER_NAME}" ;;
esac

# 이미 live 인 슬롯으로 되돌리는 것은 보통 착각입니다.
if [ "$TARGET" = "$LIVE" ]; then
  fail "[주의] ${TARGET} 는 이미 live 슬롯입니다. 되돌릴 대상이 맞는지 확인하세요."
  show_status
fi

log "복구 대상: ${TARGET} (컨테이너 ${CONTAINER}, 포트 ${PORT})"

# ── 배포 잠금 ────────────────────────────────────────────────────────────────
# 잠금을 잡아보되, 못 잡아도 진행합니다.
# 복구는 "배포가 멈췄거나 잘못됐을 때" 쓰는 도구이므로 잠금에 막히면 안 됩니다.
# 다만 배포와 동시에 돌면 서로 upstream 을 덮어쓸 수 있으므로 경고합니다.
# WAIT_LOCK=1 을 주면 배포가 끝날 때까지 기다린 뒤 진행합니다.
exec 9>"/tmp/${APP_CONTAINER_NAME}-deploy.lock"
if flock -n 9; then
  log "배포 잠금 획득 — 경합 없음"
elif [ "${WAIT_LOCK:-0}" = "1" ]; then
  log "WAIT_LOCK=1 — 배포가 끝날 때까지 대기 (최대 600초)"
  flock -w 600 9 || fail "잠금 대기 시간 초과 — 그대로 진행합니다"
else
  fail "!! 배포가 진행 중입니다. 잠금 없이 복구를 강행합니다."
  fail "   배포가 끝나면서 이 복구를 덮어쓸 수 있습니다. 완료 후 반드시 확인하세요:"
  fail "     cat ${UPSTREAM_CONF} && docker ps"
  fail "   기다렸다 하려면:  WAIT_LOCK=1 bash rollback.sh ${TARGET}"
fi

# ── 1. 대상 컨테이너 확보 ────────────────────────────────────────────────────
if docker ps --format '{{.Names}}' | grep -qx "$CONTAINER"; then
  log "컨테이너가 이미 실행 중입니다"
elif docker ps -a --format '{{.Names}}' | grep -qx "$CONTAINER"; then
  log "정지된 컨테이너를 다시 시작합니다"
  if ! docker start "$CONTAINER"; then
    fail "docker start 실패 — 죽은 포트로 전환하지 않도록 중단합니다"
    exit 1
  fi
elif [ -n "${IMAGE:-}" ]; then
  log "컨테이너가 없어 새로 기동합니다 (이미지: $IMAGE)"
  MEM_ARGS=()
  if [ -n "${CONTAINER_MEM_MB:-}" ] && [ "${CONTAINER_MEM_MB:-0}" != "0" ]; then
    MEM_ARGS=(--memory="${CONTAINER_MEM_MB}m" --memory-swap="${CONTAINER_MEMSWAP_MB:-$CONTAINER_MEM_MB}m")
  fi
  JAVA_ARGS=()
  if [ -n "${JAVA_TOOL_OPTIONS:-}" ]; then
    JAVA_ARGS=(-e "JAVA_TOOL_OPTIONS=${JAVA_TOOL_OPTIONS}")
  fi
  docker run -d \
    --name "$CONTAINER" \
    --restart unless-stopped \
    -p "${PORT}:8080" \
    --env-file "$ENV_FILE" \
    -v "${LOG_DIR}:/app/log" \
    --add-host=host.docker.internal:host-gateway \
    ${MEM_ARGS[@]+"${MEM_ARGS[@]}"} \
    ${JAVA_ARGS[@]+"${JAVA_ARGS[@]}"} \
    "$IMAGE" || {
      fail "docker run 실패 — 죽은 포트로 전환하지 않도록 중단합니다"
      exit 1
    }
else
  # 컨테이너 없는 포트로 전환하면 nginx 가 502 를 냅니다.
  # 지금보다 나빠지므로 아무것도 하지 않고 멈춥니다.
  fail "컨테이너 ${CONTAINER} 가 없고 IMAGE 도 지정되지 않았습니다."
  fail "죽은 포트로 전환하면 502 가 나므로 중단합니다."
  fail "복구할 이미지 태그를 지정해 다시 실행하세요:"
  fail "  IMAGE=<태그> bash rollback.sh ${TARGET}"
  exit 1
fi

# ── 2. 헬스체크 ──────────────────────────────────────────────────────────────
if [ "${SKIP_HEALTH:-0}" = "1" ]; then
  log "SKIP_HEALTH=1 — 헬스체크를 생략합니다"
else
  log "헬스체크 대기 (최대 ${HEALTH_TIMEOUT_SECONDS}초)"
  DEADLINE=$(( $(date +%s) + HEALTH_TIMEOUT_SECONDS ))
  HEALTHY=0
  while [ "$(date +%s)" -lt "$DEADLINE" ]; do
    BODY="$(curl -s --max-time 5 "http://127.0.0.1:${PORT}/actuator/health" 2>/dev/null || true)"
    if printf '%s' "$BODY" | grep -q '"status":"UP"'; then
      HEALTHY=1
      break
    fi
    printf '.'
    sleep 3
  done
  printf '\n'

  if [ "$HEALTHY" -ne 1 ]; then
    fail "헬스체크가 통과하지 못했습니다."
    fail "그래도 전환하려면:  SKIP_HEALTH=1 bash rollback.sh ${TARGET}"
    docker logs --tail 30 "$CONTAINER" 2>&1 | sed 's/^/  /' >&2 || true
    exit 1
  fi
  log "헬스체크 통과"
fi

# ── 3. upstream 전환 ─────────────────────────────────────────────────────────
BACKUP_CONF="$(mktemp)"
if [ -f "$UPSTREAM_CONF" ]; then
  sudo cp "$UPSTREAM_CONF" "$BACKUP_CONF"
fi

sudo tee "$UPSTREAM_CONF" > /dev/null <<EOF
# rollback.sh 가 생성했습니다.
# 생성 시각: $(date '+%Y-%m-%d %H:%M:%S') / slot=${TARGET}
upstream ${UPSTREAM_NAME} {
    server 127.0.0.1:${PORT};
    keepalive 64;
}
EOF

if ! sudo nginx -t; then
  fail "nginx -t 실패 — upstream 파일을 원래대로 되돌립니다"
  if [ -s "$BACKUP_CONF" ]; then
    sudo cp "$BACKUP_CONF" "$UPSTREAM_CONF"
  fi
  rm -f "$BACKUP_CONF"
  fail "설정을 직접 확인해야 합니다: $UPSTREAM_CONF"
  exit 1
fi

# set -e 를 쓰지 않으므로 reload 실패를 명시적으로 잡아야 합니다.
# 그냥 두면 실패해도 성공 로그를 찍고 최종 확인까지 진행합니다.
if ! sudo nginx -s reload; then
  fail "nginx reload 실패 — upstream 파일을 원래대로 되돌립니다"
  if [ -s "$BACKUP_CONF" ]; then
    sudo cp "$BACKUP_CONF" "$UPSTREAM_CONF"
    if sudo nginx -t > /dev/null 2>&1; then
      sudo nginx -s reload || true
    fi
  fi
  rm -f "$BACKUP_CONF"
  fail "복구에 실패했습니다. 수동 확인이 필요합니다: $UPSTREAM_CONF"
  exit 1
fi
rm -f "$BACKUP_CONF"
log "트래픽을 ${TARGET}(${PORT}) 로 전환했습니다"

# ── 4. 최종 확인 ─────────────────────────────────────────────────────────────
FINAL="$(
  curl -s -o /dev/null -w '%{http_code}' --max-time 5 \
    "http://127.0.0.1:${PORT}/actuator/health" 2>/dev/null || true
)"
if [ "$FINAL" = "200" ]; then
  log "복구 완료"
else
  fail "전환은 했지만 헬스체크가 ${FINAL:-응답없음} 입니다. 계속 확인이 필요합니다."
fi

docker ps --filter "name=${APP_CONTAINER_NAME}" \
  --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}'

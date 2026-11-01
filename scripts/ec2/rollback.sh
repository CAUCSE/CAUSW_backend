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
#   IMAGE=<이미지> bash rollback.sh prev     # 직전 컨테이너가 이미 지워졌을 때
#   bash rollback.sh blue                    # 8080 슬롯으로 되돌림
#   bash rollback.sh green                   # 18080 슬롯으로 되돌림
#   bash rollback.sh legacy                  # 단일 컨테이너 방식으로 전환 (blue/green 끄기 전에 실행)
#   IMAGE=<이미지> bash rollback.sh blue     # 지정한 이미지로 새로 기동
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

is_running() {
  [ "$(docker inspect --format='{{.State.Running}}' "$1" 2>/dev/null)" = "true" ]
}

# legacy 가 green 생성 뒤에 멈췄으면 최초 전환 직후 = legacy 가 직전 버전
legacy_is_predecessor() {
  container_exists "$APP_CONTAINER_NAME" || return 1
  container_exists "${APP_CONTAINER_NAME}-green" || return 1
  is_running "$APP_CONTAINER_NAME" && return 1
  # 초 단위로 잘라야 문자열 비교가 맞습니다
  local stopped created
  stopped="$(docker inspect --format='{{.State.FinishedAt}}' "$APP_CONTAINER_NAME" 2>/dev/null | cut -c1-19)"
  created="$(docker inspect --format='{{.Created}}' "${APP_CONTAINER_NAME}-green" 2>/dev/null | cut -c1-19)"
  [ -n "$stopped" ] && [ -n "$created" ] && [[ "$stopped" > "$created" ]]
}

# IMAGE 로 쓸 수 있는 로컬 이미지 목록 (직전 버전은 지워지지 않고 남아 있음)
suggest_images() {
  local live_image
  live_image="$(docker inspect --format='{{.Config.Image}}' "${APP_CONTAINER_NAME}-${LIVE}" 2>/dev/null \
    || docker inspect --format='{{.Config.Image}}' "$APP_CONTAINER_NAME" 2>/dev/null || true)"
  [ -n "$live_image" ] || return 0
  fail "로컬에 남아 있는 이미지 (최신순, 현재 live ${live_image} 제외):"
  docker image ls "${live_image%:*}" --format '{{.Repository}}:{{.Tag}}\t{{.CreatedSince}}' 2>/dev/null \
    | grep -v -e '-migration' -e '<none>' | awk -F'\t' -v live="$live_image" '$1 != live' \
    | sed 's/^/    /' >&2 || true
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

# ── 인자 형식 확인 (잠금 전, 부작용 없음) ───────────────────────────────────
case "$TARGET" in
  ""|status|prev|auto|blue|green|legacy) : ;;
  *)
    fail "사용법: bash rollback.sh [prev|blue|green|legacy|status]"
    fail "  인자 없이 실행하면 현재 상태만 출력합니다."
    exit 1
    ;;
esac

# 상태 조회는 아무것도 바꾸지 않으므로 잠금이 필요 없습니다.
if [ -z "$TARGET" ] || [ "$TARGET" = "status" ]; then
  show_status
  exit 0
fi

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

# ── 슬롯 판별은 잠금 이후에 ─────────────────────────────────────────────────
# 잠금보다 먼저 읽으면, 대기하는 사이 배포가 슬롯을 바꿔서
# 엉뚱한 컨테이너를 고르거나 배포 결과를 덮어쓸 수 있습니다.
LIVE="$(detect_live)"
LEGACY_AS_PREV=0

case "$TARGET" in
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
    # 최초 전환 직후에만 legacy 가 blue 자리를 대신합니다 (같은 8080 포트)
    if [ "$TARGET" = "blue" ] && [ -z "${IMAGE:-}" ] \
       && ! container_exists "${APP_CONTAINER_NAME}-blue" \
       && legacy_is_predecessor; then
      log "최초 전환 직후입니다 — 직전 버전인 legacy 로 복구합니다"
      TARGET=legacy
      LEGACY_AS_PREV=1
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

# ── legacy: 단일 컨테이너 방식으로 전환 (버전은 현재 live 유지) ─────────────
if [ "$TARGET" = "legacy" ] && [ "$LEGACY_AS_PREV" = "0" ] && [ -z "${IMAGE:-}" ]; then
  if [ "$LIVE" = "blue" ] && is_running "${APP_CONTAINER_NAME}-blue"; then
    # blue 가 8080 에서 서비스 중이면 이름만 바꿉니다 (중단 없음)
    if container_exists "$APP_CONTAINER_NAME"; then
      docker rm "$APP_CONTAINER_NAME" > /dev/null || {
        fail "정지된 legacy 를 지우지 못했습니다 — 중단합니다"
        exit 1
      }
    fi
    docker rename "${APP_CONTAINER_NAME}-blue" "$APP_CONTAINER_NAME" || {
      fail "docker rename 실패 — 중단합니다"
      exit 1
    }
    log "blue 의 이름을 ${APP_CONTAINER_NAME} 로 바꿨습니다 — 서비스 중단 없음"
  elif [ "$LIVE" = "green" ] && is_running "${APP_CONTAINER_NAME}-green"; then
    IMAGE="$(docker inspect --format='{{.Config.Image}}' "${APP_CONTAINER_NAME}-green")"
    log "현재 live(green) 와 같은 이미지로 띄웁니다: ${IMAGE}"
  fi
fi

if [ "$TARGET" = "legacy" ] && [ -z "${IMAGE:-}" ] \
   && container_exists "$APP_CONTAINER_NAME" && ! is_running "$APP_CONTAINER_NAME"; then
  log "정지돼 있던 legacy 를 다시 띄웁니다: $(docker inspect --format='{{.Config.Image}}' "$APP_CONTAINER_NAME") (정지 $(docker inspect --format='{{.State.FinishedAt}}' "$APP_CONTAINER_NAME" | cut -c1-19) UTC)"
fi

if [ "$TARGET" = "$LIVE" ]; then
  fail "[주의] ${TARGET} 는 이미 live 슬롯입니다. 되돌릴 대상이 맞는지 확인하세요."
  show_status
fi

log "복구 대상: ${TARGET} (컨테이너 ${CONTAINER}, 포트 ${PORT})"

# ── 1. 대상 컨테이너 확보 ────────────────────────────────────────────────────
if docker ps --format '{{.Names}}' | grep -qx "$CONTAINER"; then
  log "컨테이너가 이미 실행 중입니다"
  if [ -n "${IMAGE:-}" ]; then
    fail "[주의] 실행 중인 컨테이너를 그대로 씁니다 — 지정한 IMAGE 는 쓰지 않았습니다"
  fi
elif [ -z "${IMAGE:-}" ] && docker ps -a --format '{{.Names}}' | grep -qx "$CONTAINER"; then
  log "정지된 컨테이너를 다시 시작합니다"
  if ! docker start "$CONTAINER"; then
    fail "docker start 실패 — 죽은 포트로 전환하지 않도록 중단합니다"
    exit 1
  fi
elif [ -n "${IMAGE:-}" ]; then
  # IMAGE 를 지정했으면 정지된 컨테이너를 교체합니다
  if docker ps -a --format '{{.Names}}' | grep -qx "$CONTAINER"; then
    log "정지된 ${CONTAINER} 를 지정한 이미지로 교체합니다"
    docker rm "$CONTAINER" > /dev/null || {
      fail "docker rm 실패 — 중단합니다"
      exit 1
    }
  fi
  log "새로 기동합니다 (이미지: $IMAGE)"
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
  fail "컨테이너 ${CONTAINER} 가 없고 IMAGE 도 지정되지 않았습니다."
  fail "복구할 이미지 태그를 지정해 다시 실행하세요:"
  fail "  IMAGE=<이미지> bash rollback.sh ${TARGET}"
  suggest_images
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
UPSTREAM_EXISTED=0
if [ -f "$UPSTREAM_CONF" ]; then
  UPSTREAM_EXISTED=1
  sudo cp "$UPSTREAM_CONF" "$BACKUP_CONF"
fi

# 실패했을 때 upstream 을 원래 상태로 되돌립니다.
# 원래 "없던" 파일이면 되돌리기 = 삭제입니다. 남겨두면 다음 배포의
# 슬롯 판별이 이 파일을 읽어 실제 nginx 상태와 어긋납니다.
restore_upstream() {
  if [ "$UPSTREAM_EXISTED" = "1" ]; then
    sudo cp "$BACKUP_CONF" "$UPSTREAM_CONF"
  else
    fail "원래 없던 upstream 파일을 삭제합니다: $UPSTREAM_CONF"
    sudo rm -f "$UPSTREAM_CONF"
  fi
  if sudo nginx -t > /dev/null 2>&1; then
    sudo nginx -s reload || true
  fi
  rm -f "$BACKUP_CONF"
}

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
  restore_upstream
  fail "설정을 직접 확인해야 합니다: $UPSTREAM_CONF"
  exit 1
fi

# set -e 를 쓰지 않으므로 reload 실패를 명시적으로 잡아야 합니다.
# 그냥 두면 실패해도 성공 로그를 찍고 최종 확인까지 진행합니다.
if ! sudo nginx -s reload; then
  fail "nginx reload 실패 — upstream 파일을 원래대로 되돌립니다"
  restore_upstream
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

# 다른 컨테이너는 정지하지 않으므로 안내만 합니다
if [ "$TARGET" = "legacy" ] && container_exists "${APP_CONTAINER_NAME}-green"; then
  log "green 컨테이너가 남아 있습니다. 확인 후 정리하세요:  docker rm -f ${APP_CONTAINER_NAME}-green"
fi

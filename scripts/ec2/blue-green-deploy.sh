#!/usr/bin/env bash
#
# blue/green 무중단 배포 (app-main)
#
# 기존 restart-container.sh 를 대체합니다. 동작 차이는 하나입니다.
#
# 슬롯은 호스트 포트로 구분합니다. 컨테이너 내부 포트는 언제나 8080 입니다.
#   blue  = 8080   (기존 단일 컨테이너가 쓰던 포트)
#   green = 18080
#
# nginx 는 upstream 파일 하나만 교체하고 reload 합니다.
# 서버 블록은 upstream 을 "이름"으로 참조하므로 건드릴 필요가 없습니다.
#
# ── 필수 환경변수 ────────────────────────────────────────────────────────────
#   APP_CONTAINER_NAME   컨테이너 이름 접두사 (예: causw-app)
#   IMAGE                배포할 이미지 (ECR 전체 경로)
#   MIGRATION_IMAGE      이미지 정리에서 보존할 마이그레이션 이미지
#   AWS_REGION           ECR 로그인용 리전
#
# ── 선택 환경변수 (미지정 시 아래 기본값) ────────────────────────────────────
#   CONTAINER_MEM_MB        컨테이너 RAM 한도(MB). 0 이면 제한 없음        기본 0
#   CONTAINER_MEMSWAP_MB    RAM+스왑 합계 한도(MB).                        기본 = CONTAINER_MEM_MB
#                           기본값이면 스왑을 전혀 쓰지 않습니다(prod 권장).
#                           CONTAINER_MEM_MB 보다 크게 주면 그 차이만큼
#                           스왑 사용을 허용합니다(dev 겹침 구간용).
#   JAVA_TOOL_OPTIONS       JVM 추가 옵션. 비우면 Dockerfile 기본값 사용   기본 빈값
#   HEALTH_TIMEOUT_SECONDS  헬스체크 최대 대기                            기본 300
#   GRACE_PERIOD_SECONDS    전환 후 구 컨테이너를 남겨두는 시간           기본 30
#   STOP_TIMEOUT_SECONDS    구 컨테이너 graceful shutdown 대기            기본 40
#   NEW_OOM_SCORE_ADJ       새 컨테이너의 OOM 우선순위 가산치             기본 500
#                           (비우면 미적용. 아래 "OOM 방향" 주석 참고)
#   MIN_AVAILABLE_MB        기동 전 최소 가용 메모리(MB). 미달이면 중단.  기본 0(검사안함)
#
#

set -euo pipefail

: "${APP_CONTAINER_NAME:?APP_CONTAINER_NAME variable is required}"
: "${IMAGE:?IMAGE variable is required}"
: "${MIGRATION_IMAGE:?MIGRATION_IMAGE variable is required}"
: "${AWS_REGION:?AWS_REGION variable is required}"

BLUE_PORT="${BLUE_PORT:-8080}"
GREEN_PORT="${GREEN_PORT:-18080}"
UPSTREAM_NAME="${UPSTREAM_NAME:-app-main}"
UPSTREAM_CONF="${UPSTREAM_CONF:-/etc/nginx/conf.d/app-main-upstream.conf}"
ENV_FILE="${ENV_FILE:-/home/ubuntu/app/app-main/.env}"
LOG_DIR="${LOG_DIR:-/var/log/spring-boot/app-main}"

CONTAINER_MEM_MB="${CONTAINER_MEM_MB:-0}"
CONTAINER_MEMSWAP_MB="${CONTAINER_MEMSWAP_MB:-}"
JAVA_TOOL_OPTIONS="${JAVA_TOOL_OPTIONS:-}"
HEALTH_TIMEOUT_SECONDS="${HEALTH_TIMEOUT_SECONDS:-300}"
GRACE_PERIOD_SECONDS="${GRACE_PERIOD_SECONDS:-30}"
STOP_TIMEOUT_SECONDS="${STOP_TIMEOUT_SECONDS:-40}"
NEW_OOM_SCORE_ADJ="${NEW_OOM_SCORE_ADJ:-500}"
MIN_AVAILABLE_MB="${MIN_AVAILABLE_MB:-0}"

LOCK_FILE="/tmp/${APP_CONTAINER_NAME}-deploy.lock"

log()  { printf '[%s] %s\n' "$(date '+%H:%M:%S')" "$*"; }
fail() { printf '[%s] ERROR: %s\n' "$(date '+%H:%M:%S')" "$*" >&2; }

# ── 동시 배포 방지 ───────────────────────────────────────────────────────────
# 배포가 겹치면 두 실행이 같은 슬롯을 서로 차지하려 들어 상태가 깨집니다.
exec 9>"$LOCK_FILE"
if ! flock -n 9; then
  fail "다른 배포가 진행 중입니다. 종료합니다."
  exit 1
fi

# ── 사전 점검 ────────────────────────────────────────────────────────────────
# upstream 파일이 없으면 nginx 준비가 끝나지 않은 상태입니다.
# 여기서 파일을 새로 만들면 nginx 가 include 하지 않아 반영되지 않고,
# 배포는 성공했다고 착각하게 됩니다. 그래서 트래픽을 건드리지 않고 멈춥니다.
if [ ! -f "$UPSTREAM_CONF" ]; then
  fail "upstream 파일이 없습니다: $UPSTREAM_CONF"
  fail "nginx 최초 준비가 필요합니다. infra/nginx/README.md 를 참고하세요."
  exit 1
fi

if ! command -v nginx > /dev/null 2>&1; then
  fail "nginx 를 찾을 수 없습니다."
  exit 1
fi

# ── 현재 슬롯 판별 ───────────────────────────────────────────────────────────
# green(18080)을 먼저 확인합니다. ":8080" 으로 먼저 검사하면
# ":18080" 도 함께 걸려서 슬롯을 잘못 판단합니다.
if grep -qE "server[[:space:]]+[^;]*:${GREEN_PORT};" "$UPSTREAM_CONF"; then
  CURRENT_SLOT=green
  TARGET_SLOT=blue
  TARGET_PORT="$BLUE_PORT"
  CURRENT_PORT="$GREEN_PORT"
elif grep -qE "server[[:space:]]+[^;]*:${BLUE_PORT};" "$UPSTREAM_CONF"; then
  CURRENT_SLOT=blue
  TARGET_SLOT=green
  TARGET_PORT="$GREEN_PORT"
  CURRENT_PORT="$BLUE_PORT"
else
  fail "upstream 파일에서 슬롯을 판별할 수 없습니다: $UPSTREAM_CONF"
  sed 's/^/  /' "$UPSTREAM_CONF" >&2
  exit 1
fi

CURRENT_CONTAINER="${APP_CONTAINER_NAME}-${CURRENT_SLOT}"
TARGET_CONTAINER="${APP_CONTAINER_NAME}-${TARGET_SLOT}"

# blue/green 이전의 단일 컨테이너가 아직 8080 을 잡고 있는지 확인합니다.
# 첫 배포에서는 이 컨테이너가 live 이고, 슬롯 컨테이너는 아직 없습니다.
LEGACY_RUNNING=0
if docker ps --format '{{.Names}}' | grep -qx "$APP_CONTAINER_NAME"; then
  LEGACY_RUNNING=1
fi

log "현재 슬롯: ${CURRENT_SLOT}(${CURRENT_PORT}) → 배포 대상: ${TARGET_SLOT}(${TARGET_PORT})"
if [ "$LEGACY_RUNNING" = "1" ]; then
  log "기존 단일 컨테이너(${APP_CONTAINER_NAME})가 실행 중입니다 — 전환 후 정지합니다"
fi

# 첫 배포인데 대상이 blue(8080)라면 기존 단일 컨테이너와 포트가 충돌합니다.
if [ "$LEGACY_RUNNING" = "1" ] && [ "$TARGET_PORT" = "$BLUE_PORT" ]; then
  fail "대상 슬롯이 blue(${BLUE_PORT})인데 기존 단일 컨테이너가 같은 포트를 쓰고 있습니다."
  fail "최초 전환은 green 으로 가야 합니다. upstream 파일이 blue 를 가리키고 있는지 확인하세요:"
  fail "  $UPSTREAM_CONF"
  exit 1
fi

# ── 이미지 준비 ──────────────────────────────────────────────────────────────
aws ecr get-login-password --region "$AWS_REGION" \
  | docker login --username AWS --password-stdin "${IMAGE%%/*}"
docker pull "$IMAGE"

# 로그 디렉터리 준비 (기존 방식과 동일)
sudo mkdir -p "$LOG_DIR"
sudo chmod -R a+rwX "$LOG_DIR"

# 정리에서 보존할 현재 및 직전 이미지 ID 확인
PREVIOUS_IMAGE_ID="$(
  docker inspect --format='{{.Image}}' "$CURRENT_CONTAINER" 2>/dev/null || true
)"
if [ -z "$PREVIOUS_IMAGE_ID" ] && [ "$LEGACY_RUNNING" = "1" ]; then
  PREVIOUS_IMAGE_ID="$(
    docker inspect --format='{{.Image}}' "$APP_CONTAINER_NAME" 2>/dev/null || true
  )"
fi
CURRENT_IMAGE_ID="$(docker image inspect --format='{{.Id}}' "$IMAGE")"
IMAGE_REPOSITORY="${IMAGE%:*}"

# ── 대상 슬롯 정리 ───────────────────────────────────────────────────────────
# 지난 배포가 중간에 실패해 컨테이너가 남아있을 수 있습니다.
if docker ps -a --format '{{.Names}}' | grep -qx "$TARGET_CONTAINER"; then
  log "대상 슬롯에 남아있는 컨테이너를 제거합니다: $TARGET_CONTAINER"
  docker rm -f "$TARGET_CONTAINER" > /dev/null
fi

# ── 가용 메모리 점검 ─────────────────────────────────────────────────────────
# 겹침 구간에 들어가기 전 마지막으로 되돌릴 수 있는 지점입니다.
# 여기서 멈추면 live 컨테이너는 아무 영향도 받지 않습니다.
MEM_AVAIL_MB="$(awk '/^MemAvailable:/ {print int($2/1024)}' /proc/meminfo 2>/dev/null || echo 0)"
SWAP_FREE_MB="$(awk '/^SwapFree:/ {print int($2/1024)}' /proc/meminfo 2>/dev/null || echo 0)"
log "가용 메모리: ${MEM_AVAIL_MB}MB (스왑 여유 ${SWAP_FREE_MB}MB)"

if [ "$MIN_AVAILABLE_MB" != "0" ] && [ "$MEM_AVAIL_MB" -lt "$MIN_AVAILABLE_MB" ]; then
  fail "가용 메모리가 부족합니다 (${MEM_AVAIL_MB}MB < 기준 ${MIN_AVAILABLE_MB}MB)"
  fail "겹침 구간에 들어가면 live 컨테이너까지 위험해지므로 배포를 중단합니다."
  fail "트래픽은 ${CURRENT_SLOT}(${CURRENT_PORT}) 에 그대로 있습니다. 아무것도 바뀌지 않았습니다."
  exit 1
fi

# ── 새 컨테이너 기동 ─────────────────────────────────────────────────────────
MEM_ARGS=()
if [ "$CONTAINER_MEM_MB" = "0" ]; then
  log "새 컨테이너 기동: ${TARGET_CONTAINER} (메모리 제한 없음)"
else
  MEMSWAP_MB="${CONTAINER_MEMSWAP_MB:-$CONTAINER_MEM_MB}"
  # 합계 한도가 RAM 한도보다 작으면 docker 가 거부합니다. 미리 막습니다.
  if [ "$MEMSWAP_MB" -lt "$CONTAINER_MEM_MB" ]; then
    fail "CONTAINER_MEMSWAP_MB(${MEMSWAP_MB})가 CONTAINER_MEM_MB(${CONTAINER_MEM_MB})보다 작습니다"
    exit 1
  fi
  MEM_ARGS=(--memory="${CONTAINER_MEM_MB}m" --memory-swap="${MEMSWAP_MB}m")
  if [ "$MEMSWAP_MB" = "$CONTAINER_MEM_MB" ]; then
    log "새 컨테이너 기동: ${TARGET_CONTAINER} (RAM ${CONTAINER_MEM_MB}MB, 스왑 불허)"
  else
    log "새 컨테이너 기동: ${TARGET_CONTAINER} (RAM ${CONTAINER_MEM_MB}MB, 스왑 $(( MEMSWAP_MB - CONTAINER_MEM_MB ))MB 까지 허용)"
  fi
fi

JAVA_ARGS=()
if [ -n "$JAVA_TOOL_OPTIONS" ]; then
  JAVA_ARGS=(-e "JAVA_TOOL_OPTIONS=${JAVA_TOOL_OPTIONS}")
  log "JVM 추가 옵션: ${JAVA_TOOL_OPTIONS}"
fi

# 호스트 메모리가 바닥났을 때 커널이 live 가 아니라 이쪽을 먼저 죽이게 합니다.
# 파일 상단 "OOM 방향" 주석 참고.
OOM_ARGS=()
if [ -n "$NEW_OOM_SCORE_ADJ" ]; then
  OOM_ARGS=(--oom-score-adj="$NEW_OOM_SCORE_ADJ")
  log "OOM 우선순위 가산: ${NEW_OOM_SCORE_ADJ} (전역 OOM 시 새 컨테이너가 먼저 희생)"
fi

# 배열이 빈 경우 set -u 때문에 "unbound variable" 이 납니다.
# ${arr[@]+"${arr[@]}"} 는 비어있으면 아무것도 전개하지 않는 안전한 형태입니다.
docker run -d \
  --name "$TARGET_CONTAINER" \
  --restart unless-stopped \
  -p "${TARGET_PORT}:8080" \
  --env-file "$ENV_FILE" \
  -v "${LOG_DIR}:/app/log" \
  --add-host=host.docker.internal:host-gateway \
  ${MEM_ARGS[@]+"${MEM_ARGS[@]}"} \
  ${JAVA_ARGS[@]+"${JAVA_ARGS[@]}"} \
  ${OOM_ARGS[@]+"${OOM_ARGS[@]}"} \
  "$IMAGE"

# ── 헬스체크 게이트 ──────────────────────────────────────────────────────────
# 여기를 통과하지 못하면 트래픽은 그대로 구 컨테이너에 남습니다.
log "헬스체크 대기 (최대 ${HEALTH_TIMEOUT_SECONDS}초)"
START_TS="$(date +%s)"
DEADLINE=$(( START_TS + HEALTH_TIMEOUT_SECONDS ))
HEALTHY=0
TICK=0
LIVE_DOWN_REPORTED=0

while [ "$(date +%s)" -lt "$DEADLINE" ]; do
  # 컨테이너가 죽었으면 더 기다릴 이유가 없습니다.
  STATE="$(docker inspect --format='{{.State.Status}}' "$TARGET_CONTAINER" 2>/dev/null || echo missing)"
  if [ "$STATE" != "running" ]; then
    EXIT_CODE="$(docker inspect --format='{{.State.ExitCode}}' "$TARGET_CONTAINER" 2>/dev/null || echo '?')"
    OOM="$(docker inspect --format='{{.State.OOMKilled}}' "$TARGET_CONTAINER" 2>/dev/null || echo '?')"
    fail "컨테이너가 종료되었습니다 (상태 ${STATE}, exit ${EXIT_CODE}, OOMKilled ${OOM})"
    break
  fi

  # curl 실패는 정상적인 대기 상태입니다. set -e 에 걸리지 않도록 || true 를 붙입니다.
  BODY="$(curl -s --max-time 5 "http://127.0.0.1:${TARGET_PORT}/actuator/health" 2>/dev/null || true)"
  if printf '%s' "$BODY" | grep -q '"status":"UP"'; then
    HEALTHY=1
    break
  fi

  # 겹침 구간에서 live 컨테이너가 먼저 쓰러지는 경우를 잡아냅니다.
  # (--oom-score-adj 로 방향을 뒤집어 두었지만 절대적이지는 않습니다)
  if [ "$LIVE_DOWN_REPORTED" = "0" ] \
    && docker ps -a --format '{{.Names}}' | grep -qx "$CURRENT_CONTAINER" \
    && [ "$(docker inspect --format='{{.State.Status}}' "$CURRENT_CONTAINER" 2>/dev/null || echo '')" != "running" ]; then
    LIVE_OOM="$(docker inspect --format='{{.State.OOMKilled}}' "$CURRENT_CONTAINER" 2>/dev/null || echo '?')"
    fail "!! live 컨테이너(${CURRENT_CONTAINER})가 내려갔습니다 (OOMKilled ${LIVE_OOM})"
    fail "   지금 서비스가 중단된 상태입니다. 새 컨테이너 기동을 계속 기다립니다 —"
    fail "   이 시점에서는 그것이 가장 빠른 복구 경로입니다."
    LIVE_DOWN_REPORTED=1
  fi

  TICK=$(( TICK + 1 ))
  if [ $(( TICK % 6 )) -eq 0 ]; then
    log "  ... 대기 중 ($(( $(date +%s) - START_TS ))초 경과)"
  fi
  sleep 5
done

if [ "$HEALTHY" -ne 1 ]; then
  fail "헬스체크 실패 — 트래픽을 전환하지 않고 롤백합니다"
  fail "새 컨테이너 로그 (마지막 50줄):"
  docker logs --tail 50 "$TARGET_CONTAINER" 2>&1 | sed 's/^/  /' >&2 || true
  docker rm -f "$TARGET_CONTAINER" > /dev/null 2>&1 || true
  fail "트래픽은 ${CURRENT_SLOT}(${CURRENT_PORT}) 에 그대로 남아있습니다."
  exit 1
fi

log "헬스체크 통과 ($(( $(date +%s) - START_TS ))초)"

# ── nginx 트래픽 전환 ────────────────────────────────────────────────────────
BACKUP_CONF="$(mktemp)"
sudo cp "$UPSTREAM_CONF" "$BACKUP_CONF"

sudo tee "$UPSTREAM_CONF" > /dev/null <<EOF
# blue-green-deploy.sh 가 생성했습니다.
# 이 파일은 "지금 어느 슬롯이 live 인가"를 담은 런타임 상태입니다.
# 직접 수정하지 마세요. git 으로 관리되지 않습니다.
# 생성 시각: $(date '+%Y-%m-%d %H:%M:%S') / slot=${TARGET_SLOT}
upstream ${UPSTREAM_NAME} {
    server 127.0.0.1:${TARGET_PORT};
    keepalive 64;
}
EOF

if ! sudo nginx -t; then
  fail "nginx -t 실패 — upstream 파일을 원래대로 되돌립니다"
  sudo cp "$BACKUP_CONF" "$UPSTREAM_CONF"
  rm -f "$BACKUP_CONF"
  docker rm -f "$TARGET_CONTAINER" > /dev/null 2>&1 || true
  exit 1
fi

sudo nginx -s reload
rm -f "$BACKUP_CONF"
log "트래픽을 ${TARGET_SLOT}(${TARGET_PORT}) 로 전환했습니다"

# upstream 파일이 실제로 대상 슬롯을 가리키는지 확인합니다.
# nginx 를 통한 curl 은 Certbot 리다이렉트(301) 때문에 판정에 쓸 수 없어
# 파일 내용을 직접 확인합니다.
if ! grep -qE "server[[:space:]]+127\\.0\\.0\\.1:${TARGET_PORT};" "$UPSTREAM_CONF"; then
  fail "upstream 파일이 예상과 다릅니다 — 즉시 확인이 필요합니다: $UPSTREAM_CONF"
  sed 's/^/  /' "$UPSTREAM_CONF" >&2
  exit 1
fi

# ── 유예기간 ─────────────────────────────────────────────────────────────────
# reload 직후에도 구 워커가 처리 중인 요청이 남아있습니다.
# 이 시간 동안은 구 컨테이너를 그대로 둡니다.
log "유예기간 ${GRACE_PERIOD_SECONDS}초 대기"
sleep "$GRACE_PERIOD_SECONDS"

# ── 구 컨테이너 종료 ─────────────────────────────────────────────────────────
# --time 은 graceful shutdown 이 끝날 때까지 기다리는 시간입니다.
# application.yml 의 timeout-per-shutdown-phase(30s) 보다 넉넉해야 합니다.
if docker ps -a --format '{{.Names}}' | grep -qx "$CURRENT_CONTAINER"; then
  log "구 컨테이너 종료: $CURRENT_CONTAINER"
  docker stop --time="$STOP_TIMEOUT_SECONDS" "$CURRENT_CONTAINER" > /dev/null || true
  docker rm "$CURRENT_CONTAINER" > /dev/null || true
fi

# 기존 단일 컨테이너는 제거하지 않고 정지만 합니다.
# blue/green 이 잘못됐을 때 되돌아갈 마지막 수단으로 남겨둡니다.
# (rollback.sh legacy 로 다시 살릴 수 있습니다)
if [ "$LEGACY_RUNNING" = "1" ]; then
  log "기존 단일 컨테이너 정지 (복구용으로 삭제하지 않고 남겨둡니다): $APP_CONTAINER_NAME"
  docker stop --time="$STOP_TIMEOUT_SECONDS" "$APP_CONTAINER_NAME" > /dev/null || true
fi

# ── 이미지 정리 (기존 restart-container.sh 와 동일한 규칙) ───────────────────
IMAGE_LIST="$(
  docker image ls "$IMAGE_REPOSITORY" \
    --no-trunc \
    --format '{{.Repository}}:{{.Tag}} {{.ID}}'
)"
printf '%s\n' "$IMAGE_LIST" |
  while read -r image_ref image_id; do
    [ -n "$image_ref" ] || continue
    [ "$image_ref" = "<none>:<none>" ] && continue
    if [ "$image_ref" = "$MIGRATION_IMAGE" ] || \
      [ "$image_id" = "$CURRENT_IMAGE_ID" ] || \
      [ "$image_id" = "$PREVIOUS_IMAGE_ID" ]; then
      continue
    fi
    docker image rm "$image_ref" || true
  done

# ── 최종 검증 ────────────────────────────────────────────────────────────────
# "배포 성공" 이라고 말하기 전에 실제로 살아있는지 다시 확인합니다.
FINAL_STATE="$(docker inspect --format='{{.State.Status}}' "$TARGET_CONTAINER" 2>/dev/null || echo missing)"
FINAL_CODE="$(
  curl -s -o /dev/null -w '%{http_code}' --max-time 5 \
    "http://127.0.0.1:${TARGET_PORT}/actuator/health" 2>/dev/null || true
)"

docker ps --filter "name=${APP_CONTAINER_NAME}" \
  --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}'

if [ "$FINAL_STATE" != "running" ] || [ "$FINAL_CODE" != "200" ]; then
  fail "최종 검증 실패 (상태 ${FINAL_STATE}, 헬스체크 ${FINAL_CODE:-응답없음})"
  fail "트래픽은 이미 ${TARGET_SLOT} 로 전환된 상태입니다. 즉시 복구하세요:"
  fail "  bash ~/app/bin/rollback.sh ${CURRENT_SLOT}"
  exit 1
fi

log "배포 완료 — ${TARGET_SLOT}(${TARGET_PORT})"

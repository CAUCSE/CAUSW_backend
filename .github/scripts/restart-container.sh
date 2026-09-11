#!/usr/bin/env bash

set -euo pipefail

: "${APP_CONTAINER_NAME:?APP_CONTAINER_NAME variable is required}"
: "${IMAGE:?IMAGE variable is required}"
: "${MIGRATION_IMAGE:?MIGRATION_IMAGE variable is required}"
: "${AWS_REGION:?AWS_REGION variable is required}"

aws ecr get-login-password --region "$AWS_REGION" \
  | docker login --username AWS --password-stdin "${IMAGE%%/*}"
docker pull "$IMAGE"

# 로그 디렉터리 준비
sudo mkdir -p /var/log/spring-boot/app-main
sudo chmod -R a+rwX /var/log/spring-boot/app-main

# 정리에서 보존할 현재 및 직전 이미지 ID 확인
PREVIOUS_IMAGE_ID="$(
  docker inspect --format='{{.Image}}' "$APP_CONTAINER_NAME" 2>/dev/null || true
)"
CURRENT_IMAGE_ID="$(docker image inspect --format='{{.Id}}' "$IMAGE")"
IMAGE_REPOSITORY="${IMAGE%:*}"

# 기존 컨테이너 중지 및 제거
docker stop "$APP_CONTAINER_NAME" || true
docker rm "$APP_CONTAINER_NAME" || true

# 새 컨테이너 실행
docker run -d \
  --name "$APP_CONTAINER_NAME" \
  --restart unless-stopped \
  -p 8080:8080 \
  --env-file /home/ubuntu/app/app-main/.env \
  -v /var/log/spring-boot/app-main:/app/log \
  --add-host=host.docker.internal:host-gateway \
  "$IMAGE"

# 같은 ECR 저장소에서 현재 및 직전 버전을 제외한 이미지 삭제
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
    docker image rm "$image_ref"
  done

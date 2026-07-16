# Dev CD Local Docker Image Cleanup Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** EC2 로컬에서 현재 및 직전 애플리케이션 이미지만 보존하고, 같은 ECR 저장소의 migration 이미지와 더 오래된 이미지를 배포 후 삭제한다.

**Architecture:** 배포 직전 실행 중인 컨테이너와 새로 pull한 이미지에서 보호할 Docker 이미지 ID 두 개를 얻는다. 새 컨테이너가 정상적으로 생성된 뒤 동일 ECR 저장소의 이미지 레퍼런스만 조회해 보호 ID가 아닌 레퍼런스를 삭제하며, 전역 prune은 실행하지 않는다.

**Tech Stack:** GitHub Actions YAML, Bash, Docker CLI, appleboy/ssh-action

## Global Constraints

- EC2 로컬 Docker 이미지만 변경하고 ECR 원격 이미지는 변경하지 않는다.
- 현재 실행하는 애플리케이션 이미지와 직전에 실행하던 애플리케이션 이미지 한 개를 보존한다.
- 같은 ECR 저장소의 나머지 애플리케이션 이미지와 migration 이미지만 삭제한다.
- 다른 Docker 저장소의 이미지와 전역 dangling 이미지는 삭제하지 않는다.
- 새 컨테이너 실행 실패 시 이미지 정리를 실행하지 않는다.

---

### Task 1: EC2 배포 이미지 보호 및 저장소 한정 정리

**Files:**
- Modify: `.github/workflows/dev-cd-docker.yml:144-176`
- Reference: `docs/superpowers/specs/2026-07-16-dev-cd-image-cleanup-design.md`

**Interfaces:**
- Consumes: `needs.build-and-push.outputs.image`, `vars.APP_CONTAINER_NAME`, EC2 Docker daemon
- Produces: 실행 중인 현재 이미지와 직전 이미지 ID만 남은 애플리케이션 ECR 저장소의 로컬 이미지 집합

- [ ] **Step 1: 현재 워크플로가 보존 요구사항을 충족하지 못함을 확인**

Run:

```bash
test -z "$(rg -n 'PREVIOUS_IMAGE_ID|CURRENT_IMAGE_ID|IMAGE_REPOSITORY' .github/workflows/dev-cd-docker.yml)"
rg -n 'docker image prune -f' .github/workflows/dev-cd-docker.yml
```

Expected: 첫 명령은 exit 0으로 보호 ID 로직이 없음을 확인하고, 두 번째 명령은 기존 전역 prune 한 줄을 출력한다.

- [ ] **Step 2: 배포 스크립트를 이미지 ID 보호 방식으로 변경**

`.github/workflows/dev-cd-docker.yml`의 `Restart container with new image` 원격 스크립트를 다음 내용으로 변경한다.

```yaml
          script: |
            set -euo pipefail

            : "${APP_CONTAINER_NAME:?APP_CONTAINER_NAME variable is required}"
            : "${IMAGE:?IMAGE variable is required}"

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

            # 새 컨테이너 실행 (이미지는 flyway-migrate 단계에서 이미 pull됨)
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
              if [ "$image_id" = "$CURRENT_IMAGE_ID" ] || \
                 [ "$image_id" = "$PREVIOUS_IMAGE_ID" ]; then
                continue
              fi
              docker image rm "$image_ref"
            done
```

`set -euo pipefail`로 새 컨테이너 실행이나 삭제 루프가 실패하면 즉시 배포 단계를 실패시킨다. `docker image ls` 결과를 먼저 변수에 저장해 조회 실패가 삭제 루프에 가려지지 않게 한다.

- [ ] **Step 3: 변경된 안전 조건을 정적 검증**

Run:

```bash
rg -n 'PREVIOUS_IMAGE_ID|CURRENT_IMAGE_ID|IMAGE_REPOSITORY|--no-trunc|docker image rm' .github/workflows/dev-cd-docker.yml
if rg -n 'docker image prune' .github/workflows/dev-cd-docker.yml; then exit 1; fi
git diff --check
```

Expected: 첫 명령은 보호 ID 캡처, 저장소 필터, 전체 ID 조회, 선택 삭제 코드를 출력한다. 두 번째 명령은 출력 없이 exit 0이며, `git diff --check`도 출력 없이 exit 0이다.

- [ ] **Step 4: GitHub Actions YAML 파싱 검증**

Run:

```bash
ruby -e 'require "yaml"; YAML.parse_file(".github/workflows/dev-cd-docker.yml"); puts "YAML OK"'
```

Expected: `YAML OK` 출력과 exit 0.

- [ ] **Step 5: 변경 범위 검토**

Run:

```bash
git diff -- .github/workflows/dev-cd-docker.yml
git status --short
```

Expected: 배포 SSH 스크립트의 이미지 보존·정리 로직만 변경되고, 설계 및 구현 계획 문서 외에 관련 없는 파일 변경이 없다.

- [ ] **Step 6: 구현 커밋**

```bash
git add .github/workflows/dev-cd-docker.yml
git commit -m "fix: dev 배포 이미지 정리 범위 제한"
```

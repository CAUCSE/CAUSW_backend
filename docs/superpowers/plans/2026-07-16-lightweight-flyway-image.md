# Lightweight Flyway Migration Image Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the Gradle builder migration image in dev CD with an official Flyway CLI image that shares the existing dev configuration with Gradle.

**Architecture:** A versioned `flyway-dev.conf` owns all non-secret dev migration behavior. The Gradle plugin reads it through `configFiles`, while a `flyway/flyway:12.4.0-alpine` Docker stage copies the same file and migration SQL. An entrypoint maps the existing `DB_*` environment variables to Flyway variables, and the EC2 CD flow runs only `migrate` before deployment.

**Tech Stack:** Flyway 12.4.0, Gradle Flyway plugin, Docker multi-stage builds, GitHub Actions, Amazon ECR, EC2

## Global Constraints

- Pin the migration base image to `flyway/flyway:12.4.0-alpine`.
- Keep DB URL, username and password in the existing `.env`; never write them into a tracked Flyway config file.
- Run `migrate` only during normal dev CD. Do not run `repair` automatically.
- Change only the dev Docker CD path; do not modify production or archived workflows.
- Preserve the current and previous application image behavior from the existing cleanup work.
- Preserve the current migration image tag so its Flyway base layers remain reusable.
- Do not run automated tests, Docker builds or migration integration tests, per the user's request.
- Perform only static diff and configuration-linkage checks after implementation.

---

### Task 1: Make dev Flyway behavior a shared config file

**Files:**
- Create: `app-main/gradle/flyway/flyway-dev.conf`
- Modify: `app-main/gradle/flyway/flyway-config.gradle:61-67`

**Interfaces:**
- Consumes: Existing Gradle environment selection through `env` and existing `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` project properties.
- Produces: A tracked Flyway legacy config at `app-main/gradle/flyway/flyway-dev.conf`, consumed by both Gradle and the migration Docker stage.

- [ ] **Step 1: Create the shared dev configuration**

Create `app-main/gradle/flyway/flyway-dev.conf` with exactly these non-secret settings:

```properties
flyway.outOfOrder=false
flyway.validateOnMigrate=true
flyway.cleanDisabled=true
flyway.baselineOnMigrate=true
```

- [ ] **Step 2: Point the Gradle dev profile at the shared file**

Replace the current inline dev assignments in `app-main/gradle/flyway/flyway-config.gradle` with:

```groovy
        case 'dev':
            logger.lifecycle("🚀 Applying DEV Flyway configuration")
            configFiles = [project.file("gradle/flyway/flyway-dev.conf").absolutePath]
            break
```

Keep `url`, `user`, `password`, and the Gradle-specific migration `locations` assignment in the surrounding `flyway` block unchanged.

- [ ] **Step 3: Check the shared settings statically**

Run:

```bash
git diff --check -- app-main/gradle/flyway/flyway-dev.conf app-main/gradle/flyway/flyway-config.gradle
rg -n "flyway\.(outOfOrder|validateOnMigrate|cleanDisabled|baselineOnMigrate)|configFiles" app-main/gradle/flyway
```

Expected: `git diff --check` prints nothing. `rg` shows each dev property once in `flyway-dev.conf` and the `configFiles` assignment in `flyway-config.gradle`.

- [ ] **Step 4: Commit the shared configuration**

```bash
git add app-main/gradle/flyway/flyway-dev.conf app-main/gradle/flyway/flyway-config.gradle
git commit -m "refactor: share Flyway dev configuration"
```

### Task 2: Add the official Flyway migration image stage

**Files:**
- Create: `docker/flyway/entrypoint.sh`
- Modify: `Dockerfile:1`

**Interfaces:**
- Consumes: `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` from Docker `--env-file`; `app-main/gradle/flyway/flyway-dev.conf` from Task 1.
- Produces: Docker build target `migration` whose entrypoint accepts Flyway commands such as `migrate`.

- [ ] **Step 1: Create the environment-mapping entrypoint**

Create `docker/flyway/entrypoint.sh`:

```sh
#!/usr/bin/env sh
set -eu

: "${DB_URL:?DB_URL is required}"
: "${DB_USERNAME:?DB_USERNAME is required}"
: "${DB_PASSWORD:?DB_PASSWORD is required}"

export FLYWAY_URL="$DB_URL"
export FLYWAY_USER="$DB_USERNAME"
export FLYWAY_PASSWORD="$DB_PASSWORD"

exec flyway "$@"
```

- [ ] **Step 2: Add the migration stage before the application builder**

Insert this stage at the beginning of `Dockerfile`, before the existing builder stage:

```dockerfile
# ─── Flyway Migration ─────────────────────────────────────────────────────────
FROM flyway/flyway:12.4.0-alpine AS migration

COPY app-main/src/main/resources/db/migration /flyway/migrations
COPY app-main/gradle/flyway/flyway-dev.conf /flyway/conf/flyway-dev.conf
COPY --chmod=755 docker/flyway/entrypoint.sh /flyway/causw-entrypoint.sh

ENV FLYWAY_CONFIG_FILES=/flyway/conf/flyway-dev.conf
ENV FLYWAY_LOCATIONS=filesystem:/flyway/migrations

ENTRYPOINT ["/flyway/causw-entrypoint.sh"]
CMD ["migrate"]

```

Leave the existing application builder and runtime stages unchanged.

- [ ] **Step 3: Check the image wiring statically**

Run:

```bash
git diff --check -- Dockerfile docker/flyway/entrypoint.sh
rg -n "flyway/flyway:12\.4\.0-alpine|AS migration|FLYWAY_CONFIG_FILES|FLYWAY_LOCATIONS|causw-entrypoint" Dockerfile
rg -n "DB_URL|DB_USERNAME|DB_PASSWORD|exec flyway" docker/flyway/entrypoint.sh
```

Expected: no whitespace errors; the Docker target, config file, migration location, three required DB variables, and `exec flyway` are all present. Do not build or run the image.

- [ ] **Step 4: Commit the migration image**

```bash
git add Dockerfile docker/flyway/entrypoint.sh
git commit -m "feat: add lightweight Flyway migration image"
```

### Task 3: Run the lightweight image in dev CD

**Files:**
- Modify: `.github/workflows/dev-cd-docker.yml:52-199`

**Interfaces:**
- Consumes: Docker target `migration`; build job outputs `image` and `migration_image`; EC2 `.env`; existing ECR access.
- Produces: A Flyway job that pulls only the migration image and a deploy job that independently pulls the application image while preserving the current migration tag during cleanup.

- [ ] **Step 1: Build the migration target instead of the Gradle builder**

In the build script, replace:

```bash
docker build --target builder -t "$MIGRATION_IMAGE" .
```

with:

```bash
docker build --target migration -t "$MIGRATION_IMAGE" .
```

- [ ] **Step 2: Replace repair-plus-migrate with one Flyway CLI invocation**

Rename the job to `Flyway Migration`. In `Run Flyway on EC2`, remove the `IMAGE` environment variable and use this script:

```yaml
        env:
          MIGRATION_IMAGE: ${{ needs.build-and-push.outputs.migration_image }}
          AWS_REGION: ${{ env.AWS_REGION }}
        with:
          key: ${{ secrets.EC2_KEY_DEV }}
          host: ${{ secrets.EC2_HOST_DEV }}
          username: ${{ secrets.EC2_USER_DEV }}
          envs: MIGRATION_IMAGE,AWS_REGION
          script: |
            set -euo pipefail

            : "${MIGRATION_IMAGE:?MIGRATION_IMAGE variable is required}"

            aws ecr get-login-password --region "$AWS_REGION" \
              | docker login --username AWS --password-stdin "${MIGRATION_IMAGE%%/*}"

            docker pull "$MIGRATION_IMAGE"

            docker run --rm \
              --env-file /home/ubuntu/app/app-main/.env \
              --add-host=host.docker.internal:host-gateway \
              "$MIGRATION_IMAGE" \
              migrate
```

Do not retain the `.env` bind mount, either Gradle command, or `|| true`.

- [ ] **Step 3: Make deployment pull its own application image**

Add `MIGRATION_IMAGE` and `AWS_REGION` to the deploy action environment and `envs` list:

```yaml
        env:
          IMAGE: ${{ needs.build-and-push.outputs.image }}
          MIGRATION_IMAGE: ${{ needs.build-and-push.outputs.migration_image }}
          APP_CONTAINER_NAME: ${{ vars.APP_CONTAINER_NAME }}
          AWS_REGION: ${{ env.AWS_REGION }}
```

```yaml
          envs: IMAGE,MIGRATION_IMAGE,APP_CONTAINER_NAME,AWS_REGION
```

After the existing required-variable checks, add:

```bash
: "${MIGRATION_IMAGE:?MIGRATION_IMAGE variable is required}"

aws ecr get-login-password --region "$AWS_REGION" \
  | docker login --username AWS --password-stdin "${IMAGE%%/*}"
docker pull "$IMAGE"
```

Update the new-container comment so it no longer says the application image was pulled by the Flyway job.

- [ ] **Step 4: Preserve the current migration image tag during cleanup**

Extend the existing cleanup condition to keep the exact migration image reference:

```bash
if [ "$image_ref" = "$MIGRATION_IMAGE" ] || \
   [ "$image_id" = "$CURRENT_IMAGE_ID" ] || \
   [ "$image_id" = "$PREVIOUS_IMAGE_ID" ]; then
  continue
fi
```

This removes older migration tags but retains the currently deployed migration image layers.

- [ ] **Step 5: Check the workflow statically**

Run:

```bash
git diff --check -- .github/workflows/dev-cd-docker.yml
rg -n -- "--target migration|Flyway Migration|docker pull|MIGRATION_IMAGE|flywayRepair|flywayMigrate|gradlew" .github/workflows/dev-cd-docker.yml
```

Expected: the migration target and one Flyway CLI migration invocation are present; neither `flywayRepair`, `flywayMigrate`, nor `gradlew` appears in this workflow. Both jobs pull only the image they own.

- [ ] **Step 6: Commit the dev CD change**

```bash
git add .github/workflows/dev-cd-docker.yml
git commit -m "refactor: run dev migrations with Flyway CLI"
```

### Task 4: Final static review

**Files:**
- Review: `Dockerfile`
- Review: `docker/flyway/entrypoint.sh`
- Review: `app-main/gradle/flyway/flyway-dev.conf`
- Review: `app-main/gradle/flyway/flyway-config.gradle`
- Review: `.github/workflows/dev-cd-docker.yml`

**Interfaces:**
- Consumes: All outputs from Tasks 1-3.
- Produces: A clean, statically reviewed implementation handoff with an explicit test-not-run disclosure.

- [ ] **Step 1: Check repository cleanliness and whitespace**

Run:

```bash
git status --short --branch
git diff --check HEAD~3..HEAD
```

Expected: the branch has no uncommitted implementation files and the diff check prints nothing.

- [ ] **Step 2: Check configuration ownership and forbidden commands**

Run:

```bash
rg -n "flyway\.(outOfOrder|validateOnMigrate|cleanDisabled|baselineOnMigrate)" app-main/gradle/flyway/flyway-dev.conf
rg -n "configFiles" app-main/gradle/flyway/flyway-config.gradle
rg -n "flywayRepair|flywayMigrate|--target builder" .github/workflows/dev-cd-docker.yml
```

Expected: the four dev settings and Gradle `configFiles` reference are present. The final command prints nothing.

- [ ] **Step 3: Record skipped verification**

In the final handoff, state exactly:

```text
Tests and Docker image builds were not run, as requested. Verification was limited to static diff and configuration-linkage checks.
```

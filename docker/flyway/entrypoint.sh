#!/usr/bin/env sh
set -eu

: "${DB_URL:?DB_URL is required}"
: "${DB_USERNAME:?DB_USERNAME is required}"
: "${DB_PASSWORD:?DB_PASSWORD is required}"

export FLYWAY_URL="$DB_URL"
export FLYWAY_USER="$DB_USERNAME"
export FLYWAY_PASSWORD="$DB_PASSWORD"

exec flyway "$@"

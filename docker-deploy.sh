#!/usr/bin/env bash
set -euo pipefail

COMPOSE_FILE="${COMPOSE_FILE:-docker-compose.yml}"

if command -v docker-compose >/dev/null 2>&1; then
  DC=(docker-compose)
elif command -v docker >/dev/null 2>&1 && docker compose version >/dev/null 2>&1; then
  DC=(docker compose)
else
  echo "Error: docker-compose 或 docker compose 未安装/不可用" >&2
  exit 1
fi

if ! docker info >/dev/null 2>&1; then
  echo "Error: Docker daemon 未启动，请先启动 Docker Desktop" >&2
  exit 1
fi

SERVICES=("$@")
if [ "${#SERVICES[@]}" -eq 0 ]; then
  mapfile -t SERVICES < <("${DC[@]}" -f "$COMPOSE_FILE" config --services)
fi

PULL="${PULL:-0}"
RETRIES="${RETRIES:-3}"
SLEEP_BASE_SECONDS="${SLEEP_BASE_SECONDS:-2}"
VOLUMES="${VOLUMES:-0}"

build_one() {
  local service="$1"
  local attempt=1

  while [ "$attempt" -le "$RETRIES" ]; do
    if [ "$PULL" = "1" ]; then
      if "${DC[@]}" -f "$COMPOSE_FILE" build --pull "$service"; then
        return 0
      fi
      echo "Warn: ${service} build --pull 失败，尝试不带 --pull" >&2
    fi

    if "${DC[@]}" -f "$COMPOSE_FILE" build "$service"; then
      return 0
    fi

    if [ "$attempt" -lt "$RETRIES" ]; then
      local sleep_seconds=$((SLEEP_BASE_SECONDS * attempt))
      echo "Warn: ${service} build 失败，${sleep_seconds}s 后重试（${attempt}/${RETRIES}）" >&2
      sleep "$sleep_seconds"
    fi
    attempt=$((attempt + 1))
  done

  return 1
}

echo "Compose: ${DC[*]}"
echo "Compose file: ${COMPOSE_FILE}"
echo "Services: ${SERVICES[*]}"

for service in "${SERVICES[@]}"; do
  echo "==> Building: ${service}"
  build_one "$service"
done

echo "==> Stopping services"
DOWN_ARGS=(down --remove-orphans)
if [ "$VOLUMES" = "1" ]; then
  DOWN_ARGS+=(--volumes)
fi
"${DC[@]}" -f "$COMPOSE_FILE" "${DOWN_ARGS[@]}"

echo "==> Starting services"
"${DC[@]}" -f "$COMPOSE_FILE" up -d --remove-orphans "${SERVICES[@]}"
"${DC[@]}" -f "$COMPOSE_FILE" ps

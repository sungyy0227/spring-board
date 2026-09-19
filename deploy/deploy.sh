#!/usr/bin/env bash
set -Eeuo pipefail

PROJECT_DIR="${PROJECT_DIR:-$HOME/spring-board}"
WEB_ROOT="${WEB_ROOT:-/var/www/spring-board}"
NGINX_CONFIG="${NGINX_CONFIG:-/etc/nginx/sites-available/spring-board}"
NGINX_CONFIG_BACKUP="${NGINX_CONFIG_BACKUP:-}"
RELEASE_ID="$(date -u +%Y%m%d%H%M%S)"
RELEASE_DIR="$WEB_ROOT/releases/$RELEASE_ID"
NEXT_LINK="$WEB_ROOT/current-next"
ROLLBACK_IMAGE="spring-board-app:rollback-$RELEASE_ID"
PREVIOUS_RELEASE=""
PREVIOUS_APP_IMAGE=""
PROMOTED=0

rollback() {
    exit_code=$?
    trap - ERR
    set +e

    if [ "$PROMOTED" -eq 1 ]; then
        if [ -n "$PREVIOUS_RELEASE" ]; then
            sudo ln -sfn "$PREVIOUS_RELEASE" "$NEXT_LINK"
            sudo mv -Tf "$NEXT_LINK" "$WEB_ROOT/current"
        fi

        if [ -n "$PREVIOUS_APP_IMAGE" ]; then
            docker image tag "$ROLLBACK_IMAGE" spring-board-app:latest
            docker compose up -d --no-build app
        fi
    fi

    if [ -n "$NGINX_CONFIG_BACKUP" ] && [ -f "$NGINX_CONFIG_BACKUP" ]; then
        sudo cp "$NGINX_CONFIG_BACKUP" "$NGINX_CONFIG"
    fi

    sudo nginx -t && sudo systemctl reload nginx

    exit "$exit_code"
}

trap rollback ERR

cd "$PROJECT_DIR"

git pull --ff-only origin main

docker run --rm \
    --volume "$PROJECT_DIR/frontend:/app" \
    --volume spring-board-frontend-node-modules:/app/node_modules \
    --workdir /app \
    node:24-alpine \
    sh -c 'npm ci && npm run build'

PREVIOUS_APP_IMAGE="$(docker inspect --format '{{.Image}}' spring-board-app-1 2>/dev/null || true)"
if [ -n "$PREVIOUS_APP_IMAGE" ]; then
    docker image tag "$PREVIOUS_APP_IMAGE" "$ROLLBACK_IMAGE"
fi

docker compose build app

sudo install -d -m 755 "$WEB_ROOT/releases"
sudo install -d -m 755 "$RELEASE_DIR"
sudo cp -a "$PROJECT_DIR/frontend/dist/." "$RELEASE_DIR/"
sudo chown -R www-data:www-data "$RELEASE_DIR"

PREVIOUS_RELEASE="$(readlink -f "$WEB_ROOT/current" 2>/dev/null || true)"
sudo nginx -t

sudo ln -sfn "$RELEASE_DIR" "$NEXT_LINK"
sudo mv -Tf "$NEXT_LINK" "$WEB_ROOT/current"
PROMOTED=1

docker compose up -d --no-build app

ready=0
for _ in $(seq 1 20); do
    if curl --fail --silent http://127.0.0.1:8080/actuator/health >/dev/null; then
        ready=1
        break
    fi
    sleep 2
done

if [ "$ready" -ne 1 ]; then
    echo "Spring Boot readiness check failed" >&2
    false
fi

sudo nginx -t
sudo systemctl reload nginx

trap - ERR
docker compose ps
printf 'React release: %s\n' "$RELEASE_DIR"
printf 'Rollback image: %s\n' "$ROLLBACK_IMAGE"

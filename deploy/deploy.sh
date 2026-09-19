#!/usr/bin/env bash
set -Eeuo pipefail

PROJECT_DIR="${PROJECT_DIR:-$HOME/spring-board}"
WEB_ROOT="${WEB_ROOT:-/var/www/spring-board}"
RELEASE_ID="$(date -u +%Y%m%d%H%M%S)"
RELEASE_DIR="$WEB_ROOT/releases/$RELEASE_ID"
NEXT_LINK="$WEB_ROOT/current-next"

cd "$PROJECT_DIR"

git pull --ff-only origin main

docker compose up -d --build

docker run --rm \
    --volume "$PROJECT_DIR/frontend:/app" \
    --volume spring-board-frontend-node-modules:/app/node_modules \
    --workdir /app \
    node:24-alpine \
    sh -c 'npm ci && npm run build'

sudo install -d -m 755 "$WEB_ROOT/releases"
sudo install -d -m 755 "$RELEASE_DIR"
sudo cp -a "$PROJECT_DIR/frontend/dist/." "$RELEASE_DIR/"
sudo chown -R www-data:www-data "$RELEASE_DIR"

sudo ln -sfn "$RELEASE_DIR" "$NEXT_LINK"
sudo mv -Tf "$NEXT_LINK" "$WEB_ROOT/current"

sudo nginx -t
sudo systemctl reload nginx

docker compose ps
printf 'React release: %s\n' "$RELEASE_DIR"

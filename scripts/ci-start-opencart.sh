#!/usr/bin/env bash
# Install official OpenCart 3.0.3.8 and serve it on APP_URL (default http://127.0.0.1:8080/)
set -euo pipefail

APP_URL="${APP_URL:-http://127.0.0.1:8080/}"
DB_HOST="${DB_HOST:-127.0.0.1}"
DB_USER="${DB_USER:-root}"
DB_PASS="${DB_PASS:-root}"
DB_NAME="${DB_NAME:-opencart}"
DB_PREFIX="${DB_PREFIX:-oc_}"
WORK_DIR="${WORK_DIR:-$PWD/.ci-opencart}"
OC_ZIP_URL="${OC_ZIP_URL:-https://github.com/opencart/opencart/releases/download/3.0.3.8/opencart-3.0.3.8.zip}"

HOST_PORT="$(printf '%s' "$APP_URL" | sed -E 's#https?://[^:/]+:([0-9]+).*#\1#')"
if [[ "$HOST_PORT" == "$APP_URL" ]]; then
  HOST_PORT=8080
fi

rm -rf "$WORK_DIR"
mkdir -p "$WORK_DIR"
cd "$WORK_DIR"

echo "Downloading OpenCart 3.0.3.8..."
curl -fsSL -o oc.zip "$OC_ZIP_URL"
unzip -q oc.zip

# Zip layout: upload/ at top-level or nested under a single folder
if [[ -d upload ]]; then
  OC_ROOT="$WORK_DIR/upload"
elif [[ -d opencart-3.0.3.8/upload ]]; then
  OC_ROOT="$WORK_DIR/opencart-3.0.3.8/upload"
else
  OC_ROOT="$(find "$WORK_DIR" -type d -path '*/upload/catalog' | head -1 | sed 's#/catalog##')"
fi

if [[ -z "${OC_ROOT:-}" || ! -d "$OC_ROOT" ]]; then
  echo "Could not locate OpenCart upload/ directory"
  find "$WORK_DIR" -maxdepth 3 -type d | head -50
  exit 1
fi

echo "OpenCart root: $OC_ROOT"
cd "$OC_ROOT"

# OpenCart expects writable config stubs before CLI install
touch config.php admin/config.php
chmod 666 config.php admin/config.php
mkdir -p system/storage/{cache,download,logs,modification,session,upload,vendor}
chmod -R 777 system/storage image

echo "Waiting for MySQL at ${DB_HOST}..."
for i in $(seq 1 60); do
  if php -r "\$m=@new mysqli('${DB_HOST}','${DB_USER}','${DB_PASS}','${DB_NAME}'); exit((\$m && !\$m->connect_error)?0:1);"; then
    echo "MySQL is ready"
    break
  fi
  sleep 2
  if [[ $i -eq 60 ]]; then
    echo "MySQL did not become ready"
    exit 1
  fi
done

echo "Running OpenCart CLI install..."
cd install
php cli_install.php install \
  --db_driver mysqli \
  --db_hostname "$DB_HOST" \
  --db_username "$DB_USER" \
  --db_password "$DB_PASS" \
  --db_database "$DB_NAME" \
  --db_prefix "$DB_PREFIX" \
  --username admin \
  --password admin123 \
  --email admin@example.com \
  --http_server "$APP_URL"
cd ..
rm -rf install

echo "Starting PHP built-in server on 127.0.0.1:${HOST_PORT}..."
nohup php -S "127.0.0.1:${HOST_PORT}" -t "$OC_ROOT" >"$WORK_DIR/php-server.log" 2>&1 &
echo $! >"$WORK_DIR/php-server.pid"

for i in $(seq 1 60); do
  CODE="$(curl -sS -o /tmp/oc-body.html -w '%{http_code}' --max-time 5 -L "$APP_URL" || echo 000)"
  if [[ "$CODE" == "200" ]] && grep -Eqi 'My Account|Your Store|Featured|route=common/home' /tmp/oc-body.html; then
    echo "OpenCart storefront is up (HTTP $CODE)"
    echo "OC_ROOT=$OC_ROOT" >"$WORK_DIR/env.txt"
    exit 0
  fi
  sleep 2
done

echo "Storefront failed to become ready"
echo "===== php-server.log ====="
tail -100 "$WORK_DIR/php-server.log" || true
echo "===== curl ====="
curl -sSL -D- --max-time 5 "$APP_URL" | head -80 || true
exit 1

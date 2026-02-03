#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

DB_CMD="${DB_CMD:-}"
if [[ -z "$DB_CMD" ]]; then
  if command -v mariadb >/dev/null 2>&1; then
    DB_CMD="mariadb"
  elif command -v mysql >/dev/null 2>&1; then
    DB_CMD="mysql"
  else
    echo "ERROR: mariadb/mysql client not found in PATH."
    exit 1
  fi
fi

DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3306}"
DB_SOCKET="${DB_SOCKET:-/tmp/mysql.sock}"
DB_ROOT_USER="${DB_ROOT_USER:-root}"

EPMS_DEV_DB="${EPMS_DEV_DB:-epms_dev}"
EPMS_PROD_DB="${EPMS_PROD_DB:-epms_prod}"
EPMS_DEV_USER="${EPMS_DEV_USER:-epms_dev_user}"
EPMS_PROD_USER="${EPMS_PROD_USER:-epms_prod_user}"

## NOTE: Use Maven profiles (resources-dev/resources-prod) for DB config.

if [[ "${DB_ROOT_PASS+x}" != "x" ]]; then
  : # Use provided DB_ROOT_PASS (can be empty for socket auth)
else
  read -r -s -p "DB root password (leave empty for socket auth): " DB_ROOT_PASS
  echo
fi
DEFAULT_DEV_PASS="epms_dev_pass1!"
DEFAULT_PROD_PASS="epms_prod_pass1!"
if [[ -z "${EPMS_DEV_PASS:-}" ]]; then
  read -r -s -p "DEV user password (${EPMS_DEV_USER}) [default: ${DEFAULT_DEV_PASS}]: " EPMS_DEV_PASS
  echo
  EPMS_DEV_PASS="${EPMS_DEV_PASS:-$DEFAULT_DEV_PASS}"
fi
if [[ -z "${EPMS_PROD_PASS:-}" ]]; then
  read -r -s -p "PROD user password (${EPMS_PROD_USER}) [default: ${DEFAULT_PROD_PASS}]: " EPMS_PROD_PASS
  echo
  EPMS_PROD_PASS="${EPMS_PROD_PASS:-$DEFAULT_PROD_PASS}"
fi

CONNECT_OPTS=(-u "$DB_ROOT_USER")
if [[ -n "${DB_ROOT_PASS:-}" ]]; then
  CONNECT_OPTS+=("-p${DB_ROOT_PASS}")
fi
if [[ -S "$DB_SOCKET" ]]; then
  CONNECT_OPTS+=(--socket="$DB_SOCKET")
else
  CONNECT_OPTS+=(-h "$DB_HOST" -P "$DB_PORT")
fi

if ! "$DB_CMD" "${CONNECT_OPTS[@]}" -e "SELECT 1" >/dev/null 2>&1; then
  echo "WARN: Unable to connect as ${DB_ROOT_USER}. Check DB_ROOT_USER/DB_ROOT_PASS/DB_HOST/DB_PORT or socket."
  echo "WARN: Skipping DB setup."
  exit 0
fi

echo "==> Creating databases and users..."
if ! "$DB_CMD" "${CONNECT_OPTS[@]}" <<SQL
CREATE DATABASE IF NOT EXISTS ${EPMS_DEV_DB}  CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS ${EPMS_PROD_DB} CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE USER IF NOT EXISTS '${EPMS_DEV_USER}'@'localhost' IDENTIFIED BY '${EPMS_DEV_PASS}';
CREATE USER IF NOT EXISTS '${EPMS_PROD_USER}'@'localhost' IDENTIFIED BY '${EPMS_PROD_PASS}';

GRANT ALL PRIVILEGES ON ${EPMS_DEV_DB}.*  TO '${EPMS_DEV_USER}'@'localhost';
GRANT ALL PRIVILEGES ON ${EPMS_PROD_DB}.* TO '${EPMS_PROD_USER}'@'localhost';
FLUSH PRIVILEGES;
SQL
then
  echo "WARN: Failed to create databases/users. You may lack privileges."
  echo "WARN: Skipping DDL/DML apply."
  exit 0
fi

DDL_FILE="$PROJECT_ROOT/script/ddl/maria/com_DDL_maria.sql"
DML_FILE="$PROJECT_ROOT/script/dml/maria/com_DML_maria.sql"

if [[ ! -f "$DDL_FILE" || ! -f "$DML_FILE" ]]; then
  echo "WARN: DDL/DML files not found under script/ddl/maria or script/dml/maria."
  exit 0
fi

read -r -p "Apply DDL+DML to DEV DB (${EPMS_DEV_DB})? [Y/n] " APPLY_DEV
APPLY_DEV="${APPLY_DEV:-Y}"
if [[ "$APPLY_DEV" =~ ^[Yy]$ ]]; then
  echo "==> Applying DDL to ${EPMS_DEV_DB}..."
  if ! "$DB_CMD" --force "${CONNECT_OPTS[@]}" "$EPMS_DEV_DB" < "$DDL_FILE"; then
    echo "WARN: DDL apply failed for ${EPMS_DEV_DB}."
  fi
  echo "==> Applying DML to ${EPMS_DEV_DB}..."
  if ! "$DB_CMD" --force "${CONNECT_OPTS[@]}" "$EPMS_DEV_DB" < "$DML_FILE"; then
    echo "WARN: DML apply failed for ${EPMS_DEV_DB}."
  fi
fi

read -r -p "Apply DDL+DML to PROD DB (${EPMS_PROD_DB})? [Y/n] " APPLY_PROD
APPLY_PROD="${APPLY_PROD:-Y}"
if [[ "$APPLY_PROD" =~ ^[Yy]$ ]]; then
  echo "==> Applying DDL to ${EPMS_PROD_DB}..."
  if ! "$DB_CMD" --force "${CONNECT_OPTS[@]}" "$EPMS_PROD_DB" < "$DDL_FILE"; then
    echo "WARN: DDL apply failed for ${EPMS_PROD_DB}."
  fi
  echo "==> Applying DML to ${EPMS_PROD_DB}..."
  if ! "$DB_CMD" --force "${CONNECT_OPTS[@]}" "$EPMS_PROD_DB" < "$DML_FILE"; then
    echo "WARN: DML apply failed for ${EPMS_PROD_DB}."
  fi
fi

echo "Done."

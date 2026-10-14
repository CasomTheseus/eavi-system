#!/usr/bin/env bash
# ============================================================
# EAVI —— 数据库初始化脚本（macOS / Linux）
# 用法：bash scripts/db-init.sh
# ============================================================
set -euo pipefail

cd "$(dirname "$0")/.."
ROOT="$(pwd)"

MYSQL_USER="${MYSQL_USER:-root}"
DB_NAME="eavi_system"

echo "==> 仓库根目录：$ROOT"
echo "==> 将重建数据库 $DB_NAME 的所有表（已有数据会被清空）"
read -r -p "继续？(y/N) " reply
case "$reply" in
  [yY]|[yY][eE][sS]) ;;
  *) echo "已取消"; exit 0 ;;
esac

if ! command -v mysql >/dev/null 2>&1; then
  echo "错误：找不到 mysql 命令。请先安装 MySQL 8 客户端并加入 PATH。" >&2
  exit 1
fi

echo "==> 1/3 建库建表 (schema.sql)"
mysql -u "$MYSQL_USER" -p < "$ROOT/database/schema.sql"

echo "==> 2/3 导入演示数据 (seed.sql)"
mysql -u "$MYSQL_USER" -p < "$ROOT/database/seed.sql"

echo "==> 3/3 完整性自检 (check.sql)"
mysql -u "$MYSQL_USER" -p "$DB_NAME" < "$ROOT/database/check.sql"

echo
echo "完成。演示账号：lcy / 123456   qmn / 123456"
echo "接下来：cd frontend && npm install && npm run dev"

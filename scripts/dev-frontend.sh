#!/usr/bin/env bash
# ============================================================
# EAVI —— 启动前端开发服务器（macOS / Linux）
# 用法：bash scripts/dev-frontend.sh
# ============================================================
set -euo pipefail

cd "$(dirname "$0")/../frontend"

if ! command -v node >/dev/null 2>&1; then
  echo "错误：找不到 node。请安装 Node.js 18+" >&2
  exit 1
fi

echo "==> Node 版本：$(node -v)"

if [ ! -d node_modules ]; then
  echo "==> 首次运行，安装依赖（不要拷贝别人的 node_modules）"
  npm install
fi

echo "==> 启动 Vite，地址 http://localhost:3000"
echo "    注意：后端需要跑在 http://localhost:8080，否则登录会失败"
npm run dev

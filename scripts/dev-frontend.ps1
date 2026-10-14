# ============================================================
# EAVI —— 启动前端开发服务器（Windows PowerShell）
# 用法：powershell -ExecutionPolicy Bypass -File scripts\dev-frontend.ps1
# ============================================================

$ErrorActionPreference = 'Stop'

$Frontend = Join-Path (Split-Path -Parent $PSScriptRoot) 'frontend'
Set-Location $Frontend

$node = Get-Command node.exe -ErrorAction SilentlyContinue
if (-not $node) {
    Write-Error '找不到 node.exe。请安装 Node.js 18+ 并加入 PATH。'
}
Write-Host "==> Node 版本：$(& node -v)"

if (-not (Test-Path (Join-Path $Frontend 'node_modules'))) {
    Write-Host '==> 首次运行，安装依赖'
    Write-Host '    （不要在机器之间拷贝 node_modules —— 它含平台专用二进制）'
    & npm install
    if ($LASTEXITCODE -ne 0) { throw 'npm install 失败' }
}

Write-Host ''
Write-Host '==> 启动 Vite，地址 http://localhost:3000'
Write-Host '    注意：后端需要跑在 http://localhost:8080，否则登录会失败'
Write-Host ''
& npm run dev

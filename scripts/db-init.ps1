# ============================================================
# EAVI —— 数据库初始化脚本（Windows PowerShell）
# 用法：powershell -ExecutionPolicy Bypass -File scripts\db-init.ps1
# ============================================================

$ErrorActionPreference = 'Stop'

# 仓库根目录
$Root = Split-Path -Parent $PSScriptRoot
$MysqlUser = if ($env:MYSQL_USER) { $env:MYSQL_USER } else { 'root' }

Write-Host "==> 仓库根目录：$Root"
Write-Host "==> 将重建数据库 eavi_system 的所有表（已有数据会被清空）"
$reply = Read-Host "继续？(y/N)"
if ($reply -notmatch '^[yY]') { Write-Host '已取消'; exit 0 }

# 找 mysql.exe —— 优先 PATH，其次默认安装位置
$mysql = (Get-Command mysql.exe -ErrorAction SilentlyContinue).Source
if (-not $mysql) {
    $candidates = @(
        'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe',
        'C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe',
        'C:\xampp\mysql\bin\mysql.exe'
    )
    $mysql = $candidates | Where-Object { Test-Path $_ } | Select-Object -First 1
}
if (-not $mysql) {
    Write-Error '找不到 mysql.exe。请安装 MySQL 8 客户端，或把它的 bin 目录加入 PATH。'
}
Write-Host "==> 使用客户端：$mysql"

# 提示输入密码（不落盘、不出现在命令行历史里）
$secure = Read-Host "请输入 MySQL 的 $MysqlUser 密码" -AsSecureString
$plain  = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
            [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure))

function Invoke-SqlFile {
    param([string]$Path, [string]$Database)
    if (-not (Test-Path $Path)) { throw "找不到文件：$Path" }
    Write-Host "==> 执行 $Path"
    # 用 -p<密码> 而不是交互输入，避免 PowerShell 管道与 mysql 交互冲突
    $args = @("-u$MysqlUser", "-p$plain", '--default-character-set=utf8mb4')
    if ($Database) { $args += $Database }
    Get-Content $Path -Raw -Encoding UTF8 | & $mysql @args
    if ($LASTEXITCODE -ne 0) { throw "执行 $Path 失败，退出码 $LASTEXITCODE" }
}

Invoke-SqlFile -Path (Join-Path $Root 'database\schema.sql')
Invoke-SqlFile -Path (Join-Path $Root 'database\seed.sql')
Invoke-SqlFile -Path (Join-Path $Root 'database\check.sql') -Database 'eavi_system'

# 清掉内存里的明文密码
$plain = $null
[GC]::Collect()

Write-Host ''
Write-Host '完成。演示账号：lcy / 123456    qmn / 123456'
Write-Host '接下来：cd frontend; npm install; npm run dev'

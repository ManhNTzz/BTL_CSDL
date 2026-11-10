$ErrorActionPreference = "SilentlyContinue"
$projectRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$pidFile = Join-Path $projectRoot 'run\backend.json'
$stopped = $false

Write-Host "=== DANG DUNG UNG DUNG QUAN LY KTX ===" -ForegroundColor Cyan

# 1. Thử tắt theo file trạng thái run/backend.json chuẩn của script
if (Test-Path -LiteralPath $pidFile) {
    try {
        $state = Get-Content $pidFile -Raw | ConvertFrom-Json
        if ($state.pid) {
            $taskProcess = Get-Process -Id ([int]$state.pid) -ErrorAction SilentlyContinue
            if ($taskProcess) {
                Stop-Process -Id $taskProcess.Id -Force
                Write-Host "[OK] Da dung backend theo file trang thai (PID: $($state.pid))." -ForegroundColor Green
                $stopped = $true
            }
        }
    } catch {}
    Remove-Item -LiteralPath $pidFile -Force -ErrorAction SilentlyContinue
}

# 2. Quét dự phòng trực tiếp cổng 8080 (Xử lý trường hợp chạy từ VS Code hoặc lệch file json)
$conns = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
if ($conns) {
    $pids = $conns | Select-Object -ExpandProperty OwningProcess -Unique
    foreach ($p in $pids) {
        if ($p -gt 0) {
            $proc = Get-Process -Id $p -ErrorAction SilentlyContinue
            if ($proc) {
                Stop-Process -Id $p -Force
                Write-Host "[OK] Da giai phong cong 8080 (Da tat tien trinh $($proc.ProcessName) - PID: $p)." -ForegroundColor Green
                $stopped = $true
            }
        }
    }
}

if (-not $stopped) {
    Write-Host "[THONG BAO] Khong co tien trinh nao dang chiem cong 8080. He thống đã sạch!" -ForegroundColor Yellow
} else {
    Write-Host "[HOAN TAT] Da dong ung dụng va giai phong cong 8080 hoan toan!" -ForegroundColor Green
}
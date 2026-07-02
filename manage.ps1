# /manage.ps1
# 职责描述：一键启停所有服务，先停后启（backend / frontend / ai_engine）
# 用法：.\manage.ps1 stop      停止所有服务
#       .\manage.ps1 start     启动所有服务
#       .\manage.ps1 restart   先停再启（默认）

param(
    [ValidateSet("stop", "start", "restart")]
    [string]$Action = "restart"
)

$RootDir = Split-Path -Parent $MyInvocation.MyCommand.Path

# 通过端口号查杀进程
function Stop-ProcessByPort($Port, $Name) {
    try {
        $conn = Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue
        if ($conn) {
            $procId = $conn.OwningProcess
            Stop-Process -Id $procId -Force -ErrorAction Stop
            Write-Host "  [OK] $Name (PID $procId) 已停止" -ForegroundColor Green
        } else {
            Write-Host "  [--] $Name 未运行" -ForegroundColor DarkGray
        }
    } catch {
        Write-Host "  [!!] $Name 停止失败: $_" -ForegroundColor Red
    }
}

function Stop-AllServices {
    Write-Host "`n[1/2] 正在停止所有服务..." -ForegroundColor Yellow
    Stop-ProcessByPort 8080 "Backend (Spring Boot)"
    Stop-ProcessByPort 5173 "Frontend (Vite)"
    Stop-ProcessByPort 8000 "AI Engine (FastAPI)"
    Start-Sleep -Seconds 2
    Write-Host "所有服务已停止。`n" -ForegroundColor Green
}

function Wait-ForPort($Port, $Name, $TimeoutSec) {
    $elapsed = 0
    while ($elapsed -lt $TimeoutSec) {
        try {
            $conn = Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue
            if ($conn) {
                Write-Host "  [OK] $Name 已就绪 (localhost:$Port)" -ForegroundColor Green
                return $true
            }
        } catch { }
        Start-Sleep -Seconds 3
        $elapsed += 3
        Write-Host "  ...等待 $Name 启动中 ($elapsed/$TimeoutSec 秒)" -ForegroundColor DarkGray
    }
    Write-Host "  [!!] $Name 启动超时，请检查终端窗口中的错误信息" -ForegroundColor Red
    return $false
}

function Start-AllServices {
    Write-Host "[2/2] 正在启动所有服务..." -ForegroundColor Yellow

    # 1) Backend — 耗时最长，先启动
    Write-Host "  [..] Backend 启动中（首次编译需 3-8 分钟下载依赖）..." -ForegroundColor Cyan
    Start-Process powershell -WorkingDirectory "$RootDir\backend" -WindowStyle Normal `
        -ArgumentList "-NoExit", "-Command", ".\mvnw.cmd spring-boot:run"

    # 2) Frontend
    Start-Sleep -Seconds 3
    Write-Host "  [..] Frontend 启动中..." -ForegroundColor Cyan
    Start-Process powershell -WorkingDirectory "$RootDir\frontend" -WindowStyle Normal `
        -ArgumentList "-NoExit", "-Command", "npm run dev"

    # 3) AI Engine
    Start-Sleep -Seconds 2
    Write-Host "  [..] AI Engine 启动中..." -ForegroundColor Cyan
    Start-Process powershell -WorkingDirectory "$RootDir\ai_engine" -WindowStyle Normal `
        -ArgumentList "-NoExit", "-Command", "conda activate jobs_1; uvicorn app.main:app --reload --port 8000"

    # 等待后端就绪（最长等 5 分钟）
    Write-Host "`n  -- 等待 Backend 编译启动，请稍候..." -ForegroundColor Yellow
    Wait-ForPort 8080 "Backend" 300

    Write-Host "`n所有服务已启动。" -ForegroundColor Green
    Write-Host "  Backend: localhost:8080" -ForegroundColor Gray
    Write-Host "  Frontend: localhost:5173" -ForegroundColor Gray
    Write-Host "  AI:      localhost:8000" -ForegroundColor Gray
    Write-Host "`n执行 .\manage.ps1 stop 可停止所有服务`n" -ForegroundColor Gray
}

switch ($Action) {
    "stop"    { Stop-AllServices }
    "start"   { Start-AllServices }
    "restart" { Stop-AllServices; Start-AllServices }
}

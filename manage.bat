@echo off
title Agent Platform Service Manager

:: /manage.bat
:: Usage: double-click to restart all services (stop then start)
::        manage.bat stop       stop all services
::        manage.bat start      start all services
::        manage.bat restart    stop then start (default)

set ROOT=%~dp0

if /i "%1"=="stop" goto stop
if /i "%1"=="start" goto start
if /i "%1"=="restart" goto restart
if "%1"=="" goto restart

:usage
echo Usage: manage.bat [stop^|start^|restart]
pause
exit /b

:: ============================================================
:stop
echo.
echo [1/2] Stopping all services...
call :kill_by_port 8080 "Backend - Spring Boot"
call :kill_by_port 5173 "Frontend - Vite"
call :kill_by_port 8000 "AI Engine - FastAPI"
echo.
echo All services stopped.
if /i "%1"=="stop" pause
exit /b

:: ============================================================
:start
echo.
echo [2/2] Starting all services...

:: 1) Backend
echo.
echo   Backend starting (first time may take 3-8 min)...
start "Backend" /D "%ROOT%backend" cmd /k mvnw.cmd spring-boot:run

:: 2) Frontend
timeout /t 3 /nobreak >nul
echo   Frontend starting...
start "Frontend" /D "%ROOT%frontend" cmd /k npm run dev

:: 3) AI Engine
timeout /t 2 /nobreak >nul
echo   AI Engine starting...
start "AI Engine" /D "%ROOT%ai_engine" cmd /k "conda activate jobs_1 && uvicorn app.main:app --reload --port 8000"

:: Wait for backend
echo.
echo   Waiting for Backend to be ready...
call :wait_port 8080 "Backend" 300

echo.
echo All services started.
echo   Backend: localhost:8080
echo   Frontend: localhost:5173
echo   AI:      localhost:8000
if /i not "%1"=="start" pause
exit /b

:: ============================================================
:restart
call :stop
echo.
call :start
exit /b

:: ============================================================
:kill_by_port
set PORT=%~1
set NAME=%~2
for /f "tokens=5" %%p in ('netstat -ano ^| findstr "LISTENING" ^| findstr ":%PORT% "') do (
    taskkill /f /pid %%p >nul 2>nul && (
        echo   [OK] %NAME% stopped - PID: %%p
    ) || (
        echo   [!!] %NAME% stop failed
    )
    goto :eof
)
echo   [--] %NAME% not running
goto :eof

:: ============================================================
:wait_port
set PORT=%~1
set NAME=%~2
set TIMEOUT=%~3
set ELAPSED=0

:wait_loop
netstat -ano | findstr "LISTENING" | findstr ":%PORT% " >nul 2>nul
if not errorlevel 1 (
    echo   [OK] %NAME% ready - localhost:%PORT%
    goto :eof
)
ping -n 4 127.0.0.1 >nul
set /a ELAPSED+=3
echo   Waiting for %NAME%... (%ELAPSED%/%TIMEOUT% sec)
if %ELAPSED% lss %TIMEOUT% goto wait_loop
echo   [!!] %NAME% timeout, check the window for errors
goto :eof
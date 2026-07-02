#!/usr/bin/env bash
# /manage.sh
# 职责描述：Linux 服务管理脚本（Docker + 三服务）
#
# Usage:
#   ./manage.sh start      启动所有服务
#   ./manage.sh stop       停止所有服务
#   ./manage.sh restart     重启所有服务
#   ./manage.sh status      查看服务状态

set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
COMPOSE_FILE="$ROOT/docker-compose.yml"

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; NC='\033[0m'
ok()   { echo -e "  ${GREEN}[OK]${NC} $1"; }
info() { echo -e "  ${YELLOW}[--]${NC} $1"; }
fail() { echo -e "  ${RED}[!!]${NC} $1"; }

# ============================================================
# 通过端口号杀掉进程
# ============================================================
kill_by_port() {
    local port="$1" name="$2"
    if fuser "$port/tcp" 2>/dev/null >/dev/null; then
        fuser -k "$port/tcp" 2>/dev/null && ok "$name stopped (port $port)" || fail "$name stop failed"
    else
        info "$name not running"
    fi
}

# ============================================================
# 等待端口可用（超时 N 秒）
# ============================================================
wait_port() {
    local port="$1" name="$2" timeout="$3" elapsed=0
    while [ $elapsed -lt "$timeout" ]; do
        if lsof -ti ":$port" >/dev/null 2>&1; then
            ok "$name ready - localhost:$port"
            return 0
        fi
        sleep 3
        elapsed=$((elapsed + 3))
        echo "  Waiting for $name... (${elapsed}/${timeout} sec)"
    done
    fail "$name timeout, check logs"
    return 1
}

# ============================================================
# Stop
# ============================================================
stop() {
    echo ""
    echo "[1/2] Stopping all services..."
    kill_by_port 8080 "Backend - Spring Boot"
    kill_by_port 5173 "Frontend - Vite"
    kill_by_port 8000 "AI Engine - FastAPI"
    echo ""
    echo "  Stopping Docker containers..."
    docker compose -f "$COMPOSE_FILE" down 2>/dev/null && ok "Docker containers stopped" || info "No Docker containers running"
    echo ""
    ok "All services stopped."
}

# ============================================================
# Start
# ============================================================
start() {
    echo ""
    echo "[2/2] Starting all services..."

    # Infrastructure via Docker Compose
    echo ""
    info "Starting infrastructure (MySQL + Redis + RabbitMQ)..."
    docker compose -f "$COMPOSE_FILE" up -d mysql redis rabbitmq 2>&1 | sed 's/^/  /'
    echo ""
    info "Waiting for MySQL to be healthy..."
    docker compose -f "$COMPOSE_FILE" exec -T mysql mysqladmin ping -uroot -proot123 --silent --wait=30 2>/dev/null && ok "MySQL is healthy" || fail "MySQL not ready"
    redis-cli -h localhost ping 2>/dev/null | grep -q PONG && ok "Redis is ready" || fail "Redis not ready"

    # Nginx reverse proxy
    if systemctl is-active --quiet nginx; then
        ok "Nginx is running"
    else
        info "Starting nginx..."
        systemctl start nginx && ok "Nginx started"
    fi

    # 1) Backend
    echo ""
    info "Backend starting (first time may take 3-8 min)..."
    cd "$ROOT/backend"
    nohup ./mvnw spring-boot:run > "$ROOT/logs/backend.log" 2>&1 &
    BACKEND_PID=$!
    echo "  PID: $BACKEND_PID"

    # 2) Frontend
    sleep 3
    info "Frontend starting..."
    cd "$ROOT/frontend"
    nohup npm run dev > "$ROOT/logs/frontend.log" 2>&1 &
    FRONTEND_PID=$!
    echo "  PID: $FRONTEND_PID"

    # 3) AI Engine
    sleep 2
    info "AI Engine starting..."
    cd "$ROOT/ai_engine"
    nohup .venv/bin/uvicorn app.main:app --reload --port 8000 > "$ROOT/logs/ai_engine.log" 2>&1 &
    AI_PID=$!
    echo "  PID: $AI_PID"

    # Wait for backend to be ready
    echo ""
    wait_port 8080 "Backend" 300

    echo ""
    ok "All services started."
    echo "   Backend:  http://localhost:8080"
    echo "   Frontend: http://localhost:5173"
    echo "   AI:       http://localhost:8000"
    echo "   Unified:  http://localhost (nginx)"
    echo ""
    echo "Log files: $ROOT/logs/"
}

# ============================================================
# Status
# ============================================================
status() {
    echo ""
    echo "Service Status:"
    echo "  Backend  :8080  $(lsof -ti :8080  >/dev/null 2>&1 && echo -e "${GREEN}RUNNING${NC}" || echo -e "${RED}STOPPED${NC}")"
    echo "  Frontend :5173  $(lsof -ti :5173  >/dev/null 2>&1 && echo -e "${GREEN}RUNNING${NC}" || echo -e "${RED}STOPPED${NC}")"
    echo "  AI Engine:8000  $(lsof -ti :8000  >/dev/null 2>&1 && echo -e "${GREEN}RUNNING${NC}" || echo -e "${RED}STOPPED${NC}")"
    echo ""
    echo "Docker Containers:"
    docker compose -f "$COMPOSE_FILE" ps --format "table {{.Name}}\t{{.Status}}\t{{.Ports}}" 2>/dev/null | sed 's/^/  /' || info "Docker not available"
    echo ""
    echo "Reverse Proxy:"
    echo "  Nginx    :80    $(systemctl is-active --quiet nginx && echo -e "${GREEN}RUNNING${NC}" || echo -e "${RED}STOPPED${NC}")"
}

# ============================================================
# Main
# ============================================================
mkdir -p "$ROOT/logs"

case "${1:-restart}" in
    stop)    stop ;;
    start)   start ;;
    restart) stop; sleep 2; start ;;
    status)  status ;;
    *)
        echo "Usage: $0 {start|stop|restart|status}"
        exit 1
        ;;
esac
